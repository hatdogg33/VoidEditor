package com.voideditor.ui.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

class EditorEsSessionClient(
    private val context: Context,
    private val view: TerminalView,
    private val onShellExited: () -> Unit
) : TerminalSessionClient {

    override fun onTextChanged(changedSession: TerminalSession) {
        view.invalidate()
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        view.invalidate()
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        view.post {
            if (finishedSession == view.currentSession) onShellExited()
        }
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
        val cleaned = trimSelection(text) ?: return
        context.getSystemService(ClipboardManager::class.java)
            .setPrimaryClip(ClipData.newPlainText("terminal", cleaned))
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
        if (!text.isNullOrEmpty() && session != null) {
            val bytes = text.toByteArray(Charsets.UTF_8)
            session.write(bytes, 0, bytes.size)
        }
    }

    override fun onBell(session: TerminalSession) {}

    override fun onColorsChanged(session: TerminalSession) {
        view.invalidate()
    }

    override fun onTerminalCursorStateChange(state: Boolean) {}

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}

    override fun getTerminalCursorStyle(): Int = TerminalEmulator.TERMINAL_CURSOR_STYLE_BLOCK

    override fun logError(tag: String?, message: String?) {}

    override fun logWarn(tag: String?, message: String?) {}

    override fun logInfo(tag: String?, message: String?) {}

    override fun logDebug(tag: String?, message: String?) {}

    override fun logVerbose(tag: String?, message: String?) {}

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}

    override fun logStackTrace(tag: String?, e: Exception?) {}
}

class EditorEsViewClient(
    private val context: Context,
    private val view: TerminalView,
    private val ctrlArmed: () -> Boolean,
    private val altArmed: () -> Boolean,
    private val shiftArmed: () -> Boolean,
    private val onKeyConsumed: () -> Unit,
    private val onZoom: (Boolean) -> Unit
) : TerminalViewClient {

    override fun onScale(scale: Float): Float {
        if (scale < 0.9f || scale > 1.1f) {
            onZoom(scale > 1f)
            return 1f
        }
        return scale
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        context.getSystemService(InputMethodManager::class.java)
            .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean = false

    override fun shouldEnforceCharBasedInput(): Boolean = true

    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false

    override fun isTerminalViewSelected(): Boolean = true

    override fun copyModeChanged(copyMode: Boolean) {}

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean = false

    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean = false

    override fun onLongPress(event: MotionEvent?): Boolean = false

    override fun readControlKey(): Boolean = ctrlArmed()

    override fun readAltKey(): Boolean = altArmed()

    override fun readShiftKey(): Boolean = shiftArmed()

    override fun readFnKey(): Boolean = false

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean {
        onKeyConsumed()
        return false
    }

    override fun onEmulatorSet() {}

    override fun logError(tag: String?, message: String?) {}

    override fun logWarn(tag: String?, message: String?) {}

    override fun logInfo(tag: String?, message: String?) {}

    override fun logDebug(tag: String?, message: String?) {}

    override fun logVerbose(tag: String?, message: String?) {}

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}

    override fun logStackTrace(tag: String?, e: Exception?) {}
}

internal fun trimSelection(text: String?): String? {
    if (text.isNullOrEmpty()) return null
    val lines = text.split('\n')
    val trimmed = lines.map { it.trimEnd(' ', '\t', '\u00A0') }
    var start = 0
    var end = trimmed.size
    while (start < end && trimmed[start].isBlank()) start++
    while (end > start && trimmed[end - 1].isBlank()) end--
    if (start >= end) return null
    return trimmed.subList(start, end).joinToString("\n").ifBlank { null }
}
