package com.voideditor.lsp

import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.widget.CodeEditor

object LspPopupSizing {

    private const val HoverWidthRatio = 0.62f
    private const val HoverHeightDp = 170
    private const val SignatureWidthRatio = 0.58f
    private const val SignatureHeightDp = 120

    fun apply(lspEditor: LspEditor, editor: CodeEditor) {
        runCatching {
            val hover = lspEditor.hoverWindow ?: return@runCatching
            shrink(hover, editor, HoverWidthRatio, HoverHeightDp)
        }
        runCatching {
            val signature = lspEditor.signatureHelpWindow ?: return@runCatching
            shrink(signature, editor, SignatureWidthRatio, SignatureHeightDp)
        }
    }

    private fun shrink(window: Any, editor: CodeEditor, widthRatio: Float, heightDp: Int) {
        val width = (editor.width * widthRatio).toInt().coerceAtLeast(1)
        val height = (editor.dpUnit * heightDp).toInt().coerceAtLeast(1)
        setPrivateInt(window, "maxWidth", width)
        setPrivateInt(window, "maxHeight", height)
    }

    private fun setPrivateInt(target: Any, fieldName: String, value: Int) {
        var current: Class<*>? = target.javaClass
        while (current != null) {
            val field = runCatching { current!!.getDeclaredField(fieldName) }.getOrNull()
            if (field != null) {
                runCatching {
                    field.isAccessible = true
                    field.setInt(target, value)
                }
                return
            }
            current = current.superclass
        }
    }
}
