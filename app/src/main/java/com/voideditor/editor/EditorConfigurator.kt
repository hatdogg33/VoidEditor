package com.voideditor.editor

import com.voideditor.data.AppSettings
import com.voideditor.data.BoolSpec
import com.voideditor.data.ChoiceSpec
import com.voideditor.data.EditorSettings
import com.voideditor.data.FloatSpec
import com.voideditor.data.IntSpec
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.DirectAccessProps
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.getComponent

object EditorConfigurator {

    private fun boolOf(key: String): Boolean {
        val spec = EditorSettings.specs.firstOrNull { it.key == key } as? BoolSpec
        return AppSettings.bool(key, spec?.default ?: false)
    }

    private fun intOf(key: String): Int {
        val spec = EditorSettings.specs.firstOrNull { it.key == key }
        val default = when (spec) {
            is IntSpec -> spec.default
            is ChoiceSpec -> spec.default
            else -> 0
        }
        return AppSettings.int(key, default)
    }

    private fun floatOf(key: String): Float {
        val spec = EditorSettings.specs.firstOrNull { it.key == key } as? FloatSpec
        return AppSettings.float(key, spec?.default ?: 0f)
    }

    fun apply(editor: CodeEditor) {
        editor.setTextSize(floatOf(EditorSettings.TextSize))
        editor.tabWidth = intOf(EditorSettings.TabWidth)
        editor.setTextLetterSpacing(floatOf(EditorSettings.TextLetterSpacing))
        editor.setLineSpacing(
            floatOf(EditorSettings.LineSpacingExtra),
            floatOf(EditorSettings.LineSpacingMultiplier)
        )
        editor.setVerticalExtraSpaceFactor(floatOf(EditorSettings.VerticalExtraSpaceFactor))
        editor.isLigatureEnabled = boolOf(EditorSettings.LigatureEnabled)
        editor.setRenderFunctionCharacters(boolOf(EditorSettings.RenderFunctionCharacters))

        editor.isLineNumberEnabled = boolOf(EditorSettings.LineNumber)
        editor.setPinLineNumber(boolOf(EditorSettings.PinLineNumber))
        editor.isFirstLineNumberAlwaysVisible =
            boolOf(EditorSettings.FirstLineNumberAlwaysVisible)
        editor.isDisplayLnPanel = boolOf(EditorSettings.DisplayLnPanel)
        editor.setLnPanelPositionMode(intOf(EditorSettings.LnPanelPositionMode))
        editor.setLnPanelPosition(intOf(EditorSettings.LnPanelPosition))

        editor.setWordwrap(
            boolOf(EditorSettings.Wordwrap),
            boolOf(EditorSettings.AntiWordBreaking)
        )
        editor.isScalable = boolOf(EditorSettings.Scalable)

        editor.isHighlightCurrentLine = boolOf(EditorSettings.HighlightCurrentLine)
        editor.isHighlightCurrentBlock = boolOf(EditorSettings.HighlightCurrentBlock)
        editor.isHighlightBracketPair = boolOf(EditorSettings.HighlightBracketPair)
        editor.isBlockLineEnabled = boolOf(EditorSettings.BlockLine)
        editor.setBlockLineWidth(floatOf(EditorSettings.BlockLineWidth))

        editor.isCursorAnimationEnabled = boolOf(EditorSettings.CursorAnimation)
        editor.setCursorBlinkPeriod(intOf(EditorSettings.CursorBlinkPeriod))
        editor.isStickyTextSelection = boolOf(EditorSettings.StickyTextSelection)

        editor.isUndoEnabled = boolOf(EditorSettings.UndoEnabled)
        editor.isSoftKeyboardEnabled = boolOf(EditorSettings.SoftKeyboardEnabled)
        editor.setScrollBarEnabled(boolOf(EditorSettings.ScrollBarEnabled))
        editor.isHardwareAcceleratedDrawAllowed = boolOf(EditorSettings.HardwareAccelerated)

        editor.setNonPrintablePaintingFlags(nonPrintableFlags())

        applyProps(editor.props)
        applyComponents(editor)
        editor.invalidate()
    }

    private fun nonPrintableFlags(): Int {
        var flags = 0
        if (boolOf(EditorSettings.DrawWhitespaceLeading)) {
            flags = flags or CodeEditor.FLAG_DRAW_WHITESPACE_LEADING
        }
        if (boolOf(EditorSettings.DrawWhitespaceInner)) {
            flags = flags or CodeEditor.FLAG_DRAW_WHITESPACE_INNER
        }
        if (boolOf(EditorSettings.DrawWhitespaceTrailing)) {
            flags = flags or CodeEditor.FLAG_DRAW_WHITESPACE_TRAILING
        }
        if (boolOf(EditorSettings.DrawWhitespaceEmptyLine)) {
            flags = flags or CodeEditor.FLAG_DRAW_WHITESPACE_FOR_EMPTY_LINE
        }
        if (boolOf(EditorSettings.DrawWhitespaceInSelection)) {
            flags = flags or CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION
        }
        if (boolOf(EditorSettings.DrawLineSeparator)) {
            flags = flags or CodeEditor.FLAG_DRAW_LINE_SEPARATOR
        }
        if (boolOf(EditorSettings.DrawTabSameAsSpace)) {
            flags = flags or CodeEditor.FLAG_DRAW_TAB_SAME_AS_SPACE
        }
        if (boolOf(EditorSettings.DrawSoftWrap)) {
            flags = flags or CodeEditor.FLAG_DRAW_SOFT_WRAP
        }
        return flags
    }

    private fun applyProps(props: DirectAccessProps) {
        props.autoIndent = boolOf(EditorSettings.AutoIndent)
        props.symbolPairAutoCompletion = boolOf(EditorSettings.SymbolPairAutoCompletion)
        props.deleteEmptyLineFast = boolOf(EditorSettings.DeleteEmptyLineFast)
        props.deleteMultiSpaces = intOf(EditorSettings.DeleteMultiSpaces)
        props.enhancedHomeAndEnd = boolOf(EditorSettings.EnhancedHomeAndEnd)
        props.formatPastedText = boolOf(EditorSettings.FormatPastedText)
        props.autoCompletionOnComposing = boolOf(EditorSettings.AutoCompletionOnComposing)
        props.selectCompletionItemOnEnterForSoftKbd =
            boolOf(EditorSettings.SelectCompletionOnEnter)
        props.disallowSuggestions = boolOf(EditorSettings.DisallowSuggestions)
        props.useICULibToSelectWords = boolOf(EditorSettings.UseICUToSelectWords)
        props.reselectOnLongPress = boolOf(EditorSettings.ReselectOnLongPress)
        props.dragSelectAfterLongPress = boolOf(EditorSettings.DragSelectAfterLongPress)
        props.clipboardTextLengthLimit = intOf(EditorSettings.ClipboardLengthLimit)
        props.overScrollEnabled = boolOf(EditorSettings.OverScrollEnabled)
        props.scrollFling = boolOf(EditorSettings.ScrollFling)
        props.singleDirectionFling = boolOf(EditorSettings.SingleDirectionFling)
        props.scrollAnimationDurationMs = intOf(EditorSettings.ScrollAnimationDuration)
        props.fastScrollSensitivity = floatOf(EditorSettings.FastScrollSensitivity)
        props.stickyScroll = boolOf(EditorSettings.StickyScroll)
        props.stickyScrollMaxLines = intOf(EditorSettings.StickyScrollMaxLines)
        props.drawSideBlockLine = boolOf(EditorSettings.DrawSideBlockLine)
        props.boldMatchingDelimiters = boolOf(EditorSettings.BoldMatchingDelimiters)
        props.enableRoundTextBackground = boolOf(EditorSettings.RoundTextBackground)
        props.roundTextBackgroundFactor = floatOf(EditorSettings.RoundTextBackgroundFactor)
        props.textBackgroundWrapTextOnly = boolOf(EditorSettings.TextBackgroundWrapTextOnly)
        props.indicatorWaveLength = floatOf(EditorSettings.IndicatorWaveLength)
        props.indicatorWaveWidth = floatOf(EditorSettings.IndicatorWaveWidth)
        props.indicatorWaveAmplitude = floatOf(EditorSettings.IndicatorWaveAmplitude)
        props.cacheRenderNodeForLongLines = boolOf(EditorSettings.CacheRenderNodeLongLines)
        props.actionWhenLineNumberClicked = intOf(EditorSettings.LineNumberClickAction)
        props.hardwrapColumn = intOf(EditorSettings.HardwrapColumn)
        props.showMinimap = boolOf(EditorSettings.ShowMinimap)
    }

    private fun applyComponents(editor: CodeEditor) {
        runCatching {
            editor.getComponent<EditorAutoCompletion>().apply {
                setEnabledAnimation(boolOf(EditorSettings.CompletionAnimation))
                setHighlightMatchedLabel(boolOf(EditorSettings.CompletionHighlightMatched))
                setMaxHeight(dpToPx(editor, intOf(EditorSettings.CompletionMaxHeight)))
                setCompletionWndPositionMode(
                    when (intOf(EditorSettings.CompletionWidthMode)) {
                        1 -> EditorAutoCompletion.WINDOW_POS_MODE_AUTO
                        2 -> EditorAutoCompletion.WINDOW_POS_MODE_FULL_WIDTH_ALWAYS
                        else -> EditorAutoCompletion.WINDOW_POS_MODE_FOLLOW_CURSOR_ALWAYS
                    }
                )
            }
        }
        runCatching {
            editor.getComponent<Magnifier>().isEnabled = boolOf(EditorSettings.Magnifier)
        }
        runCatching {
            editor.getComponent<EditorTextActionWindow>().isEnabled =
                boolOf(EditorSettings.TextActionWindow)
        }
    }

    private fun dpToPx(editor: CodeEditor, dp: Int): Int =
        (dp * editor.resources.displayMetrics.density).toInt()
}
