package com.voideditor.data

sealed interface SettingSpec {
    val key: String
    val title: String
}

data class HeaderSpec(
    override val key: String,
    override val title: String
) : SettingSpec

data class BoolSpec(
    override val key: String,
    override val title: String,
    val summary: String,
    val default: Boolean
) : SettingSpec

data class IntSpec(
    override val key: String,
    override val title: String,
    val summary: String,
    val default: Int,
    val min: Int,
    val max: Int,
    val step: Int = 1,
    val unit: String = ""
) : SettingSpec

data class FloatSpec(
    override val key: String,
    override val title: String,
    val summary: String,
    val default: Float,
    val min: Float,
    val max: Float,
    val decimals: Int = 2,
    val unit: String = ""
) : SettingSpec

data class ChoiceSpec(
    override val key: String,
    override val title: String,
    val summary: String,
    val default: Int,
    val options: List<Pair<Int, String>>
) : SettingSpec

object EditorSettings {

    const val TextSize = "editor_text_size"
    const val TabWidth = "editor_tab_width"
    const val LineNumber = "editor_line_number"
    const val PinLineNumber = "editor_pin_line_number"
    const val FirstLineNumberAlwaysVisible = "editor_first_ln_visible"
    const val LineNumberClickAction = "editor_ln_click_action"
    const val Wordwrap = "editor_wordwrap"
    const val AntiWordBreaking = "editor_anti_word_breaking"
    const val Scalable = "editor_scalable"
    const val BlockLine = "editor_block_line"
    const val BlockLineWidth = "editor_block_line_width"
    const val DrawSideBlockLine = "editor_side_block_line"
    const val HighlightCurrentLine = "editor_highlight_current_line"
    const val HighlightCurrentBlock = "editor_highlight_current_block"
    const val HighlightBracketPair = "editor_highlight_bracket_pair"
    const val BoldMatchingDelimiters = "editor_bold_delimiters"
    const val CursorAnimation = "editor_cursor_animation"
    const val CursorBlinkPeriod = "editor_cursor_blink"
    const val StickyTextSelection = "editor_sticky_selection"
    const val StickyScroll = "editor_sticky_scroll"
    const val StickyScrollMaxLines = "editor_sticky_scroll_lines"
    const val ShowMinimap = "editor_minimap"
    const val DisplayLnPanel = "editor_ln_panel"
    const val LnPanelPositionMode = "editor_ln_panel_mode"
    const val LnPanelPosition = "editor_ln_panel_position"
    const val LineSpacingExtra = "editor_line_spacing_extra"
    const val LineSpacingMultiplier = "editor_line_spacing_mult"
    const val TextLetterSpacing = "editor_letter_spacing"
    const val LigatureEnabled = "editor_ligature"
    const val RenderFunctionCharacters = "editor_render_function_chars"
    const val VerticalExtraSpaceFactor = "editor_vertical_extra_space"
    const val UndoEnabled = "editor_undo"
    const val AutoIndent = "editor_auto_indent"
    const val SymbolPairAutoCompletion = "editor_symbol_pair"
    const val DeleteEmptyLineFast = "editor_delete_empty_line"
    const val DeleteMultiSpaces = "editor_delete_multi_spaces"
    const val EnhancedHomeAndEnd = "editor_enhanced_home_end"
    const val FormatPastedText = "editor_format_pasted"
    const val AutoCompletionOnComposing = "editor_completion_composing"
    const val CompletionAnimation = "editor_completion_animation"
    const val CompletionHighlightMatched = "editor_completion_highlight"
    const val CompletionMaxHeight = "editor_completion_max_height"
    const val CompletionWidthMode = "editor_completion_width_mode"
    const val SelectCompletionOnEnter = "editor_completion_enter"
    const val SoftKeyboardEnabled = "editor_soft_keyboard"
    const val DisallowSuggestions = "editor_disallow_suggestions"
    const val Magnifier = "editor_magnifier"
    const val TextActionWindow = "editor_text_action_window"
    const val UseICUToSelectWords = "editor_icu_words"
    const val ReselectOnLongPress = "editor_reselect_long_press"
    const val DragSelectAfterLongPress = "editor_drag_select"
    const val ScrollBarEnabled = "editor_scrollbar"
    const val OverScrollEnabled = "editor_over_scroll"
    const val ScrollFling = "editor_scroll_fling"
    const val SingleDirectionFling = "editor_single_direction_fling"
    const val ScrollAnimationDuration = "editor_scroll_anim_duration"
    const val FastScrollSensitivity = "editor_fast_scroll_sensitivity"
    const val DrawWhitespaceLeading = "editor_ws_leading"
    const val DrawWhitespaceInner = "editor_ws_inner"
    const val DrawWhitespaceTrailing = "editor_ws_trailing"
    const val DrawWhitespaceEmptyLine = "editor_ws_empty_line"
    const val DrawWhitespaceInSelection = "editor_ws_in_selection"
    const val DrawLineSeparator = "editor_draw_line_separator"
    const val DrawTabSameAsSpace = "editor_tab_as_space"
    const val DrawSoftWrap = "editor_draw_soft_wrap"
    const val HardwrapColumn = "editor_hardwrap_column"
    const val RoundTextBackground = "editor_round_text_bg"
    const val RoundTextBackgroundFactor = "editor_round_text_bg_factor"
    const val TextBackgroundWrapTextOnly = "editor_text_bg_wrap_only"
    const val IndicatorWaveLength = "editor_indicator_wave_length"
    const val IndicatorWaveWidth = "editor_indicator_wave_width"
    const val IndicatorWaveAmplitude = "editor_indicator_wave_amplitude"
    const val HardwareAccelerated = "editor_hardware_accelerated"
    const val CacheRenderNodeLongLines = "editor_cache_render_node"
    const val ClipboardLengthLimit = "editor_clipboard_limit"

    val specs: List<SettingSpec> = listOf(
        HeaderSpec("hdr_text", "Text"),
        FloatSpec(TextSize, "Text size", "Editor font size in sp", 15f, 8f, 40f, 0, "sp"),
        IntSpec(TabWidth, "Tab width", "Spaces used to render a tab", 4, 1, 16),
        FloatSpec(TextLetterSpacing, "Letter spacing", "Extra space between characters", 0f, -0.1f, 0.5f, 2),
        FloatSpec(LineSpacingExtra, "Line spacing extra", "Extra pixels between rows", 0f, 0f, 40f, 0, "px"),
        FloatSpec(LineSpacingMultiplier, "Line spacing multiplier", "Row height multiplier", 1f, 0.8f, 2.5f, 2, "x"),
        FloatSpec(VerticalExtraSpaceFactor, "Vertical extra space", "Scrollable space after the last line", 0.5f, 0f, 1f, 2),
        BoolSpec(LigatureEnabled, "Font ligatures", "Render -> and != as single glyphs", false),
        BoolSpec(RenderFunctionCharacters, "Render function characters", "Show control characters visibly", true),

        HeaderSpec("hdr_line_number", "Line numbers"),
        BoolSpec(LineNumber, "Show line numbers", "Display the line number gutter", true),
        BoolSpec(PinLineNumber, "Pin line numbers", "Keep the gutter fixed when scrolling sideways", false),
        BoolSpec(FirstLineNumberAlwaysVisible, "First number always visible", "Keep line 1 visible in wordwrap mode", true),
        ChoiceSpec(
            LineNumberClickAction,
            "Line number click",
            "Action when the gutter is tapped",
            2,
            listOf(0 to "Do nothing", 1 to "Select line", 2 to "Cursor to line start")
        ),
        BoolSpec(DisplayLnPanel, "Line number panel", "Show the line tooltip while scrolling", true),
        ChoiceSpec(
            LnPanelPositionMode,
            "Panel position mode",
            "Fixed spot or follow the scrollbar",
            1,
            listOf(0 to "Fixed", 1 to "Follow")
        ),
        ChoiceSpec(
            LnPanelPosition,
            "Panel position",
            "Where the panel is drawn",
            6,
            listOf(2 to "Top", 15 to "Center", 8 to "Bottom", 6 to "Top right", 3 to "Top left")
        ),

        HeaderSpec("hdr_layout", "Layout"),
        BoolSpec(Wordwrap, "Word wrap", "Wrap long lines instead of scrolling", false),
        BoolSpec(AntiWordBreaking, "Anti word breaking", "Avoid splitting words when wrapping", false),
        IntSpec(HardwrapColumn, "Hard wrap marker", "Column for the reminder line, 0 disables", 0, 0, 200),
        BoolSpec(Scalable, "Pinch to zoom", "Change text size with a pinch gesture", true),

        HeaderSpec("hdr_highlight", "Highlighting"),
        BoolSpec(HighlightCurrentLine, "Highlight current line", "Tint the row under the cursor", true),
        BoolSpec(HighlightCurrentBlock, "Highlight current block", "Emphasise the enclosing block line", true),
        BoolSpec(HighlightBracketPair, "Highlight bracket pair", "Mark the matching bracket", true),
        BoolSpec(BoldMatchingDelimiters, "Bold matching delimiters", "Draw matched brackets in bold", true),
        BoolSpec(BlockLine, "Block guide lines", "Vertical indent guides", true),
        FloatSpec(BlockLineWidth, "Block line width", "Thickness of the indent guides", 1f, 0.5f, 4f, 1),
        BoolSpec(DrawSideBlockLine, "Side block line", "Guide line shown in wordwrap mode", true),
        BoolSpec(RoundTextBackground, "Round text background", "Rounded corners on highlighted text", true),
        FloatSpec(RoundTextBackgroundFactor, "Round background factor", "Corner radius factor", 0.13f, 0f, 0.5f, 2),
        BoolSpec(TextBackgroundWrapTextOnly, "Background wraps text only", "Do not extend the background to the row end", false),

        HeaderSpec("hdr_whitespace", "Invisible characters"),
        BoolSpec(DrawWhitespaceLeading, "Leading whitespace", "Show indentation dots", true),
        BoolSpec(DrawWhitespaceInner, "Inner whitespace", "Show spaces inside a line", false),
        BoolSpec(DrawWhitespaceTrailing, "Trailing whitespace", "Show spaces at the line end", true),
        BoolSpec(DrawWhitespaceEmptyLine, "Empty line whitespace", "Show indentation on blank lines", true),
        BoolSpec(DrawWhitespaceInSelection, "Whitespace in selection", "Show spaces inside a selection", true),
        BoolSpec(DrawLineSeparator, "Line separators", "Mark line break characters", false),
        BoolSpec(DrawTabSameAsSpace, "Tab as space", "Render tabs like spaces", false),
        BoolSpec(DrawSoftWrap, "Soft wrap marker", "Mark where a line was wrapped", false),

        HeaderSpec("hdr_cursor", "Cursor and selection"),
        BoolSpec(CursorAnimation, "Cursor animation", "Animate the cursor between positions", true),
        IntSpec(CursorBlinkPeriod, "Cursor blink period", "Milliseconds per blink, 0 disables", 500, 0, 2000, 50, "ms"),
        BoolSpec(StickyTextSelection, "Sticky selection", "Keep the selection after editing", false),
        BoolSpec(UseICUToSelectWords, "ICU word selection", "Use ICU for word boundaries", true),
        BoolSpec(ReselectOnLongPress, "Reselect on long press", "Long press replaces the current selection", true),
        BoolSpec(DragSelectAfterLongPress, "Drag select after long press", "Keep selecting while the finger moves", true),
        BoolSpec(Magnifier, "Magnifier", "Show a magnifier while dragging handles", true),
        BoolSpec(TextActionWindow, "Text action window", "Show the copy and paste popup", true),
        IntSpec(ClipboardLengthLimit, "Clipboard limit", "Maximum characters that can be copied", 524288, 4096, 4194304, 4096),

        HeaderSpec("hdr_editing", "Editing"),
        BoolSpec(UndoEnabled, "Undo history", "Keep an undo and redo stack", true),
        BoolSpec(AutoIndent, "Auto indent", "Copy the indentation to the new line", true),
        BoolSpec(SymbolPairAutoCompletion, "Auto close brackets", "Insert the closing bracket automatically", true),
        BoolSpec(DeleteEmptyLineFast, "Fast empty line delete", "Delete a blank line in one press", true),
        IntSpec(DeleteMultiSpaces, "Delete multiple spaces", "Spaces removed per press, -1 follows tab size", 1, -1, 8),
        BoolSpec(EnhancedHomeAndEnd, "Enhanced home and end", "Jump to the first non blank character first", true),
        BoolSpec(FormatPastedText, "Format pasted text", "Run the formatter after pasting", false),

        HeaderSpec("hdr_completion", "Completion"),
        BoolSpec(CompletionAnimation, "Completion animation", "Animate the completion popup", true),
        BoolSpec(CompletionHighlightMatched, "Highlight matched text", "Emphasise the typed prefix", true),
        IntSpec(CompletionMaxHeight, "Completion height", "Maximum popup height in dp", 260, 120, 600, 10, "dp"),
        ChoiceSpec(
            CompletionWidthMode,
            "Completion width",
            "How wide the completion popup grows",
            0,
            listOf(0 to "Compact", 1 to "Auto", 2 to "Full width")
        ),
        BoolSpec(AutoCompletionOnComposing, "Complete while composing", "Suggest during IME composition", true),
        BoolSpec(SelectCompletionOnEnter, "Enter selects first item", "Accept the first suggestion with enter", true),

        HeaderSpec("hdr_input", "Input"),
        BoolSpec(SoftKeyboardEnabled, "Software keyboard", "Allow the on screen keyboard", true),
        BoolSpec(DisallowSuggestions, "Disallow IME suggestions", "Block keyboard autocorrect", false),

        HeaderSpec("hdr_scroll", "Scrolling"),
        BoolSpec(ScrollBarEnabled, "Scroll bars", "Show the scroll bars", true),
        BoolSpec(OverScrollEnabled, "Over scroll", "Allow scrolling past the bounds", false),
        BoolSpec(ScrollFling, "Fling scrolling", "Keep scrolling after the finger lifts", true),
        BoolSpec(SingleDirectionFling, "Single direction fling", "Restrict fling to one axis", true),
        IntSpec(ScrollAnimationDuration, "Scroll animation", "Smooth scroll duration", 250, 0, 1000, 25, "ms"),
        FloatSpec(FastScrollSensitivity, "Fast scroll sensitivity", "Multiplier when alt is held", 5f, 1f, 20f, 1, "x"),
        BoolSpec(StickyScroll, "Sticky scroll", "Pin the enclosing scope to the top", false),
        IntSpec(StickyScrollMaxLines, "Sticky scroll lines", "Maximum pinned lines", 3, 1, 10),

        HeaderSpec("hdr_diagnostics", "Diagnostics"),
        FloatSpec(IndicatorWaveLength, "Wave length", "Squiggle length in dp", 18f, 4f, 40f, 0, "dp"),
        FloatSpec(IndicatorWaveWidth, "Wave width", "Squiggle stroke width in dp", 0.9f, 0.2f, 4f, 1, "dp"),
        FloatSpec(IndicatorWaveAmplitude, "Wave amplitude", "Squiggle height in dp", 4f, 1f, 12f, 0, "dp"),

        HeaderSpec("hdr_performance", "Performance"),
        BoolSpec(HardwareAccelerated, "Hardware accelerated draw", "Use the GPU for rendering", true),
        BoolSpec(CacheRenderNodeLongLines, "Cache long lines", "Trade memory for smoother long lines", false),
        BoolSpec(ShowMinimap, "Minimap", "Experimental code overview on the right", false)
    )
}

object PreferenceSettings {

    const val LspEnabled = "lsp_enabled"
    const val LspInlayHint = "lsp_inlay_hint"
    const val LspHover = "lsp_hover"
    const val LspSignatureHelp = "lsp_signature_help"
    const val LspDiagnostics = "lsp_diagnostics"
    const val LspMaxFileKb = "lsp_max_file_kb"
    const val BuildAbi = "build_abi"
    const val AutoSaveOnBuild = "auto_save_on_build"
    const val KeepTerminalAlive = "keep_terminal_alive"
    const val BuildApiLevel = "build_api_level"
    const val BuildType = "build_type"
    const val ConsoleAutoOpen = "console_auto_open"
    const val ConsoleMaxLines = "console_max_lines"
    const val AutoSaveEnabled = "auto_save_enabled"
    const val AutoSaveInterval = "auto_save_interval"

    val specs: List<SettingSpec> = listOf(
        HeaderSpec("hdr_lsp", "Code intelligence"),
        BoolSpec(LspEnabled, "Enable clangd", "Completion, diagnostics and navigation for C and C++", false),
        BoolSpec(LspHover, "Hover documentation", "Show docs when a symbol is tapped", true),
        BoolSpec(LspSignatureHelp, "Signature help", "Show parameter hints inside calls", true),
        BoolSpec(LspDiagnostics, "Diagnostics", "Underline errors and warnings", true),
        BoolSpec(LspInlayHint, "Inlay hints", "Inline parameter and type hints, costs memory", true),
        IntSpec(LspMaxFileKb, "Skip files above", "Files larger than this are opened without clangd", 2048, 128, 8192, 128, "KB"),

        HeaderSpec("hdr_build", "Build"),
        ChoiceSpec(
            BuildAbi,
            "Target ABI",
            "Architecture passed to CMake",
            0,
            listOf(0 to "arm64-v8a", 1 to "armeabi-v7a", 2 to "Both (arm64 + arm)")
        ),
        ChoiceSpec(
            BuildApiLevel,
            "Android API level",
            "Minimum platform for the native build",
            24,
            listOf(21 to "21", 24 to "24", 28 to "28", 31 to "31", 34 to "34")
        ),
        ChoiceSpec(
            BuildType,
            "Build type",
            "CMake build configuration",
            0,
            listOf(0 to "Release", 1 to "Debug", 2 to "RelWithDebInfo", 3 to "MinSizeRel")
        ),
        BoolSpec(AutoSaveOnBuild, "Save before build", "Write every modified tab when run is pressed", true),
        BoolSpec(ConsoleAutoOpen, "Open console on build", "Reveal the build console automatically", true),
        IntSpec(ConsoleMaxLines, "Console history", "Lines kept in the build console", 2000, 200, 10000, 200),

        HeaderSpec("hdr_terminal", "Terminal"),
        BoolSpec(KeepTerminalAlive, "Keep session alive", "Leave the shell running when the terminal is closed", true),

        HeaderSpec("hdr_autosave", "Auto-save"),
        BoolSpec(AutoSaveEnabled, "Enable auto-save", "Automatically save modified files periodically", true),
        IntSpec(AutoSaveInterval, "Save interval", "Seconds between auto-saves", 30, 10, 300, 10, "s")
    )
}
