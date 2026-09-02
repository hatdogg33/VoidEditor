package com.voideditor.ui.terminal

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.voideditor.R
import com.voideditor.proot.InstallPhase
import com.voideditor.data.AppSettings
import com.voideditor.data.PreferenceSettings
import com.voideditor.proot.ProotConfig
import com.voideditor.proot.UbuntuInstaller
import com.voideditor.service.SessionRecord
import com.voideditor.service.TermuxService
import com.voideditor.ui.theme.SpringGreen
import com.termux.terminal.TerminalColors
import com.termux.terminal.TerminalSession
import com.termux.terminal.TextStyle
import com.termux.view.TerminalView
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

private val DrawerWidth = 220.dp
private val TerminalBackground = Color(0xFF0A2129)
private val HeaderBackground = Color(0xFF071B21)
private val DrawerBackground = Color(0xFF0A222B)
private val DrawerCardActive = Color(0xFF0D2C35)
private val DrawerCardInactive = Color(0xFF092027)
private val DrawerBorder = Color(0x3302F5A1)
private val KeyBackground = Color(0xFF0D252D)
private val KeyForeground = Color(0xFFDDF5EA)
private val KeyBorder = Color(0x3302F5A1)
private val ArmedBackground = Color(0x3302F5A1)
private val CardSurface = Color(0xFF0B2129)
private val KillRed = Color(0xFFFF5252)

private val HamburgerBrush = Brush.linearGradient(
    colors = listOf(SpringGreen, Color(0xFF00E676))
)

private val KeyShape = RoundedCornerShape(6.dp)
private val CardShape = RoundedCornerShape(16.dp)
private val ItemShape = RoundedCornerShape(10.dp)
private val DrawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)

private const val HoldDelayMs = 280L
private const val RepeatIntervalMs = 45L
private const val TerminalTextSize = 13
private const val MinTerminalTextSize = 8
private const val MaxTerminalTextSize = 28

private enum class TerminalFlow {
    AskInstall,
    Installing,
    AskNotification,
    Terminal
}

private enum class ModifierKey {
    Ctrl,
    Alt
}

private data class TerminalKey(
    val label: String,
    val payload: String? = null,
    val modifier: ModifierKey? = null,
    val repeatable: Boolean = false
)

private val FirstRow = listOf(
    TerminalKey("ESC", "\u001b"),
    TerminalKey("/", "/"),
    TerminalKey("-", "-"),
    TerminalKey("HOME", "\u001b[H", repeatable = true),
    TerminalKey("▲", "\u001b[A", repeatable = true),
    TerminalKey("END", "\u001b[F", repeatable = true),
    TerminalKey("PGUP", "\u001b[5~", repeatable = true)
)

private val SecondRow = listOf(
    TerminalKey("TAB", "\t"),
    TerminalKey("CTRL", modifier = ModifierKey.Ctrl),
    TerminalKey("ALT", modifier = ModifierKey.Alt),
    TerminalKey("◀", "\u001b[D", repeatable = true),
    TerminalKey("▼", "\u001b[B", repeatable = true),
    TerminalKey("▶", "\u001b[C", repeatable = true),
    TerminalKey("PGDN", "\u001b[6~", repeatable = true)
)

internal fun buildShellEnv(home: String): Array<String> = arrayOf(
    "TERM=xterm-256color",
    "HOME=$home",
    "PATH=/system/bin:/system/xbin:/vendor/bin",
    "LANG=C.UTF-8",
    "ENV=$home/.editor-es-shrc"
)

internal fun installShellProfile(context: Context) {
    val profile = File(context.filesDir, ".editor-es-shrc")
    val content = "clear() { printf '\u001b[2J\u001b[3J\u001b[H'; }\n"
    if (!profile.exists() || profile.readText() != content) {
        profile.writeText(content)
    }
}

internal fun applyColorScheme() {
    runCatching {
        val colors = TerminalColors.COLOR_SCHEME.mDefaultColors
        colors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFDDF5EA.toInt()
        colors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF0A2129.toInt()
        colors[TextStyle.COLOR_INDEX_CURSOR] = 0xFF02F5A1.toInt()
    }
}

internal fun writeText(session: TerminalSession?, text: String) {
    if (session == null) return
    val bytes = text.toByteArray(Charsets.UTF_8)
    session.write(bytes, 0, bytes.size)
}

private fun instantiateSession(
    context: Context,
    view: TerminalView,
    projectDir: File?,
    bootCommand: String?,
    onShellExited: () -> Unit
): TerminalSession {
    val client = VoidEditorSessionClient(context, view, onShellExited)
    val ubuntuReady = ProotConfig.isInstalled(context) && ProotConfig.isAvailable(context)
    return if (ubuntuReady) {
        ProotConfig.registerAndroidIds(context)
        ProotConfig.writeShellProfile(context)
        ProotConfig.prepareStorageMounts(context)
        val cwd = projectDir?.absolutePath ?: "/root"
        if (projectDir != null) {
            runCatching {
                File(ProotConfig.rootfsDir(context), cwd.trimStart('/')).mkdirs()
            }
        }
        TerminalSession(
            ProotConfig.prootBinary(context),
            context.filesDir.absolutePath,
            ProotConfig.prootArgs(context, cwd, bootCommand),
            ProotConfig.prootEnv(context),
            null,
            client
        )
    } else {
        val home = projectDir?.absolutePath ?: context.filesDir.absolutePath
        val args = if (bootCommand != null) {
            arrayOf("-c", "echo 'Ubuntu is required for this action'; exec /system/bin/sh")
        } else {
            emptyArray()
        }
        TerminalSession(
            "/system/bin/sh",
            home,
            args,
            buildShellEnv(context.filesDir.absolutePath),
            null,
            client
        )
    }
}

@Composable
fun TerminalScreen(
    onBack: () -> Unit,
    projectDir: File? = null,
    initialCommand: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var ctrlArmed by remember { mutableStateOf(false) }
    var altArmed by remember { mutableStateOf(false) }
    var terminalTextSize by remember { mutableStateOf(TerminalTextSize) }
    val appliedTextSize = remember { intArrayOf(TerminalTextSize) }
    var terminalView by remember { mutableStateOf<TerminalView?>(null) }
    var sessionRef by remember { mutableStateOf<TerminalSession?>(null) }
    var activeSessionId by remember { mutableIntStateOf(0) }
    var sessionsVersion by remember { mutableIntStateOf(0) }
    val drawerAnim = remember { Animatable(0f) }
    val density = LocalDensity.current
    val drawerWidthPx = remember(density) { with(density) { DrawerWidth.toPx() } }
    val isFinishing = remember { mutableStateOf(false) }
    val localView = LocalView.current
    var flow by remember {
        mutableStateOf(
            if (ProotConfig.isInstalled(context)) TerminalFlow.Terminal else TerminalFlow.AskInstall
        )
    }

    remember {
        applyColorScheme()
        installShellProfile(context)
    }

    fun openDrawer() {
        coroutineScope.launch {
            drawerAnim.animateTo(1f, spring(dampingRatio = 0.88f, stiffness = 420f))
        }
    }

    fun closeDrawer() {
        coroutineScope.launch {
            drawerAnim.animateTo(0f, spring(dampingRatio = 0.95f, stiffness = 500f))
        }
    }

    fun settleDrawer() {
        coroutineScope.launch {
            val target = if (drawerAnim.value > 0.45f) 1f else 0f
            drawerAnim.animateTo(target, spring(dampingRatio = 0.9f, stiffness = 450f))
        }
    }

    fun hideKeyboard() {
        val window = (localView.context as? Activity)?.window ?: return
        WindowCompat.getInsetsController(window, localView)
            .hide(WindowInsetsCompat.Type.ime())
        terminalView?.clearFocus()
    }

    fun leaveTerminal() {
        if (!isFinishing.value) {
            isFinishing.value = true
            hideKeyboard()
            if (!AppSettings.bool(PreferenceSettings.KeepTerminalAlive, true)) {
                sessionRef?.finishIfRunning()
                TermuxService.unregisterSession(context, activeSessionId)
            }
            sessionRef = null
            onBack()
        }
    }

    fun terminateTerminal() {
        if (!isFinishing.value) {
            isFinishing.value = true
            hideKeyboard()
            sessionRef?.finishIfRunning()
            sessionRef = null
            TermuxService.unregisterSession(context, activeSessionId)
            onBack()
        }
    }

    // Attach a session to the view
    fun attachSessionToView(session: TerminalSession, id: Int, view: TerminalView) {
        val client = VoidEditorSessionClient(context, view) {
            TermuxService.killSession(context, id)
            val remaining = TermuxService.allSessions()
            if (remaining.isNotEmpty()) {
                val next = remaining.last()
                sessionRef = next.session
                activeSessionId = next.id
                view.attachSession(next.session)
            } else {
                terminateTerminal()
            }
            sessionsVersion++
        }
        session.updateTerminalSessionClient(client)
        view.attachSession(session)
        sessionRef = session
        activeSessionId = id
    }

    // Start or attach initial session
    fun startInitialSession(view: TerminalView) {
        val tag = if (initialCommand != null) {
            "boot:${initialCommand.hashCode()}"
        } else {
            projectDir?.let { "project:${it.absolutePath}" }
        }

        if (tag != null) {
            TermuxService.taggedSession(tag)?.let { (id, existing) ->
                attachSessionToView(existing, id, view)
                return
            }
        }

        TermuxService.liveSession()?.let { existing ->
            attachSessionToView(existing, TermuxService.currentSessionId(), view)
            return
        }

        val session = instantiateSession(context, view, projectDir, initialCommand) {
            val remaining = TermuxService.allSessions()
            if (remaining.size <= 1) {
                terminateTerminal()
            } else {
                TermuxService.killSession(context, activeSessionId)
                val next = TermuxService.allSessions().lastOrNull()
                if (next != null) {
                    terminalView?.let { attachSessionToView(next.session, next.id, it) }
                }
            }
            sessionsVersion++
        }

        val sessionName = projectDir?.name ?: "Session ${TermuxService.sessionCount() + 1}"
        val sessionId = if (tag != null) {
            TermuxService.registerTagged(context, tag, session, sessionName)
        } else {
            TermuxService.registerSession(context, session, sessionName)
        }
        attachSessionToView(session, sessionId, view)
        sessionsVersion++
    }

    // Create a brand new session
    fun createNewSession(view: TerminalView) {
        val newSessionNumber = TermuxService.allSessions().size + 1
        val sessionName = "Session $newSessionNumber"
        val session = instantiateSession(context, view, projectDir, null) {
            TermuxService.killSession(context, activeSessionId)
            val next = TermuxService.allSessions().lastOrNull()
            if (next != null) {
                terminalView?.let { attachSessionToView(next.session, next.id, it) }
            } else {
                terminateTerminal()
            }
            sessionsVersion++
        }
        val id = TermuxService.registerSession(context, session, sessionName)
        attachSessionToView(session, id, view)
        closeDrawer()
        sessionsVersion++
    }

    // Switch to an existing session
    fun switchToSession(record: SessionRecord, view: TerminalView) {
        TermuxService.setActiveSession(record.id)
        attachSessionToView(record.session, record.id, view)
        closeDrawer()
        sessionsVersion++
    }

    // Kill a specific session
    fun killSessionById(id: Int) {
        TermuxService.killSession(context, id)
        val remaining = TermuxService.allSessions()
        if (remaining.isEmpty()) {
            terminateTerminal()
        } else if (activeSessionId == id) {
            val next = remaining.last()
            terminalView?.let { attachSessionToView(next.session, next.id, it) }
        }
        sessionsVersion++
    }

    DisposableEffect(Unit) {
        TermuxService.onExitRequested = { terminateTerminal() }
        TermuxService.onSessionsChanged = { sessionsVersion++ }
        onDispose {
            TermuxService.onExitRequested = null
            TermuxService.onSessionsChanged = null
        }
    }

    fun sendKey(key: TerminalKey) {
        when (key.modifier) {
            ModifierKey.Ctrl -> ctrlArmed = !ctrlArmed
            ModifierKey.Alt -> altArmed = !altArmed
            null -> {
                val payload = key.payload ?: return
                val session = terminalView?.currentSession
                if (ctrlArmed && payload.length == 1 && payload[0].isLetter()) {
                    val code = (payload.lowercase()[0] - 'a' + 1).toByte()
                    session?.write(byteArrayOf(code), 0, 1)
                } else {
                    writeText(session, payload)
                }
                ctrlArmed = false
                altArmed = false
            }
        }
    }

    val drawerProgress = drawerAnim.value

    BackHandler {
        when {
            drawerProgress > 0.05f -> closeDrawer()
            flow == TerminalFlow.AskInstall -> leaveTerminal()
            flow == TerminalFlow.Installing -> leaveTerminal()
            flow == TerminalFlow.AskNotification -> flow = TerminalFlow.Terminal
            flow == TerminalFlow.Terminal -> leaveTerminal()
        }
    }

    LaunchedEffect(flow, terminalView) {
        if (flow == TerminalFlow.Terminal && terminalView != null && sessionRef == null) {
            startInitialSession(terminalView!!)
        }
    }

    if (flow == TerminalFlow.AskInstall) {
        InstallAskDialog(
            onOk = { flow = TerminalFlow.Installing },
            onCancel = { leaveTerminal() }
        )
    }

    if (flow == TerminalFlow.AskNotification) {
        NotificationPermissionGate(onDismiss = { flow = TerminalFlow.Terminal })
    }

    val sessionsList = remember(sessionsVersion) { TermuxService.allSessions() }
    val currentSessionRecord = sessionsList.firstOrNull { it.id == activeSessionId }
    val sessionDisplayName = currentSessionRecord?.name
        ?: projectDir?.name?.uppercase()
        ?: "TERMINAL"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TerminalBackground)
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
        ) {
            // === Top Bar with Custom Hamburger Menu ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(HeaderBackground)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom 3-line horizontal bars (vertical stack)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            hideKeyboard()
                            if (drawerProgress > 0.5f) closeDrawer() else openDrawer()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    TerminalMenuIcon(
                        modifier = Modifier.size(20.dp, 14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Active Session Title + Badge
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing active dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SpringGreen)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = sessionDisplayName.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = SpringGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (sessionsList.size > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${sessionsList.indexOfFirst { it.id == activeSessionId } + 1}/${sessionsList.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = KeyForeground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // === Terminal View ===
            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        TerminalView(ctx, null).apply {
                            isFocusable = true
                            isFocusableInTouchMode = true
                            setTerminalViewClient(
                                VoidEditorViewClient(
                                    context = ctx,
                                    view = this,
                                    ctrlArmed = { ctrlArmed },
                                    altArmed = { altArmed },
                                    shiftArmed = { false },
                                    onKeyConsumed = {
                                        ctrlArmed = false
                                        altArmed = false
                                    },
                                    onZoom = { zoomIn ->
                                        val delta = if (zoomIn) 1 else -1
                                        terminalTextSize = (terminalTextSize + delta)
                                            .coerceIn(MinTerminalTextSize, MaxTerminalTextSize)
                                    }
                                )
                            )
                            setTextSize(terminalTextSize)
                            setTypeface(
                                runCatching {
                                    Typeface.createFromAsset(ctx.assets, "fonts/JetBrainsMono-Regular.ttf")
                                }.getOrDefault(Typeface.MONOSPACE)
                            )
                            terminalView = this
                            requestFocus()
                        }
                    },
                    update = { view ->
                        if (appliedTextSize[0] != terminalTextSize) {
                            view.setTextSize(terminalTextSize)
                            appliedTextSize[0] = terminalTextSize
                        }
                    }
                )
            }

            // === Terminal Extra Keys Toolbar ===
            Column(modifier = Modifier.background(HeaderBackground)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 4.dp),
                    content = {
                        FirstRow.forEach { key ->
                            TerminalKeyChip(
                                key = key,
                                armed = (key.modifier == ModifierKey.Ctrl && ctrlArmed) ||
                                    (key.modifier == ModifierKey.Alt && altArmed),
                                onTap = { sendKey(key) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    content = {
                        SecondRow.forEach { key ->
                            TerminalKeyChip(
                                key = key,
                                armed = (key.modifier == ModifierKey.Ctrl && ctrlArmed) ||
                                    (key.modifier == ModifierKey.Alt && altArmed),
                                onTap = { sendKey(key) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                )
            }
        }

        // === Sessions Drawer Overlay (Smooth spring animated) ===
        if (drawerProgress > 0f) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f * drawerProgress))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { closeDrawer() }
            )

            // Sliding Panel from Left (width = 220.dp)
            Column(
                modifier = Modifier
                    .width(DrawerWidth)
                    .fillMaxHeight()
                    .offset { IntOffset(((drawerProgress - 1f) * drawerWidthPx).roundToInt(), 0) }
                    .clip(DrawerShape)
                    .background(DrawerBackground)
                    .border(width = 1.dp, color = DrawerBorder, shape = DrawerShape)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { settleDrawer() },
                            onDragCancel = { settleDrawer() }
                        ) { change, drag ->
                            change.consume()
                            val next = (drawerAnim.value + drag / drawerWidthPx).coerceIn(0f, 1f)
                            coroutineScope.launch { drawerAnim.snapTo(next) }
                        }
                    }
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(12.dp)
            ) {
                // Drawer Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.terminal),
                            contentDescription = null,
                            tint = SpringGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SESSIONS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = SpringGreen
                        )
                    }

                    // Badge count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2602F5A1))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${sessionsList.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpringGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // "+ New Session" Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(ItemShape)
                        .background(Color(0x1A02F5A1))
                        .border(1.dp, SpringGreen, ItemShape)
                        .clickable {
                            terminalView?.let { createNewSession(it) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.add),
                            contentDescription = null,
                            tint = SpringGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Session",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpringGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sessions List
                Text(
                    text = "ACTIVE",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = KeyForeground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sessionsList, key = { it.id }) { record ->
                        val isActive = record.id == activeSessionId
                        SessionItemCard(
                            record = record,
                            isActive = isActive,
                            onSelect = {
                                terminalView?.let { switchToSession(record, it) }
                            },
                            onKill = {
                                killSessionById(record.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Exit Terminal Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(ItemShape)
                        .background(Color(0x0DFFFFFF))
                        .clickable { leaveTerminal() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_left),
                            contentDescription = null,
                            tint = KeyForeground.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Back to Editor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = KeyForeground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Install flow screens
        if (flow == TerminalFlow.Installing) {
            InstallerScreen(
                onFinished = { flow = TerminalFlow.AskNotification },
                onFailed = { leaveTerminal() }
            )
        }
    }
}

/**
 * Custom 3-horizontal-lines menu icon (hamburger) with rounded caps and gradient
 */
@Composable
private fun TerminalMenuIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.height * 0.16f
        val lineSpacing = size.height * 0.38f
        for (i in 0..2) {
            val y = strokeWidth / 2f + i * lineSpacing
            drawLine(
                brush = HamburgerBrush,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Session card item inside the Sessions Drawer
 */
@Composable
private fun SessionItemCard(
    record: SessionRecord,
    isActive: Boolean,
    onSelect: () -> Unit,
    onKill: () -> Unit
) {
    val bg = if (isActive) DrawerCardActive else DrawerCardInactive
    val border = if (isActive) SpringGreen else Color(0x1A02F5A1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ItemShape)
            .background(bg)
            .border(1.dp, border, ItemShape)
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left active indicator / terminal icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (isActive) Color(0x3302F5A1) else Color(0x1402F5A1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.terminal),
                contentDescription = null,
                tint = if (isActive) SpringGreen else KeyForeground.copy(alpha = 0.5f),
                modifier = Modifier.size(13.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Session Name and status
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.name,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) Color(0xFFF2FFFA) else KeyForeground.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isActive) SpringGreen else Color(0xFF888888))
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (isActive) "active" else "running",
                    fontSize = 9.5.sp,
                    color = if (isActive) SpringGreen else KeyForeground.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Kill "x" button
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0x1AFF5252))
                .clickable(onClick = onKill),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = "Close session",
                tint = KillRed,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@Composable
private fun InstallAskDialog(onOk: () -> Unit, onCancel: () -> Unit) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(CardSurface)
                .border(1.dp, KeyBorder, CardShape)
                .padding(22.dp)
        ) {
            Text(
                text = stringResource(R.string.ubuntu_install_title),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SpringGreen
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.ubuntu_install_message),
                fontSize = 13.sp,
                color = KeyForeground,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = onOk) {
                    Text(stringResource(R.string.ok), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InstallerScreen(onFinished: () -> Unit, onFailed: () -> Unit) {
    val context = LocalContext.current
    val installer = remember { UbuntuInstaller(context) }
    var phase by remember { mutableStateOf<InstallPhase>(InstallPhase.Idle) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    LaunchedEffect(Unit) {
        installer.install { newPhase -> mainHandler.post { phase = newPhase } }
    }

    DisposableEffect(Unit) {
        onDispose {
            installer.cancel()
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    LaunchedEffect(phase) {
        when (phase) {
            InstallPhase.Done -> {
                delay(600)
                onFinished()
            }
            is InstallPhase.Failed -> {
                delay(1200)
                onFailed()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .clip(CardShape)
                .background(CardSurface)
                .border(1.dp, KeyBorder, CardShape)
                .padding(22.dp)
        ) {
            Text(
                text = stringResource(R.string.ubuntu_installing),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SpringGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            when (val current = phase) {
                InstallPhase.Idle -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = SpringGreen,
                        strokeCap = StrokeCap.Round
                    )
                }
                is InstallPhase.Downloading -> {
                    LinearProgressIndicator(
                        progress = { current.percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = SpringGreen,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Downloading rootfs • ${current.percent}% • " +
                            "%.1f MB / %.1f MB".format(current.receivedMb, current.totalMb),
                        fontSize = 12.sp,
                        color = KeyForeground
                    )
                }
                is InstallPhase.Extracting -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = SpringGreen,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "extracting: ${current.entry}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SpringGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${current.count} files",
                        fontSize = 12.sp,
                        color = KeyForeground
                    )
                }
                InstallPhase.Finalizing -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = SpringGreen,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Configuring Ubuntu…",
                        fontSize = 12.sp,
                        color = KeyForeground
                    )
                }
                InstallPhase.Done -> {
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = SpringGreen,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ubuntu installed ✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpringGreen
                    )
                }
                is InstallPhase.Failed -> {
                    Text(
                        text = current.message,
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionGate(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permissionGranted = true
        onDismiss()
    }

    LaunchedEffect(Unit) {
        delay(400)
        if (permissionGranted) onDismiss()
    }

    if (!permissionGranted) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(CardSurface)
                    .border(1.dp, KeyBorder, CardShape)
                    .padding(22.dp)
            ) {
                Text(
                    text = stringResource(R.string.notification_permission_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpringGreen
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.notification_permission_message),
                    fontSize = 13.sp,
                    color = KeyForeground,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    ) {
                        Text(stringResource(R.string.ok), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalKeyChip(
    key: TerminalKey,
    armed: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(30.dp)
            .graphicsLayer {
                val scale = if (pressed) 0.9f else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(KeyShape)
            .background(if (armed) ArmedBackground else KeyBackground)
            .border(1.dp, if (armed) SpringGreen else KeyBorder, KeyShape)
            .pointerInput(key) {
                coroutineScope {
                    awaitEachGesture {
                        awaitFirstDown()
                        pressed = true
                        onTap()
                        val repeatJob = if (key.repeatable) {
                            launch {
                                delay(HoldDelayMs)
                                while (true) {
                                    onTap()
                                    delay(RepeatIntervalMs)
                                }
                            }
                        } else {
                            null
                        }
                        try {
                            waitForUpOrCancellation()
                        } finally {
                            repeatJob?.cancel()
                            pressed = false
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key.label,
            fontSize = 9.5.sp,
            fontWeight = if (armed) FontWeight.Bold else FontWeight.SemiBold,
            letterSpacing = 0.3.sp,
            color = if (armed) SpringGreen else KeyForeground,
            maxLines = 1
        )
    }
}