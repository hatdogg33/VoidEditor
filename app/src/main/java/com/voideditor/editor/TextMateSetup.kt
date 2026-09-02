package com.voideditor.editor

import android.content.Context
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import org.eclipse.tm4e.core.registry.IThemeSource
import java.util.concurrent.atomic.AtomicBoolean

object TextMateSetup {

    private val initialized = AtomicBoolean(false)

    fun ensureInitialized(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        runCatching {
            FileProviderRegistry.getInstance().addFileProvider(
                AssetsFileResolver(context.applicationContext.assets)
            )
            val themeRegistry = ThemeRegistry.getInstance()
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream("textmate/dark_plus.json"),
                        "textmate/dark_plus.json",
                        null
                    ),
                    "dark_plus"
                ).apply { isDark = true }
            )
            themeRegistry.setTheme("dark_plus")
            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
        }
    }
}
