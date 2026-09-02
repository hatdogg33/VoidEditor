package com.voideditor.patch

import android.content.Context
import com.android.apksig.ApkSigner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.android.tools.smali.dexlib2.DexFileFactory
import com.android.tools.smali.dexlib2.HiddenApiRestriction
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.Annotation
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.DexFile
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.MethodParameter
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableDexFile
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.cert.X509Certificate
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

sealed class PatchPhase {
    data object Idle : PatchPhase()
    data object Running : PatchPhase()
    data object Done : PatchPhase()
    data object Cancelled : PatchPhase()
    data class Failed(val message: String) : PatchPhase()
}

class PatchCancelledException : Exception("patch cancelled")

data class PatchLibVariant(val name: String, val abi: String, val file: File)

object ApkPatcher {

    private const val LoaderBase = "Leditorespatch"
    private const val LibraryAlignment = 16384
    private val HelperPattern = Regex("^Leditorespatch\\d+;$")
    private val KnownAbis = listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86", "riscv64")

    fun outputDir(context: Context): File = File(context.cacheDir, "patch").apply { mkdirs() }

    fun scanLibraries(projectDir: File): List<PatchLibVariant> {
        val buildDir = File(projectDir, "build")
        if (!buildDir.isDirectory) return emptyList()
        val presets = buildDir.listFiles { file -> file.isDirectory }?.sortedBy { it.name } ?: return emptyList()
        val variants = mutableListOf<PatchLibVariant>()
        val seen = mutableSetOf<String>()
        for (preset in presets) {
            val abi = KnownAbis.firstOrNull { preset.name.startsWith("android-$it-") } ?: continue
            val libs = preset.listFiles { file -> file.isFile && file.name.startsWith("lib") && file.name.endsWith(".so") } ?: continue
            for (lib in libs.sortedBy { it.name }) {
                val name = lib.name.removePrefix("lib").removeSuffix(".so")
                if (seen.add(name + "|" + abi)) variants.add(PatchLibVariant(name, abi, lib))
            }
        }
        return variants.sortedWith(compareBy({ it.name }, { it.abi }))
    }

    suspend fun patch(
        context: Context,
        apkFile: File,
        variant: PatchLibVariant,
        cancelled: AtomicBoolean,
        onLine: (String) -> Unit,
        onPhase: (PatchPhase) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "patch-work")
        workDir.deleteRecursively()
        workDir.mkdirs()
        val outDir = outputDir(context)
        val unsigned = File(outDir, apkFile.nameWithoutExtension + "-patched-unsigned.apk")
        val signed = File(outDir, apkFile.nameWithoutExtension + "-patched.apk")
        try {
            onPhase(PatchPhase.Running)
            if (!apkFile.isFile) throw IllegalArgumentException("apk file not found: " + apkFile.absolutePath)
            onLine("opening " + apkFile.name)
            val manifestBytes = ZipFile(apkFile).use { zip ->
                val entry = zip.getEntry("AndroidManifest.xml")
                    ?: throw IllegalArgumentException("AndroidManifest.xml missing")
                zip.getInputStream(entry).use { it.readBytes() }
            }
            if (cancelled.get()) throw PatchCancelledException()
            onLine("parsing manifest")
            val launcher = AxmlParser.launcherActivity(manifestBytes)
            val targetType = "L" + launcher.replace(".", "/") + ";"
            onLine("launcher activity: " + launcher)
            val dexNames = ZipFile(apkFile).use { zip ->
                zip.entries().toList().map { it.name }
                    .filter { it == "classes.dex" || (it.startsWith("classes") && it.endsWith(".dex")) }
                    .sorted()
            }
            val opcodes = Opcodes.getDefault()
            val oldLibraries = LinkedHashSet<String>()
            val replacedDexes = LinkedHashMap<String, File>()
            val marker = "editorespatch".toByteArray(Charsets.US_ASCII)
            var patchedTarget = false
            for (dexName in dexNames) {
                if (cancelled.get()) throw PatchCancelledException()
                onLine("scanning " + dexName)
                val raw = ZipFile(apkFile).use { zip ->
                    val entry = zip.getEntry(dexName) ?: return@use null
                    zip.getInputStream(entry).use { input -> input.readBytes() }
                } ?: continue
                if (patchedTarget && !containsBytes(raw, marker)) continue
                val extracted = File(workDir, dexName.replace("/", "_"))
                extracted.outputStream().use { output -> output.write(raw) }
                val dexFile = DexFileFactory.loadDexFile(extracted, opcodes)
                var patchedClass: ClassDef? = null
                var injectorClass: ClassDef? = null
                val targetClass = if (patchedTarget) null else dexFile.classes.firstOrNull { it.type == targetType }
                if (targetClass != null) {
                    onLine("target class found in " + dexName)
                    val onCreate = targetClass.methods.firstOrNull {
                        it.name == "onCreate" && it.parameterTypes == listOf("Landroid/os/Bundle;") && it.returnType == "V"
                    } ?: throw IllegalArgumentException("onCreate(Landroid/os/Bundle;)V not found in " + launcher)
                    val implementation = onCreate.implementation
                        ?: throw IllegalArgumentException("onCreate has no implementation in " + launcher)
                    val mutable = MutableMethodImplementation(implementation)
                    val removed = removeHelperInvokes(mutable)
                    if (removed > 0) onLine("removed " + removed + " previous patch call(s) from onCreate")
                    val loaderType = findLoaderType(dexFile)
                    injectorClass = buildInjector(loaderType, variant.name)
                    mutable.addInstruction(
                        0,
                        BuilderInstruction35c(
                            Opcode.INVOKE_STATIC, 0, 0, 0, 0, 0, 0,
                            ImmutableMethodReference(loaderType, "load", emptyList(), "V")
                        )
                    )
                    val directMethods = targetClass.directMethods.map { method ->
                        if (method.name == "onCreate" && method.parameterTypes == listOf("Landroid/os/Bundle;") && method.returnType == "V") patchMethod(method, mutable) else method
                    }
                    val virtualMethods = targetClass.virtualMethods.map { method ->
                        if (method.name == "onCreate" && method.parameterTypes == listOf("Landroid/os/Bundle;") && method.returnType == "V") patchMethod(method, mutable) else method
                    }
                    patchedClass = ImmutableClassDef(
                        targetClass.type,
                        targetClass.accessFlags,
                        targetClass.superclass,
                        targetClass.interfaces,
                        targetClass.sourceFile,
                        targetClass.annotations,
                        targetClass.staticFields,
                        targetClass.instanceFields,
                        directMethods,
                        virtualMethods
                    )
                    patchedTarget = true
                }
                var stripped = false
                val combined = LinkedHashSet<ClassDef>()
                for (cls in dexFile.classes) {
                    when {
                        HelperPattern.matches(cls.type) -> {
                            helperLibraryName(cls)?.let { oldLibraries.add(it) }
                            stripped = true
                        }
                        cls.type == targetType && patchedClass != null -> combined.add(patchedClass!!)
                        else -> combined.add(cls)
                    }
                }
                if (patchedClass != null) combined.add(injectorClass!!)
                if (patchedClass != null || stripped) {
                    val patchedDex = File(workDir, "patched-" + dexName)
                    DexFileFactory.writeDexFile(patchedDex.absolutePath, ImmutableDexFile(opcodes, combined))
                    replacedDexes[dexName] = patchedDex
                    if (patchedClass != null) onLine("injected loadLibrary(" + variant.name + ") into onCreate")
                    if (stripped) onLine("removed previous patch from " + dexName)
                }
            }
            if (!patchedTarget) throw IllegalArgumentException("target class " + launcher + " not found in any dex")
            if (oldLibraries.isNotEmpty()) onLine("removing previous libraries: " + oldLibraries.joinToString(", "))
            onLine("packing apk")
            val counter = CountingOutputStream(FileOutputStream(unsigned))
            val zipOut = ZipOutputStream(counter)
            zipOut.use {
                ZipFile(apkFile).use { source ->
                    val entries = source.entries().toList()
                    for (entry in entries) {
                        if (cancelled.get()) throw PatchCancelledException()
                        val name = entry.name
                        when {
                            entry.isDirectory -> {
                                zipOut.putNextEntry(ZipEntry(name))
                                zipOut.closeEntry()
                            }
                            name.startsWith("META-INF/") -> Unit
                            replacedDexes.containsKey(name) -> Unit
                            isOldLibraryEntry(name, oldLibraries) -> Unit
                            name == "lib/" + variant.abi + "/lib" + variant.name + ".so" -> Unit
                            else -> copyEntry(source, entry, zipOut, counter)
                        }
                    }
                }
                for ((dexEntryName, patchedDexFile) in replacedDexes) {
                    val dexEntry = ZipEntry(dexEntryName)
                    dexEntry.method = ZipEntry.DEFLATED
                    zipOut.putNextEntry(dexEntry)
                    patchedDexFile.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
                val target = "lib/" + variant.abi + "/lib" + variant.name + ".so"
                val bytes = variant.file.readBytes()
                val crc = CRC32().apply { update(bytes) }
                val nameBytes = target.toByteArray(Charsets.UTF_8)
                val pad = alignmentPad(counter.count + 30L + nameBytes.size, LibraryAlignment)
                val libEntry = ZipEntry(target).apply {
                    method = ZipEntry.STORED
                    size = bytes.size.toLong()
                    compressedSize = bytes.size.toLong()
                    this.crc = crc.value
                    extra = pad
                }
                zipOut.putNextEntry(libEntry)
                zipOut.write(bytes)
                zipOut.closeEntry()
                onLine("added " + target)
            }
            onLine("signing v1 v2 v3")
            val signerEntry = PatcherKeystore.entry(context)
            val signerCertificate = signerEntry.certificate as X509Certificate
            val signerConfig = ApkSigner.SignerConfig.Builder(
                "EditorEs Patcher",
                signerEntry.privateKey,
                listOf(signerCertificate)
            ).build()
            ApkSigner.Builder(listOf(signerConfig))
                .setInputApk(unsigned)
                .setOutputApk(signed)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .setV3SigningEnabled(true)
                .build()
                .sign()
            onLine("done: " + signed.name)
            onPhase(PatchPhase.Done)
            signed
        } catch (cancelledError: PatchCancelledException) {
            onLine("patch cancelled")
            onPhase(PatchPhase.Cancelled)
            null
        } catch (error: Exception) {
            onLine("failed: " + (error.message ?: error.javaClass.simpleName))
            onPhase(PatchPhase.Failed(error.message ?: "patch failed"))
            null
        } finally {
            workDir.deleteRecursively()
            unsigned.delete()
        }
    }

    private fun findLoaderType(dexFile: DexFile): String {
        val existing = dexFile.classes.filter { !HelperPattern.matches(it.type) }.map { it.type }.toHashSet()
        var index = 0
        while (true) {
            val candidate = LoaderBase + index + ";"
            if (candidate !in existing) return candidate
            index++
        }
    }

    private fun removeHelperInvokes(mutable: MutableMethodImplementation): Int {
        val instructions = mutable.instructions.toList()
        val targets = mutableListOf<Int>()
        for (index in instructions.indices) {
            val reference = (instructions[index] as? ReferenceInstruction)?.reference as? MethodReference ?: continue
            if (HelperPattern.matches(reference.definingClass) && reference.name == "load") targets.add(index)
        }
        for (index in targets.asReversed()) mutable.removeInstruction(index)
        return targets.size
    }

    private fun helperLibraryName(cls: ClassDef): String? {
        val load = cls.methods.firstOrNull { it.name == "load" } ?: return null
        val implementation = load.implementation ?: return null
        for (instruction in implementation.instructions) {
            val reference = (instruction as? ReferenceInstruction)?.reference as? StringReference ?: continue
            return reference.string
        }
        return null
    }

    private fun isOldLibraryEntry(name: String, oldLibraries: Set<String>): Boolean {
        if (oldLibraries.isEmpty() || !name.startsWith("lib/") || !name.endsWith(".so")) return false
        val fileName = name.substringAfterLast('/').removePrefix("lib").removeSuffix(".so")
        return fileName in oldLibraries
    }

    private fun containsBytes(data: ByteArray, marker: ByteArray): Boolean {
        if (data.size < marker.size) return false
        outer@ for (index in 0..data.size - marker.size) {
            for (offset in marker.indices) {
                if (data[index + offset] != marker[offset]) continue@outer
            }
            return true
        }
        return false
    }

    private fun buildInjector(loaderType: String, libName: String): ClassDef {
        val implementation = ImmutableMethodImplementation(
            1,
            listOf(
                BuilderInstruction21c(Opcode.CONST_STRING, 0, ImmutableStringReference(libName)),
                BuilderInstruction35c(
                    Opcode.INVOKE_STATIC, 1, 0, 0, 0, 0, 0,
                    ImmutableMethodReference("Ljava/lang/System;", "loadLibrary", listOf("Ljava/lang/String;"), "V")
                ),
                BuilderInstruction10x(Opcode.RETURN_VOID)
            ),
            emptyList(),
            emptyList()
        )
        val method = ImmutableMethod(
            loaderType,
            "load",
            emptyList<MethodParameter>(),
            "V",
            0x9,
            emptySet<Annotation>(),
            emptySet<HiddenApiRestriction>(),
            implementation
        )
        return ImmutableClassDef(
            loaderType,
            0x11,
            "Ljava/lang/Object;",
            emptyList(),
            null,
            emptySet(),
            emptyList(),
            emptyList(),
            listOf(method),
            emptyList()
        )
    }

    private fun patchMethod(method: Method, mutable: MutableMethodImplementation): ImmutableMethod =
        ImmutableMethod(
            method.definingClass,
            method.name,
            method.parameters,
            method.returnType,
            method.accessFlags,
            method.annotations,
            emptySet<HiddenApiRestriction>(),
            ImmutableMethodImplementation(
                mutable.registerCount,
                mutable.instructions,
                mutable.tryBlocks,
                mutable.debugItems.toList()
            )
        )

    private fun copyEntry(source: ZipFile, entry: ZipEntry, output: ZipOutputStream, counter: CountingOutputStream) {
        val name = entry.name
        val nameBytes = name.toByteArray(Charsets.UTF_8)
        if (entry.method == ZipEntry.STORED) {
            val crc = CRC32()
            var size = 0L
            source.getInputStream(entry).use { input ->
                val buffer = ByteArray(65536)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    crc.update(buffer, 0, read)
                    size += read
                }
            }
            val stored = ZipEntry(name).apply {
                method = ZipEntry.STORED
                this.size = size
                compressedSize = size
                this.crc = crc.value
                extra = alignmentPad(counter.count + 30L + nameBytes.size, 4)
            }
            output.putNextEntry(stored)
            source.getInputStream(entry).use { input -> input.copyTo(output) }
            output.closeEntry()
        } else {
            val deflated = ZipEntry(name)
            output.putNextEntry(deflated)
            source.getInputStream(entry).use { input -> input.copyTo(output) }
            output.closeEntry()
        }
    }

    private fun alignmentPad(base: Long, alignment: Int): ByteArray {
        val remainder = (base % alignment).toInt()
        var pad = (alignment - remainder) % alignment
        if (pad in 1..3) pad += alignment
        if (pad == 0) return ByteArray(0)
        val bytes = ByteArray(pad)
        if (pad >= 4) {
            bytes[0] = 0x35
            bytes[1] = 0xD9.toByte()
            bytes[2] = ((pad - 4) and 0xFF).toByte()
            bytes[3] = (((pad - 4) shr 8) and 0xFF).toByte()
        }
        return bytes
    }
}

private class CountingOutputStream(output: OutputStream) : OutputStream() {

    private val target = output

    var count = 0L
        private set

    override fun write(value: Int) {
        target.write(value)
        count++
    }

    override fun write(buffer: ByteArray) {
        target.write(buffer)
        count += buffer.size
    }

    override fun write(buffer: ByteArray, offset: Int, length: Int) {
        target.write(buffer, offset, length)
        count += length
    }

    override fun flush() {
        target.flush()
    }

    override fun close() {
        target.close()
    }
}


