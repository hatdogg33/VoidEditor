package com.voideditor.lsp

import android.content.Context
import com.voideditor.build.ToolchainKind
import com.voideditor.build.ToolchainPaths
import com.voideditor.proot.ProotConfig
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class ClangdConnection(
    private val context: Context,
    private val projectDir: File
) : StreamConnectionProvider {

    @Volatile
    private var process: Process? = null

    override fun start() {
        if (process?.isAlive == true) return
        prepareMountPoint()
        val guestPath = projectDir.absolutePath
        val command = listOf(ToolchainPaths.guestClangd()) + ClangdFlags
        val args = ProotConfig.rawArgs(
            context = context,
            command = command,
            guestCwd = guestPath,
            binds = listOf("$guestPath:$guestPath"),
            extraPath = listOf(ToolchainPaths.guestNdkBin(), ToolchainPaths.guestCMakeBin()),
            extraEnv = listOf("ANDROID_NDK_ROOT=${ToolchainPaths.guestDir(ToolchainKind.Ndk)}")
        )
        val builder = ProcessBuilder(args)
        builder.redirectErrorStream(false)
        builder.environment().putAll(ProotConfig.prootEnvMap(context))
        process = builder.start()
    }

    private fun prepareMountPoint() {
        val rootfs = ProotConfig.rootfsDir(context)
        val relative = projectDir.absolutePath.trimStart('/')
        runCatching { File(rootfs, relative).mkdirs() }
    }

    override val inputStream: InputStream
        get() = process?.inputStream
            ?: throw IllegalStateException("LSP connection not started. Call start() first.")

    override val outputStream: OutputStream
        get() = process?.outputStream
            ?: throw IllegalStateException("LSP connection not started. Call start() first.")

    override val isClosed: Boolean
        get() = process?.isAlive != true

    override fun close() {
        runCatching { process?.outputStream?.close() }
        runCatching { process?.destroy() }
        process = null
    }

    companion object {
        private val ClangdFlags = listOf(
            "--background-index",
            "--background-index-priority=low",
            "--pch-storage=disk",
            "--malloc-trim",
            "-j=2",
            "--clang-tidy",
            "--all-scopes-completion",
            "--completion-style=detailed",
            "--function-arg-placeholders",
            "--header-insertion=iwyu",
            "--header-insertion-decorators",
            "--include-cleaner-stdlib",
            "--rename-file-limit=50",
            "--limit-results=100",
            "--limit-references=1000",
            "--ranking-model=decision_forest",
            "--log=error"
        )

        fun isAvailable(context: Context): Boolean =
            ProotConfig.isInstalled(context) &&
                ToolchainPaths.isInstalled(context, ToolchainKind.Ndk) &&
                ToolchainPaths.clangdBinary(context).isFile
    }
}
