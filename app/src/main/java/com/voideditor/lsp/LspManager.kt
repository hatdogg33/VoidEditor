package com.voideditor.lsp

import android.content.Context
import com.voideditor.build.ToolchainKind
import com.voideditor.build.ToolchainPaths
import com.voideditor.data.AppSettings
import com.voideditor.data.PreferenceSettings
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.languageServerDefinition
import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.lsp.editor.LspProject
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.getComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.CodeActionOptions
import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.DiagnosticRegistrationOptions
import org.eclipse.lsp4j.DocumentLinkOptions
import org.eclipse.lsp4j.RenameOptions
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.SignatureHelpOptions
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

class LspManager(private val context: Context, private val projectDir: File) {

    private var project: LspProject? = null
    private val attached = mutableMapOf<String, LspEditor>()

    fun supports(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return CppExtensions.any { lower.endsWith(it) }
    }

    fun tooLarge(file: File): Boolean {
        val limitKb = AppSettings.int(PreferenceSettings.LspMaxFileKb, 2048)
        return file.length() > limitKb * 1024L
    }

    private fun expectedCapabilities(): ServerCapabilities = ServerCapabilities().apply {
        completionProvider = CompletionOptions(true, CompletionTriggers)
        signatureHelpProvider = SignatureHelpOptions(SignatureTriggers, SignatureRetriggers)
        hoverProvider = Either.forLeft(true)
        definitionProvider = Either.forLeft(true)
        typeDefinitionProvider = Either.forLeft(true)
        implementationProvider = Either.forLeft(true)
        referencesProvider = Either.forLeft(true)
        documentHighlightProvider = Either.forLeft(true)
        documentSymbolProvider = Either.forLeft(true)
        workspaceSymbolProvider = Either.forLeft(true)
        documentFormattingProvider = Either.forLeft(true)
        documentRangeFormattingProvider = Either.forLeft(true)
        codeActionProvider = Either.forRight(CodeActionOptions())
        renameProvider = Either.forRight(RenameOptions(true))
        inlayHintProvider = Either.forLeft(true)
        colorProvider = Either.forLeft(true)
        foldingRangeProvider = Either.forLeft(true)
        selectionRangeProvider = Either.forLeft(true)
        documentLinkProvider = DocumentLinkOptions(false)
        callHierarchyProvider = Either.forLeft(true)
        typeHierarchyProvider = Either.forLeft(true)
        diagnosticProvider = DiagnosticRegistrationOptions(true, true)
    }

    private fun ensureProject(): LspProject {
        project?.let { return it }
        val created = LspProject(projectDir.absolutePath)
        val capabilities = expectedCapabilities()
        for (ext in CppExtensions) {
            created.addServerDefinition(
                languageServerDefinition {
                    name("clangd")
                    ext(ext.removePrefix("."))
                    connection {
                        provider { ClangdConnection(context, projectDir) }
                    }
                    expectedCapabilities(capabilities)
                }
            )
        }
        project = created
        return created
    }

    suspend fun attach(
        editor: CodeEditor,
        file: File,
        onStatus: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        if (!ClangdConnection.isAvailable(context)) {
            onStatus("clangd not installed, reinstall the NDK")
            return@withContext false
        }
        if (!supports(file.name)) return@withContext false
        if (tooLarge(file)) {
            onStatus("${file.name} is too large for LSP")
            return@withContext false
        }
        writeFallbackFlags()
        val path = file.absolutePath
        val lspProject = ensureProject()
        val lspEditor = lspProject.getOrCreateEditor(path)
        withContext(Dispatchers.Main) {
            lspEditor.wrapperLanguage =
                com.voideditor.editor.EditorLanguageResolver.resolve(file.name)
            lspEditor.editor = editor
            lspEditor.isEnableHover = AppSettings.bool(PreferenceSettings.LspHover, true)
            lspEditor.isEnableSignatureHelp =
                AppSettings.bool(PreferenceSettings.LspSignatureHelp, true)
            lspEditor.isEnableInlayHint = AppSettings.bool(PreferenceSettings.LspInlayHint, true)
            lspEditor.completionTriggers.addAll(CompletionTriggers)
            lspEditor.signatureHelpTriggers.addAll(SignatureTriggers)
            lspEditor.signatureHelpReTriggers.addAll(SignatureRetriggers)
            editor.getComponent<EditorAutoCompletion>().setEnabledAnimation(true)
        }
        val connected = runCatching { lspEditor.connect(false) }.getOrDefault(false)
        if (connected) {
            attached[path] = lspEditor
            withContext(Dispatchers.Main) { LspPopupSizing.apply(lspEditor, editor) }
            onStatus("clangd connected")
        } else {
            onStatus("clangd failed to start")
            runCatching { lspEditor.dispose() }
        }
        connected
    }

    suspend fun detach(path: String) = withContext(Dispatchers.IO) {
        val lspEditor = attached.remove(path) ?: return@withContext
        runCatching { lspEditor.dispose() }
    }

    suspend fun notifySaved(path: String) = withContext(Dispatchers.IO) {
        val lspEditor = attached[path] ?: return@withContext
        runCatching { lspEditor.saveDocument() }
    }

    fun attachedEditor(path: String): LspEditor? = attached[path]

    fun isAttached(path: String): Boolean = attached[path]?.isConnected == true

    suspend fun shutdown() = withContext(Dispatchers.IO) {
        attached.clear()
        val current = project ?: return@withContext
        project = null
        runCatching { current.closeAllEditors() }
        runCatching { current.dispose() }
    }

    private fun writeFallbackFlags() {
        if (File(projectDir, "compile_flags.txt").isFile) return
        if (File(projectDir, "build/compile_commands.json").isFile) return
        if (File(projectDir, "compile_commands.json").isFile) return
        val ndk = ToolchainPaths.guestDir(ToolchainKind.Ndk)
        val sysroot = "$ndk/toolchains/llvm/prebuilt/linux-arm64/sysroot"
        runCatching {
            File(projectDir, "compile_flags.txt").writeText(
                listOf(
                    "-xc++",
                    "-std=c++17",
                    "--target=aarch64-none-linux-android24",
                    "--sysroot=$sysroot",
                    "-I.",
                    "-Iinclude",
                    "-DANDROID",
                    "-D__ANDROID_API__=24"
                ).joinToString("\n") + "\n"
            )
        }
    }

    companion object {
        private val CppExtensions =
            listOf(".cpp", ".cc", ".cxx", ".c", ".h", ".hpp", ".hh", ".hxx")

        private val CompletionTriggers = listOf(".", ">", ":", "<", "\"", "/", "*", "&")

        private val SignatureTriggers = listOf("(", ",", "<")

        private val SignatureRetriggers = listOf(")", ">")
    }
}
