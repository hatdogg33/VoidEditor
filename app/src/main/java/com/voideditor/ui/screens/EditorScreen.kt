package com.voideditor.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R
import com.voideditor.build.BuildEvent
import com.voideditor.build.BuildRequest
import com.voideditor.build.BuildRunner
import com.voideditor.build.RunConfigurations
import com.voideditor.data.AppSettings
import com.voideditor.data.PreferenceSettings
import com.voideditor.lsp.LocationEntry
import com.voideditor.lsp.LspManager
import com.voideditor.lsp.LspNavigation
import com.voideditor.lsp.SymbolEntry
import com.voideditor.build.ToolchainKind
import com.voideditor.build.ToolchainPaths
import com.voideditor.data.FileOps
import com.voideditor.data.ProjectCreator
import com.voideditor.editor.EditorConfigurator
import com.voideditor.editor.EditorThemeName
import com.voideditor.editor.EditorSearch
import com.voideditor.editor.EditorLanguageResolver
import com.voideditor.editor.EditorPane
import com.voideditor.ui.build.BuildConsole
import com.voideditor.ui.build.RunMenu
import com.voideditor.ui.build.ConsoleLine
import com.voideditor.ui.build.ConsoleLineKind
import com.voideditor.ui.build.ToolchainInstallDialog
import com.voideditor.ui.dialogs.ConfirmDialog
import com.voideditor.ui.dialogs.NameInputDialog
import com.voideditor.ui.dialogs.UnsavedChangesDialog
import com.voideditor.ui.editor.FindReplaceBar
import com.voideditor.ui.editor.FindState
import com.voideditor.ui.editor.SymbolBar
import com.voideditor.ui.editor.EditorToolsMenu
import com.voideditor.ui.lsp.LocationListPanel
import com.voideditor.ui.lsp.SymbolListPanel
import com.voideditor.ui.explorer.ExplorerDrawerContent
import com.voideditor.ui.explorer.ExplorerState
import com.voideditor.ui.explorer.NodeAction
import com.voideditor.ui.explorer.NodeActionSheet
import com.voideditor.ui.icons.XedIcons
import com.voideditor.ui.theme.VoidEditorPalette
import com.voideditor.ui.theme.SpringGreen
import com.voideditor.patch.ApkPatcher
import com.voideditor.patch.PatchLibVariant
import com.voideditor.patch.PatchPhase
import com.voideditor.ui.inject.InjectDialog
import com.voideditor.ui.inject.PatchConsoleDialog
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentListener
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voideditor.viewmodel.EditorViewModel
import com.voideditor.ui.search.FileSearchBar
import com.voideditor.ui.search.FileSearchResults
import com.voideditor.ui.git.GitStatusPanel
import com.voideditor.ui.git.GitLogPanel
import com.voideditor.ui.git.GitCommitDialog

private val ErrorTint = Color(0xFFEF6767)
private val DisabledTint = Color(0xFF3F5F58)
private val SidebarBackground = Color(0xFF0A222B)
private val TitleBarBackground = Color(0xFF0E2A33)
private val EditorBackground = Color(0xFF0A2129)
private val TabBarBackground = Color(0xFF0A222B)
private val TabActiveForeground = Color(0xFFF2FFFA)
private val TabInactiveForeground = Color(0xFF7FA898)
private val DirtyDot = SpringGreen
private val AccentGreen = SpringGreen
private val TabMenuBackground = Color(0xFF0A222B)
private val TabMenuText = Color(0xFFDDF5EA)
private val TabMenuIcon = Color(0xFF6E9184)
private val LspStatusColor = Color(0xFF6FD9AE)
private val LspStatusBackground = Color(0xFF08202A)
private val DrawerWidth = 220.dp
private val ConsoleHeight = 260.dp
private val DrawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
private val HamburgerBrush = Brush.linearGradient(
    colors = listOf(SpringGreen, SpringGreen.copy(alpha = 0.55f))
)

private data class TabItem(
    val path: String,
    val name: String,
    val dirty: Boolean,
    val text: String
)

private sealed interface ExplorerDialog {
    data class Menu(val target: File) : ExplorerDialog
    data class Input(val initial: String, val parent: File, val kind: NodeAction) : ExplorerDialog
    data class Delete(val target: File) : ExplorerDialog
    data class UnsavedClose(val path: String, val name: String) : ExplorerDialog
}

private sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data object Saved : SaveState
    data object Failed : SaveState
}

private class DirtyMarker(private val onDirty: () -> Unit) : ContentListener {
    var enabled = true
    override fun beforeReplace(content: Content) {}
    override fun afterInsert(content: Content, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, insertedContent: CharSequence) {
        if (enabled) onDirty()
    }
    override fun afterDelete(content: Content, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, deletedContent: CharSequence) {
        if (enabled) onDirty()
    }
}

@Composable
fun EditorScreen(
    projectPath: String,
    onOpenSettings: () -> Unit,
    onOpenTerminal: () -> Unit,
    viewModel: EditorViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerAnim = remember { Animatable(0f) }
    val density = LocalDensity.current
    val drawerWidthPx = remember(density) { with(density) { DrawerWidth.toPx() } }

    val projectDir = remember { File(projectPath) }
    val explorer = remember { ExplorerState(projectDir) }

    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val gitStatus by viewModel.gitStatus.collectAsState()
    val gitLog by viewModel.gitLog.collectAsState()

    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchCaseSensitive by remember { mutableStateOf(false) }
    var gitVisible by remember { mutableStateOf(false) }
    var showGitCommit by remember { mutableStateOf(false) }

    val tabs = remember {
        val initial = File(projectDir, ProjectCreator.MAIN_SOURCE_NAME)
        if (initial.exists()) {
            mutableStateListOf(TabItem(initial.absolutePath, initial.name, false, runCatching { initial.readText() }.getOrDefault("")))
        } else {
            mutableStateListOf<TabItem>()
        }
    }
    var activePath by remember { mutableStateOf(tabs.firstOrNull()?.path) }
    var editorRef by remember { mutableStateOf<CodeEditor?>(null) }
    var saveState by remember { mutableStateOf<SaveState>(SaveState.Idle) }
    var dialog by remember { mutableStateOf<ExplorerDialog?>(null) }

    val context = LocalContext.current
    val lspManager = remember(projectDir) { LspManager(context, projectDir) }
    val lspScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    var lspStatus by remember { mutableStateOf<String?>(null) }
    val buildRunner = remember { BuildRunner(context) }
    val runConfigurations = remember(projectDir) {
        RunConfigurations(context, projectDir, buildRunner)
    }
    var menuExpanded by remember { mutableStateOf(false) }
    var tabMenuFor by remember { mutableStateOf<String?>(null) }
    var pendingCloseQueue by remember { mutableStateOf<List<String>>(emptyList()) }
    var consoleMaximized by remember { mutableStateOf(false) }
    var toolsExpanded by remember { mutableStateOf(false) }
    var findState by remember { mutableStateOf(FindState()) }
    var findVisible by remember { mutableStateOf(false) }
    val findFocusRequester = remember { FocusRequester() }
    var symbolPanel by remember { mutableStateOf<List<SymbolEntry>?>(null) }
    var locationPanel by remember { mutableStateOf<Pair<String, List<LocationEntry>>?>(null) }
    val consoleLines = remember { mutableStateListOf<ConsoleLine>() }
    var consoleVisible by remember { mutableStateOf(false) }
    var building by remember { mutableStateOf(false) }
    var showToolchainDialog by remember { mutableStateOf(false) }
    var showInjectDialog by remember { mutableStateOf(false) }
    var showPatchConsole by remember { mutableStateOf(false) }
    var patchPhase by remember { mutableStateOf<PatchPhase>(PatchPhase.Idle) }
    var patchedOutput by remember { mutableStateOf<File?>(null) }
    val patchConsoleLines = remember { mutableStateListOf<String>() }
    val patchCancelled = remember { AtomicBoolean(false) }

    val installLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val output = patchedOutput
        if (result.resultCode == Activity.RESULT_OK && output != null) {
            output.delete()
            patchConsoleLines.add("patched apk removed from cache")
        }
    }

    fun startPatch(apkPath: String, variant: PatchLibVariant) {
        showInjectDialog = false
        patchConsoleLines.clear()
        patchedOutput = null
        patchCancelled.set(false)
        patchPhase = PatchPhase.Running
        showPatchConsole = true
        scope.launch {
            val output = ApkPatcher.patch(context, File(apkPath), variant, patchCancelled, { line -> patchConsoleLines.add(line) }, { phase -> patchPhase = phase })
            if (output != null) patchedOutput = output
        }
    }

    fun installPatched() {
        val output = patchedOutput ?: return
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", output)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        installLauncher.launch(intent)
    }
    var historyRevision by remember { mutableStateOf(0) }

    val dirtyMarker = remember {
        DirtyMarker {
            historyRevision++
            val current = activePath
            if (current != null) {
                val index = tabs.indexOfFirst { it.path == current }
                if (index >= 0 && !tabs[index].dirty) {
                    tabs[index] = tabs[index].copy(dirty = true)
                }
            }
        }
    }

    fun detachListener(editor: CodeEditor) {
        runCatching { editor.text.removeContentListener(dirtyMarker) }
    }

    fun loadTabIntoEditor(editor: CodeEditor, tab: TabItem) {
        dirtyMarker.enabled = false
        detachListener(editor)
        editor.setText(tab.text)
        editor.setEditorLanguage(EditorLanguageResolver.resolve(tab.name))
        editor.text.addContentListener(dirtyMarker)
        dirtyMarker.enabled = true
        if (AppSettings.bool(PreferenceSettings.LspEnabled, false)) {
            scope.launch {
                lspManager.attach(editor, File(tab.path)) { lspStatus = it }
            }
        }
    }

    fun captureActiveText() {
        val editor = editorRef ?: return
        val current = activePath ?: return
        val index = tabs.indexOfFirst { it.path == current }
        if (index >= 0) {
            tabs[index] = tabs[index].copy(text = editor.text.toString())
        }
    }

    fun openFile(file: File) {
        val path = file.absolutePath
        if (tabs.none { it.path == path }) {
            tabs.add(TabItem(path, file.name, false, runCatching { file.readText() }.getOrDefault("")))
        }
        activePath = path
        val tab = tabs.first { it.path == path }
        editorRef?.let { loadTabIntoEditor(it, tab) }
    }

    fun switchTab(path: String) {
        if (path == activePath) return
        captureActiveText()
        activePath = path
        val tab = tabs.firstOrNull { it.path == path } ?: return
        editorRef?.let { loadTabIntoEditor(it, tab) }
    }

    fun removeTab(path: String) {
        val index = tabs.indexOfFirst { it.path == path }
        if (index < 0) return
        scope.launch { lspManager.detach(path) }
        if (path == activePath) {
            val next = tabs.getOrNull(index - 1)?.path ?: tabs.getOrNull(index + 1)?.path
            tabs.removeAt(index)
            activePath = next
            val nextTab = next?.let { p -> tabs.firstOrNull { it.path == p } }
            if (nextTab != null) {
                editorRef?.let { loadTabIntoEditor(it, nextTab) }
            }
        } else {
            tabs.removeAt(index)
        }
    }

    // Close tabs one by one; dirty files queue the existing per-file
    // "Save changes?" dialog, then the batch resumes automatically.
    fun closeTabs(paths: List<String>) {
        var queue = paths
        while (queue.isNotEmpty()) {
            val path = queue.first()
            val tab = tabs.firstOrNull { it.path == path }
            queue = queue.drop(1)
            if (tab == null) continue
            if (tab.dirty) {
                pendingCloseQueue = queue
                dialog = ExplorerDialog.UnsavedClose(path, tab.name)
                return
            }
            removeTab(path)
        }
    }

    fun resumePendingClose() {
        val pending = pendingCloseQueue
        if (pending.isEmpty()) return
        pendingCloseQueue = emptyList()
        closeTabs(pending)
    }

    fun closeOtherTabs(keepPath: String) =
        closeTabs(tabs.filter { it.path != keepPath }.map { it.path })

    fun closeAllTabs() = closeTabs(tabs.map { it.path })

    fun saveActive() {
        val editor = editorRef ?: return
        val current = activePath ?: return
        if (saveState is SaveState.Saving) return
        saveState = SaveState.Saving
        scope.launch {
            val text = editor.text.toString()
            val saved = withContext(Dispatchers.IO) {
                runCatching { File(current).writeText(text) }.isSuccess
            }
            val index = tabs.indexOfFirst { it.path == current }
            if (saved && index >= 0) {
                tabs[index] = tabs[index].copy(dirty = false, text = text)
            }
            if (saved) lspManager.notifySaved(current)
            saveState = if (saved) SaveState.Saved else SaveState.Failed
        }
    }

    suspend fun saveAllDirty(): Int {
        captureActiveText()
        val pending = tabs.filter { it.dirty }
        if (pending.isEmpty()) return 0
        val failures = withContext(Dispatchers.IO) {
            pending.count { tab ->
                runCatching { File(tab.path).writeText(tab.text) }.isFailure
            }
        }
        for (tab in pending) {
            val index = tabs.indexOfFirst { it.path == tab.path }
            if (index >= 0) tabs[index] = tabs[index].copy(dirty = false)
        }
        if (failures == 0) saveState = SaveState.Saved
        return pending.size
    }

    fun appendConsole(text: String, kind: ConsoleLineKind = ConsoleLineKind.Normal) {
        consoleLines.add(ConsoleLine(text, kind))
        val limit = AppSettings.int(PreferenceSettings.ConsoleMaxLines, 2000)
        if (consoleLines.size > limit) consoleLines.removeRange(0, consoleLines.size - limit)
    }

    fun execute(requests: List<BuildRequest>) {
        if (building || requests.isEmpty()) return
        if (AppSettings.bool(PreferenceSettings.ConsoleAutoOpen, true)) consoleVisible = true
        building = true
        scope.launch {
            val savedCount =
                if (AppSettings.bool(PreferenceSettings.AutoSaveOnBuild, true)) saveAllDirty() else 0
            if (savedCount > 0) {
                appendConsole(
                    if (savedCount == 1) "> saved 1 file" else "> saved $savedCount files"
                )
            }
            for ((index, request) in requests.withIndex()) {
                if (requests.size > 1) {
                    val name = when (request) {
                        is BuildRequest.Build -> request.preset
                        is BuildRequest.CleanBuild -> request.preset
                    }
                    appendConsole("> [${index + 1}/${requests.size}] $name")
                }
                var failed = false
                buildRunner.run(projectDir, request) { event ->
                    when (event) {
                        is BuildEvent.Line -> appendConsole(event.text)
                        is BuildEvent.Finished -> {
                            if (event.exitCode == 0) {
                                appendConsole("> build succeeded", ConsoleLineKind.Success)
                            } else {
                                failed = true
                                appendConsole("> build failed with exit code ${event.exitCode}", ConsoleLineKind.Error)
                            }
                        }
                        is BuildEvent.Failed -> {
                            failed = true
                            appendConsole("> ${event.message}", ConsoleLineKind.Error)
                        }
                    }
                }
                if (failed) break
            }
            building = false
        }
    }

    fun copyProjectPath(file: File) {
        val root = projectDir.absolutePath.trimEnd('/')
        val path = file.absolutePath
        val relative = if (path.startsWith("$root/")) {
            path.removePrefix("$root/")
        } else {
            file.name
        }
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText(file.name, relative))
        lspStatus = "copied $relative"
    }

    fun runSearch(state: FindState) {
        val editor = editorRef ?: return
        if (state.query.isEmpty()) {
            EditorSearch.stop(editor)
            findState = state.copy(current = 0, total = 0)
            return
        }
        EditorSearch.search(
            editor = editor,
            query = state.query,
            caseSensitive = state.caseSensitive,
            wholeWord = state.wholeWord,
            regex = state.regex
        )
        findState = state.copy(
            total = EditorSearch.matchCount(editor),
            current = EditorSearch.currentIndex(editor)
        )
    }

    fun syncSearchCounter() {
        val editor = editorRef ?: return
        findState = findState.copy(
            total = EditorSearch.matchCount(editor),
            current = EditorSearch.currentIndex(editor)
        )
    }

    fun closeFind() {
        editorRef?.let { EditorSearch.stop(it) }
        findVisible = false
        findState = FindState()
    }

    fun formatCode() {
        val editor = editorRef ?: return
        if (!EditorSearch.format(editor)) {
            lspStatus = "formatter not available"
        }
    }

    fun jumpTo(line: Int, column: Int) {
        val editor = editorRef ?: return
        val safeLine = line.coerceIn(0, (editor.text.lineCount - 1).coerceAtLeast(0))
        val safeColumn = column.coerceIn(0, editor.text.getColumnCount(safeLine))
        editor.setSelection(safeLine, safeColumn)
    }

    fun openLocation(entry: LocationEntry) {
        if (entry.file.absolutePath == activePath) {
            jumpTo(entry.line, entry.column)
            return
        }
        openFile(entry.file)
        scope.launch {
            withContext(Dispatchers.Main) { jumpTo(entry.line, entry.column) }
        }
    }

    fun currentLspEditor(): io.github.rosemoe.sora.lsp.editor.LspEditor? {
        val current = activePath ?: return null
        return lspManager.attachedEditor(current)
    }

    fun showSymbols() {
        val lspEditor = currentLspEditor()
        if (lspEditor == null) {
            lspStatus = "clangd is not attached to this file"
            return
        }
        scope.launch {
            val symbols = LspNavigation.documentSymbols(lspEditor)
            if (symbols.isEmpty()) {
                lspStatus = "no symbols found"
            } else {
                locationPanel = null
                symbolPanel = symbols
            }
        }
    }

    fun goToDefinition() {
        val editor = editorRef ?: return
        val lspEditor = currentLspEditor()
        if (lspEditor == null) {
            lspStatus = "clangd is not attached to this file"
            return
        }
        val position = editor.cursor.left()
        scope.launch {
            val results = LspNavigation.definition(lspEditor, position)
            when {
                results.isEmpty() -> lspStatus = "no definition found"
                results.size == 1 -> openLocation(results.first())
                else -> {
                    symbolPanel = null
                    locationPanel = "Definition" to results
                }
            }
        }
    }

    fun findReferences() {
        val editor = editorRef ?: return
        val lspEditor = currentLspEditor()
        if (lspEditor == null) {
            lspStatus = "clangd is not attached to this file"
            return
        }
        val position = editor.cursor.left()
        scope.launch {
            val results = LspNavigation.references(lspEditor, position)
            if (results.isEmpty()) {
                lspStatus = "no references found"
            } else {
                symbolPanel = null
                locationPanel = "References" to results
            }
        }
    }

    fun copyConsoleOutput() {
        if (consoleLines.isEmpty()) return
        val text = consoleLines.joinToString("\n") { it.text }
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("build output", text))
        lspStatus = "copied ${consoleLines.size} lines"
    }

    fun runBuild(clean: Boolean) {
        if (building) return
        consoleLines.clear()
        scope.launch {
            if (!runConfigurations.hasPresets()) {
                withContext(Dispatchers.IO) { runConfigurations.bootstrap() }
                appendConsole("> created ${com.voideditor.build.CmakePresets.ProjectFileName}")
            }
            val presets = runConfigurations.activePresets()
            if (presets.isEmpty()) {
                appendConsole("> no build preset found", ConsoleLineKind.Error)
                return@launch
            }
            val requests = presets.map { preset ->
                if (clean) BuildRequest.CleanBuild(preset) else BuildRequest.Build(preset)
            }
            execute(requests)
        }
    }

    fun openRunMenu() {
        val ready = ToolchainPaths.isInstalled(context, ToolchainKind.CMake) &&
            ToolchainPaths.isInstalled(context, ToolchainKind.Ndk)
        if (!ready) {
            showToolchainDialog = true
            return
        }
        menuExpanded = true
    }

    fun undo() {
        val editor = editorRef ?: return
        if (editor.canUndo()) {
            editor.undo()
            historyRevision++
        }
    }

    fun redo() {
        val editor = editorRef ?: return
        if (editor.canRedo()) {
            editor.redo()
            historyRevision++
        }
    }

    fun openDrawer() {
        scope.launch { drawerAnim.animateTo(1f, spring(stiffness = 320f, dampingRatio = 0.8f)) }
    }

    fun closeDrawer() {
        scope.launch { drawerAnim.animateTo(0f, spring(stiffness = 320f, dampingRatio = 0.8f)) }
    }

    fun settleDrawer() {
        val target = if (drawerAnim.value > 0.5f) 1f else 0f
        scope.launch { drawerAnim.animateTo(target, spring(stiffness = 320f, dampingRatio = 0.8f)) }
    }

    val drawerProgress = drawerAnim.value

    BackHandler(enabled = drawerProgress > 0f) {
        closeDrawer()
    }

    LaunchedEffect(findVisible) {
        if (!findVisible) return@LaunchedEffect
        editorRef?.let { editor ->
            editor.clearFocus()
            val manager = context.getSystemService(InputMethodManager::class.java)
            manager?.hideSoftInputFromWindow(editor.windowToken, 0)
        }
        withFrameNanos { }
        runCatching { findFocusRequester.requestFocus() }
    }

    BackHandler(enabled = drawerProgress == 0f && findVisible) {
        closeFind()
    }

    BackHandler(enabled = drawerProgress == 0f && !findVisible && symbolPanel != null) {
        symbolPanel = null
    }

    BackHandler(enabled = drawerProgress == 0f && !findVisible && locationPanel != null) {
        locationPanel = null
    }

    BackHandler(enabled = drawerProgress == 0f && consoleVisible && consoleMaximized) {
        consoleMaximized = false
    }

    BackHandler(enabled = drawerProgress == 0f && consoleVisible && !consoleMaximized) {
        consoleVisible = false
    }

    BackHandler(enabled = drawerProgress == 0f && searchVisible) {
        searchVisible = false
        searchQuery = ""
    }

    BackHandler(enabled = drawerProgress == 0f && gitVisible) {
        gitVisible = false
    }

    LaunchedEffect(editorRef) {
        editorRef?.let { EditorConfigurator.apply(it) }
    }

    // Re-apply the selected editor theme when returning from Settings
    val themeName = AppSettings.string(EditorThemeName.PREF_KEY, EditorThemeName.DEFAULT.name)
    LaunchedEffect(themeName) {
        editorRef?.let { ref ->
            EditorThemeName.fromName(themeName).apply(ref)
        }
    }

    // Initialize ViewModel and start auto-save
    LaunchedEffect(projectDir) {
        viewModel.refreshGitStatus(projectDir)
        viewModel.startAutoSave {
            val modified = mutableMapOf<String, String>()
            tabs.filter { it.dirty }.forEach { tab ->
                modified[tab.path] = tab.text
            }
            modified
        }
    }

    // Add file to recent files when opened
    LaunchedEffect(activePath) {
        activePath?.let { viewModel.addRecentFile(it) }
    }

    DisposableEffect(Unit) {
        onDispose {
            buildRunner.stop()
            lspScope.launch { lspManager.shutdown() }
            viewModel.stopAutoSave()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EditorBackground)
                .systemBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TitleBarBackground)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { openDrawer() }) {
                    HamburgerIcon(modifier = Modifier.size(22.dp))
                }
                Text(
                    text = projectDir.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TabActiveForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val canUndo = remember(historyRevision, editorRef) {
                    editorRef?.canUndo() == true
                }
                val canRedo = remember(historyRevision, editorRef) {
                    editorRef?.canRedo() == true
                }

                IconButton(onClick = { undo() }, enabled = canUndo) {
                    Icon(
                        painter = painterResource(R.drawable.undo),
                        contentDescription = stringResource(R.string.undo),
                        tint = if (canUndo) Color(0xFFE4F5EC) else DisabledTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = { redo() }, enabled = canRedo) {
                    Icon(
                        painter = painterResource(R.drawable.redo),
                        contentDescription = stringResource(R.string.redo),
                        tint = if (canRedo) Color(0xFFE4F5EC) else DisabledTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = {
                    findVisible = !findVisible
                    if (!findVisible) closeFind()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "Find",
                        tint = if (findVisible) AccentGreen else Color(0xFFE4F5EC),
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(onClick = {
                    searchVisible = !searchVisible
                    if (!searchVisible) searchQuery = ""
                }) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_search),
                        contentDescription = "Project Search",
                        tint = if (searchVisible) AccentGreen else Color(0xFFE4F5EC),
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(onClick = {
                    gitVisible = !gitVisible
                    if (gitVisible) viewModel.refreshGitStatus(projectDir)
                }) {
                    Text(
                        "Git",
                        color = if (gitVisible) AccentGreen else Color(0xFFE4F5EC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box {
                    IconButton(onClick = { toolsExpanded = true }) {
                        Icon(
                            painter = painterResource(R.drawable.command_palette),
                            contentDescription = "Tools",
                            tint = Color(0xFFE4F5EC),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    EditorToolsMenu(
                        expanded = toolsExpanded,
                        onDismiss = { toolsExpanded = false },
                        onFormat = {
                            toolsExpanded = false
                            formatCode()
                        },
                        onSymbols = {
                            toolsExpanded = false
                            showSymbols()
                        },
                        onDefinition = {
                            toolsExpanded = false
                            goToDefinition()
                        },
                        onReferences = {
                            toolsExpanded = false
                            findReferences()
                        }
                    )
                }

                IconButton(onClick = { saveActive() }) {
                    Icon(
                        painter = painterResource(if (saveState is SaveState.Saved) R.drawable.select else R.drawable.save),
                        contentDescription = stringResource(R.string.save),
                        tint = when (saveState) {
                            SaveState.Saved -> VoidEditorPalette.mint
                            SaveState.Failed -> ErrorTint
                            else -> Color(0xFFE4F5EC)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(onClick = { openRunMenu() }, enabled = !building) {
                        Icon(
                            painter = painterResource(R.drawable.run),
                            contentDescription = stringResource(R.string.run_build),
                            tint = if (building) DisabledTint else AccentGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    RunMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        onBuild = {
                            menuExpanded = false
                            runBuild(false)
                        },
                        onCleanBuild = {
                            menuExpanded = false
                            runBuild(true)
                        }
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                if (tabs.isEmpty()) {
                    EmptyEditorState()
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TabBarBackground),
                            content = {
                                items(tabs, key = { it.path }) { tab ->
                                    TabChip(
                                        tab = tab,
                                        active = tab.path == activePath,
                                        menuExpanded = tabMenuFor == tab.path,
                                        onClick = {
                                            if (tab.path == activePath) {
                                                // Already on this tab → open the tab menu
                                                tabMenuFor = tab.path
                                            } else {
                                                // Clicking another tab = just switch, no menu
                                                switchTab(tab.path)
                                                tabMenuFor = null
                                            }
                                        },
                                        onDismissMenu = { tabMenuFor = null },
                                        onCloseFile = {
                                            tabMenuFor = null
                                            if (tab.dirty) {
                                                dialog = ExplorerDialog.UnsavedClose(tab.path, tab.name)
                                            } else {
                                                removeTab(tab.path)
                                            }
                                        },
                                        onCloseOthers = {
                                            tabMenuFor = null
                                            closeOtherTabs(tab.path)
                                        },
                                        onCloseAll = {
                                            tabMenuFor = null
                                            closeAllTabs()
                                        }
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            EditorPane(
                                onEditorCreated = { editor ->
                                    editorRef = editor
                                    val tab = activePath?.let { p -> tabs.firstOrNull { it.path == p } }
                                    if (tab != null) loadTabIntoEditor(editor, tab)
                                },
                                onEditorReleased = { editorRef = null }
                            )
                        }
                    }
                }
            }
            if (findVisible) {
                FindReplaceBar(
                    state = findState,
                    focusRequester = findFocusRequester,
                    onQueryChange = { value -> runSearch(findState.copy(query = value)) },
                    onReplacementChange = { value ->
                        findState = findState.copy(replacement = value)
                    },
                    onToggleOption = { option ->
                        val next = when (option) {
                            "case" -> findState.copy(caseSensitive = !findState.caseSensitive)
                            "word" -> findState.copy(
                                wholeWord = !findState.wholeWord,
                                regex = false
                            )
                            else -> findState.copy(
                                regex = !findState.regex,
                                wholeWord = false
                            )
                        }
                        runSearch(next)
                    },
                    onToggleReplace = {
                        findState = findState.copy(showReplace = !findState.showReplace)
                    },
                    onNext = {
                        editorRef?.let { EditorSearch.next(it) }
                        syncSearchCounter()
                    },
                    onPrevious = {
                        editorRef?.let { EditorSearch.previous(it) }
                        syncSearchCounter()
                    },
                    onReplace = {
                        editorRef?.let { EditorSearch.replaceCurrent(it, findState.replacement) }
                        syncSearchCounter()
                    },
                    onReplaceAll = {
                        editorRef?.let { EditorSearch.replaceAll(it, findState.replacement) }
                        syncSearchCounter()
                    },
                    onClose = { closeFind() }
                )
            }

            if (searchVisible) {
                FileSearchBar(
                    query = searchQuery,
                    onQueryChange = { query ->
                        searchQuery = query
                        viewModel.searchProject(projectDir, query, searchCaseSensitive)
                    },
                    onDismiss = {
                        searchVisible = false
                        searchQuery = ""
                    },
                    caseSensitive = searchCaseSensitive,
                    onCaseSensitiveChange = { searchCaseSensitive = it }
                )
                FileSearchResults(
                    results = searchResults,
                    isSearching = isSearching,
                    onResultClick = { path, line ->
                        openFile(File(path))
                        jumpTo(line - 1, 0)
                    }
                )
            }

            if (gitVisible) {
                gitStatus?.let { status ->
                    GitStatusPanel(
                        status = status,
                        onCommitClick = { showGitCommit = true },
                        onInitClick = {
                            viewModel.gitInit(projectDir) { success, output ->
                                lspStatus = if (success) "Git initialized" else output
                            }
                        },
                        onRefreshClick = { viewModel.refreshGitStatus(projectDir) }
                    )
                    if (gitLog.isNotEmpty()) {
                        GitLogPanel(
                            log = gitLog,
                            modifier = Modifier.height(150.dp)
                        )
                    }
                }
            }

            if (showGitCommit) {
                GitCommitDialog(
                    onCommit = { message ->
                        viewModel.gitCommit(projectDir, message) { success, output ->
                            lspStatus = if (success) "Committed!" else output
                            showGitCommit = false
                        }
                    },
                    onDismiss = { showGitCommit = false }
                )
            }

            symbolPanel?.let { symbols ->
                SymbolListPanel(
                    title = "Symbols",
                    symbols = symbols,
                    onSelect = { symbol ->
                        symbolPanel = null
                        jumpTo(symbol.line, symbol.column)
                    },
                    onClose = { symbolPanel = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ConsoleHeight)
                )
            }

            locationPanel?.let { (title, locations) ->
                LocationListPanel(
                    title = title,
                    locations = locations,
                    projectDir = projectDir,
                    onSelect = { entry ->
                        locationPanel = null
                        openLocation(entry)
                    },
                    onClose = { locationPanel = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ConsoleHeight)
                )
            }

            if (consoleVisible) {
                BuildConsole(
                    lines = consoleLines,
                    running = building,
                    maximized = consoleMaximized,
                    onToggleMaximize = { consoleMaximized = !consoleMaximized },
                    onCopy = { copyConsoleOutput() },
                    onStop = {
                        buildRunner.stop()
                        building = false
                        appendConsole("> build stopped", ConsoleLineKind.Error)
                    },

                    onClose = {
                        consoleVisible = false
                        consoleMaximized = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (consoleMaximized) {
                                Modifier.weight(1f)
                            } else {
                                Modifier.height(ConsoleHeight)
                            }
                        )
                )
            }

            lspStatus?.let { status ->
                Text(
                    text = status,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = LspStatusColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LspStatusBackground)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            SymbolBar(editor = editorRef)
        }

        if (drawerProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f * drawerProgress))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { closeDrawer() }
            )
            Column(
                modifier = Modifier
                    .width(DrawerWidth)
                    .fillMaxHeight()
                    .offset { IntOffset(((drawerProgress - 1f) * drawerWidthPx).roundToInt(), 0) }
                    .clip(DrawerShape)
                    .background(SidebarBackground)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { settleDrawer() },
                            onDragCancel = { settleDrawer() }
                        ) { change, drag ->
                            change.consume()
                            val next = (drawerAnim.value + drag / drawerWidthPx).coerceIn(0f, 1f)
                            scope.launch { drawerAnim.snapTo(next) }
                        }
                    }
                    .systemBarsPadding()
            ) {
                ExplorerDrawerContent(
                    projectDir = projectDir,
                    explorer = explorer,
                    activeFilePath = activePath,
                    onFileClick = { file ->
                        closeDrawer()
                        openFile(file)
                    },
                    onMenuRequested = { dialog = ExplorerDialog.Menu(it) },
                    onQuickAction = { action, parent ->
                        dialog = ExplorerDialog.Input(initial = "", parent = parent, kind = action)
                    },
                    onOpenSettings = {
                        closeDrawer()
                        onOpenSettings()
                    },
                    onOpenTerminal = {
                        closeDrawer()
                        onOpenTerminal()
                    },
                    onOpenInject = {
                        closeDrawer()
                        showInjectDialog = true
                    }
                )
            }
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Saved || saveState is SaveState.Failed) {
            delay(1600)
            saveState = SaveState.Idle
        }
    }

    LaunchedEffect(lspStatus) {
        if (lspStatus != null) {
            delay(2600)
            lspStatus = null
        }
    }

    if (showToolchainDialog) {
        ToolchainInstallDialog(
            onDismiss = { showToolchainDialog = false },
            onReady = {
                showToolchainDialog = false
                openRunMenu()
            }
        )
    }

    if (showInjectDialog) {
        InjectDialog(
            projectDir = projectDir,
            onDismiss = { showInjectDialog = false },
            onPatch = { apkPath, variant -> startPatch(apkPath, variant) }
        )
    }

    if (showPatchConsole) {
        PatchConsoleDialog(
            lines = patchConsoleLines,
            phase = patchPhase,
            onDismiss = { showPatchConsole = false },
            onCancel = { patchCancelled.set(true) },
            onInstall = { installPatched() }
        )
    }

    when (val current = dialog) {
        is ExplorerDialog.Menu -> NodeActionSheet(
            target = current.target,
            onDismiss = { dialog = null },
            onAction = { action ->
                val target = current.target
                dialog = when (action) {
                    NodeAction.NewFile -> ExplorerDialog.Input("", target, action)
                    NodeAction.NewFolder -> ExplorerDialog.Input("", target, action)
                    NodeAction.CopyPath -> {
                        copyProjectPath(target)
                        null
                    }
                    NodeAction.Rename -> ExplorerDialog.Input(target.name, target, action)
                    NodeAction.Delete -> ExplorerDialog.Delete(target)
                }
            }
        )
        is ExplorerDialog.Input -> NameInputDialog(
            title = when (current.kind) {
                NodeAction.NewFile -> stringResource(R.string.new_file)
                NodeAction.NewFolder -> stringResource(R.string.new_folder)
                else -> stringResource(R.string.rename)
            },
            initialValue = current.initial,
            onDismiss = { dialog = null },
            onSubmit = { name ->
                val result = when (current.kind) {
                    NodeAction.NewFile -> FileOps.createFile(current.parent, name)
                    NodeAction.NewFolder -> FileOps.createFolder(current.parent, name)
                    NodeAction.Rename -> FileOps.rename(current.parent, name)
                    else -> Result.failure(IllegalStateException())
                }
                result.fold(
                    onSuccess = { file ->
                        explorer.refresh()
                        when (current.kind) {
                            NodeAction.NewFile -> {
                                explorer.expand(file.parentFile)
                                openFile(file)
                            }
                            NodeAction.NewFolder -> explorer.expand(file)
                            NodeAction.Rename -> {
                                val oldPath = current.parent.absolutePath
                                val index = tabs.indexOfFirst { it.path == oldPath }
                                if (index >= 0) {
                                    tabs[index] = tabs[index].copy(path = file.absolutePath, name = file.name)
                                    if (activePath == oldPath) {
                                        activePath = file.absolutePath
                                        editorRef?.let { loadTabIntoEditor(it, tabs[index]) }
                                    }
                                }
                            }
                            else -> Unit
                        }
                    },
                    onFailure = { return@NameInputDialog it.message }
                )
                null
            }
        )
        is ExplorerDialog.Delete -> ConfirmDialog(
            title = stringResource(R.string.delete),
            message = if (current.target.isDirectory) {
                stringResource(R.string.delete_folder_message, current.target.name)
            } else {
                stringResource(R.string.delete_file_message, current.target.name)
            },
            confirmLabel = stringResource(R.string.delete),
            onDismiss = { dialog = null },
            onConfirm = {
                val target = current.target
                FileOps.delete(target)
                explorer.refresh()
                val prefix = target.absolutePath
                tabs.filter { it.path == prefix || it.path.startsWith(prefix + File.separator) }
                    .forEach { removeTab(it.path) }
            }
        )
        is ExplorerDialog.UnsavedClose -> UnsavedChangesDialog(
            fileName = current.name,
            onDismiss = {
                // Cancel stops the whole batch; only Save/Don't save continue it.
                pendingCloseQueue = emptyList()
                dialog = null
            },
            onSave = {
                val path = current.path
                val editor = editorRef
                val index = tabs.indexOfFirst { it.path == path }
                if (editor != null && index >= 0 && path == activePath) {
                    scope.launch {
                        val text = editor.text.toString()
                        withContext(Dispatchers.IO) { runCatching { File(path).writeText(text) } }
                        removeTab(path)
                        dialog = null
                        resumePendingClose()
                    }
                } else {
                    removeTab(path)
                    dialog = null
                    resumePendingClose()
                }
            },
            onDontSave = {
                removeTab(current.path)
                dialog = null
                resumePendingClose()
            }
        )
        null -> Unit
    }
}

@Composable
private fun TabChip(
    tab: TabItem,
    active: Boolean,
    menuExpanded: Boolean,
    onClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onCloseFile: () -> Unit,
    onCloseOthers: () -> Unit,
    onCloseAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .height(36.dp)
            .background(if (active) EditorBackground else TabBarBackground)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (active) AccentGreen else Color.Transparent)
        )
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tab.dirty) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DirtyDot)
                )
                Spacer(modifier = Modifier.width(7.dp))
            }
            Icon(
                painter = painterResource(XedIcons.fileType(tab.name)),
                contentDescription = null,
                tint = if (active) AccentGreen else Color(0xFF9BC4B4),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = tab.name,
                fontSize = 13.sp,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                color = if (active) TabActiveForeground else TabInactiveForeground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        TabDropdownMenu(
            expanded = menuExpanded,
            onDismiss = onDismissMenu,
            onCloseFile = onCloseFile,
            onCloseOthers = onCloseOthers,
            onCloseAll = onCloseAll
        )
    }
}

@Composable
private fun TabDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCloseFile: () -> Unit,
    onCloseOthers: () -> Unit,
    onCloseAll: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(TabMenuBackground)
            .width(IntrinsicSize.Max)
    ) {
        CompactEntry(stringResource(R.string.close_file), R.drawable.close, onCloseFile)
        CompactEntry(stringResource(R.string.close_other_tabs), R.drawable.circle, onCloseOthers)
        CompactEntry(stringResource(R.string.close_all_tabs), R.drawable.drag_indicator, onCloseAll)
    }
}

@Composable
private fun CompactEntry(label: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = TabMenuIcon,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TabMenuText,
            maxLines = 1
        )
    }
}

@Composable
private fun HamburgerIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.height * 0.09f
        listOf(0.28f, 0.5f, 0.72f).forEach { fraction ->
            drawLine(
                brush = HamburgerBrush,
                start = Offset(size.width * 0.08f, size.height * fraction),
                end = Offset(size.width * 0.92f, size.height * fraction),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun EmptyEditorState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.file),
                contentDescription = null,
                tint = TabInactiveForeground,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.no_open_files),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TabActiveForeground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.open_from_explorer),
                fontSize = 13.sp,
                color = TabInactiveForeground
            )
        }
    }
}
