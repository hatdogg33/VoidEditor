package com.voideditor.editor

import android.content.Context
import android.graphics.Typeface
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.eclipse.tm4e.core.registry.IThemeSource

/**
 * Every theme is a TextMate theme JSON loaded through ThemeRegistry.
 *
 * WHY: syntax highlighting for all languages runs through TextMateLanguage, whose
 * spans carry color ids >= 255. Only TextMateColorScheme can resolve those ids
 * (from the theme's token colors). Plain built-in schemes (SchemeDarcula etc.)
 * return 0 for them -> invisible text. Routing every theme through the same
 * TextMateColorScheme pipeline guarantees correct, visible colors everywhere and
 * gives each theme its own distinct token palette.
 */
object EditorTheme {

    const val THEME_TEXTMATE = "TEXTMATE"   // dark_plus.json  (default)
    const val THEME_DRACULA = "DRACULA"     // dracula.json
    const val THEME_DARCOLA = "DARCOLA"     // darcula.json
    const val THEME_VS2019 = "VS2019"       // vs2019.json
    const val THEME_GITHUB = "GITHUB"       // github.json

    private val loadedThemes = mutableSetOf<String>()

    private fun assetThemeFile(themeName: String): String = when (themeName) {
        THEME_DRACULA -> "textmate/dracula.json"
        THEME_DARCOLA -> "textmate/darcula.json"
        THEME_VS2019 -> "textmate/vs2019.json"
        THEME_GITHUB -> "textmate/github.json"
        else -> "textmate/dark_plus.json"
    }

    private fun registryThemeName(themeName: String): String = when (themeName) {
        THEME_DRACULA -> "dracula"
        THEME_DARCOLA -> "Darcula"
        THEME_VS2019 -> "VS2019 Dark"
        THEME_GITHUB -> "GitHub Dark"
        else -> "dark_plus"
    }

    /** Load the theme JSON into ThemeRegistry once (idempotent per process). */
    fun ensureThemeLoaded(themeName: String) {
        val registryName = registryThemeName(themeName)
        if (registryName in loadedThemes) return
        runCatching {
            val registry = ThemeRegistry.getInstance()
            val source = IThemeSource.fromInputStream(
                FileProviderRegistry.getInstance().tryGetInputStream(assetThemeFile(themeName)),
                assetThemeFile(themeName),
                null
            )
            registry.loadTheme(ThemeModel(source, registryName).apply { isDark = true })
            loadedThemes.add(registryName)
        }
    }

    /**
     * Apply a theme by its EditorThemeName constant.
     * Always succeeds visually: falls back to dark_plus if a theme fails to load.
     */
    fun applyTheme(editor: CodeEditor, themeName: String, context: Context? = null) {
        ensureThemeLoaded(themeName)
        val ok = runCatching {
            val registry = ThemeRegistry.getInstance()
            registry.setTheme(registryThemeName(themeName))
            editor.colorScheme = TextMateColorScheme.create(registry)
        }.isSuccess
        if (!ok) {
            // Hard fallback: re-select dark_plus (guaranteed present from TextMateSetup)
            runCatching {
                val registry = ThemeRegistry.getInstance()
                registry.setTheme("dark_plus")
                editor.colorScheme = TextMateColorScheme.create(registry)
            }
        }
        context ?: return
        runCatching {
            editor.typefaceText = Typeface.createFromAsset(context.assets, "fonts/JetBrainsMono-Regular.ttf")
        }
    }

    /** Original entry point: default dark_plus theme + JetBrains Mono font. */
    fun apply(editor: CodeEditor, context: Context) {
        applyTheme(editor, THEME_TEXTMATE, context)
    }

    fun applyTextMate(editor: CodeEditor) = applyTheme(editor, THEME_TEXTMATE)

    fun applyDracula(editor: CodeEditor) = applyTheme(editor, THEME_DRACULA)

    fun applyDarcula(editor: CodeEditor) = applyTheme(editor, THEME_DARCOLA)

    fun applyVS2019(editor: CodeEditor) = applyTheme(editor, THEME_VS2019)

    fun applyGitHub(editor: CodeEditor) = applyTheme(editor, THEME_GITHUB)
}
