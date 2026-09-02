package com.voideditor.editor

import io.github.rosemoe.sora.widget.CodeEditor

/**
 * Editor color themes. Names are persisted via AppSettings (key "editor_theme").
 * All themes are TextMate theme JSONs - see EditorTheme.applyTheme.
 */
enum class EditorThemeName(
    val displayName: String,
    val description: String
) {
    TEXTMATE("TextMate (VS Code)", "EditorEs green-on-teal dark"),
    DRACULA("Dracula", "Pink / purple / cyan on #282a36"),
    DARCOLA("Darcula", "Android Studio: orange keywords, amber functions"),
    VS2019("VS 2019 Dark", "Blue keywords, teal types, orange strings"),
    GITHUB("GitHub Dark", "Red keywords, purple functions on #0d1117");

    fun apply(editor: CodeEditor) {
        EditorTheme.applyTheme(editor, name)
    }

    companion object {
        const val PREF_KEY = "editor_theme"
        val DEFAULT = TEXTMATE

        fun fromName(name: String?): EditorThemeName =
            entries.firstOrNull { it.name == name } ?: DEFAULT

        fun current(): EditorThemeName = fromName(
            com.voideditor.data.AppSettings.string(PREF_KEY, DEFAULT.name)
        )

        fun save(theme: EditorThemeName) {
            com.voideditor.data.AppSettings.putString(PREF_KEY, theme.name)
        }
    }
}
