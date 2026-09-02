package com.voideditor.patch

import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val ChunkXml = 0x0003
private const val ChunkStringPool = 0x0001
private const val ChunkStartElement = 0x0102
private const val ChunkEndElement = 0x0103
private const val Utf8Flag = 0x100
private const val ValueTypeString = 0x03
private const val ActionMain = "android.intent.action.MAIN"
private const val CategoryLauncher = "android.intent.category.LAUNCHER"

class AxmlException(message: String) : Exception(message)

class AxmlElement(
    val name: String,
    val attributes: Map<String, String>,
    val parent: AxmlElement?
) {
    internal val children = mutableListOf<AxmlElement>()

    private fun walk(block: (AxmlElement) -> Unit) {
        block(this)
        for (child in children) child.walk(block)
    }

    fun all(): List<AxmlElement> {
        val out = mutableListOf<AxmlElement>()
        walk { out.add(it) }
        return out
    }
}

object AxmlParser {

    fun parse(bytes: ByteArray): Pair<AxmlElement, String?> {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(0)
        val docType = buffer.short.toInt() and 0xFFFF
        if (docType != ChunkXml) throw AxmlException("not a binary xml document")
        buffer.position(4)
        val docSize = buffer.int
        val pool = mutableListOf<String>()
        val stack = ArrayDeque<AxmlElement>()
        var root: AxmlElement? = null
        var offset = 8
        while (offset + 8 <= docSize && offset < bytes.size) {
            buffer.position(offset)
            val type = buffer.short.toInt() and 0xFFFF
            buffer.short
            val size = buffer.int
            if (size <= 0) break
            when (type) {
                ChunkStringPool -> pool.addAll(readStringPool(buffer, offset))
                ChunkStartElement -> {
                    buffer.position(offset + 16)
                    buffer.int
                    val nameIndex = buffer.int
                    buffer.short
                    buffer.short
                    val attributeCount = buffer.short.toInt() and 0xFFFF
                    buffer.short
                    buffer.short
                    buffer.short
                    val attributes = LinkedHashMap<String, String>()
                    var attrOffset = offset + 36
                    for (index in 0 until attributeCount) {
                        buffer.position(attrOffset)
                        buffer.int
                        val attrNameIndex = buffer.int
                        val rawIndex = buffer.int
                        buffer.position(attrOffset + 15)
                        val dataType = buffer.get().toInt() and 0xFF
                        buffer.position(attrOffset + 16)
                        val data = buffer.int
                        val name = pool.getOrNull(attrNameIndex)
                        val value = if (dataType == ValueTypeString) pool.getOrNull(data) else pool.getOrNull(rawIndex)
                        if (name != null && value != null) attributes[name] = value
                        attrOffset += 20
                    }
                    val element = AxmlElement(pool.getOrElse(nameIndex) { "" }, attributes, stack.lastOrNull())
                    stack.lastOrNull()?.children?.add(element)
                    if (root == null) root = element
                    stack.addLast(element)
                }
                ChunkEndElement -> {
                    if (stack.isNotEmpty()) stack.removeLast()
                }
            }
            offset += size
        }
        val resolved = root ?: throw AxmlException("empty xml document")
        return resolved to resolved.attributes["package"]
    }

    fun launcherActivity(bytes: ByteArray): String {
        val (root, packageName) = parse(bytes)
        val candidates = root.all().filter { it.name == "activity" || it.name == "activity-alias" }
        val launchable = candidates.filter { hasLauncherFilter(it) }
        val plain = launchable.firstOrNull { it.name == "activity" }
        val alias = launchable.firstOrNull { it.name == "activity-alias" }
        val name = plain?.attributes?.get("name")
            ?: alias?.attributes?.get("targetActivity")
            ?: throw AxmlException("launcher activity not found in manifest")
        return resolveClassName(name, packageName)
    }

    private fun hasLauncherFilter(element: AxmlElement): Boolean {
        var action = false
        var category = false
        for (node in element.all()) {
            if (node.name == "action" && node.attributes["name"] == ActionMain) action = true
            if (node.name == "category" && node.attributes["name"] == CategoryLauncher) category = true
        }
        return action && category
    }

    private fun resolveClassName(name: String, packageName: String?): String = when {
        name.startsWith(".") -> (packageName ?: throw AxmlException("package attribute missing in manifest")) + name
        name.contains(".") -> name
        else -> (packageName ?: throw AxmlException("package attribute missing in manifest")) + "." + name
    }

    private fun readStringPool(buffer: ByteBuffer, chunkOffset: Int): List<String> {
        buffer.position(chunkOffset + 8)
        val stringCount = buffer.int
        buffer.int
        val flags = buffer.int
        val stringsStart = buffer.int
        buffer.int
        val offsets = IntArray(stringCount) { index ->
            buffer.position(chunkOffset + 28 + index * 4)
            buffer.int
        }
        val utf8 = (flags and Utf8Flag) != 0
        val strings = mutableListOf<String>()
        for (relative in offsets) {
            buffer.position(chunkOffset + stringsStart + relative)
            strings.add(if (utf8) readUtf8String(buffer) else readUtf16String(buffer))
        }
        return strings
    }

    private fun readUtf8String(buffer: ByteBuffer): String {
        readVarint(buffer)
        val byteLength = readVarint(buffer)
        val bytes = ByteArray(byteLength)
        buffer.get(bytes)
        val text = String(bytes, Charsets.UTF_8)
        buffer.get()
        return text
    }

    private fun readUtf16String(buffer: ByteBuffer): String {
        val length = readUtf16Length(buffer)
        val builder = StringBuilder(length)
        for (index in 0 until length) builder.append(buffer.char)
        buffer.short
        return builder.toString()
    }

    private fun readUtf16Length(buffer: ByteBuffer): Int {
        val first = buffer.short.toInt() and 0xFFFF
        if (first and 0x8000 == 0) return first
        return ((first and 0x7FFF) shl 16) or (buffer.short.toInt() and 0xFFFF)
    }

    private fun readVarint(buffer: ByteBuffer): Int {
        val first = buffer.get().toInt() and 0xFF
        if (first and 0x80 == 0) return first
        return ((first and 0x7F) shl 8) or (buffer.get().toInt() and 0xFF)
    }
}
