package com.voideditor.editor

import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage

object EditorLanguageResolver {

    private const val CPP = "source.cpp"
    private const val C = "source.c"
    private const val KOTLIN = "source.kotlin"
    private const val JAVA = "source.java"
    private const val JSON = "source.json"
    private const val MARKDOWN = "text.html.markdown"

    fun resolve(fileName: String): Language {
        val lower = fileName.lowercase()
        val scope = when {
            lower.endsWith(".cpp") || lower.endsWith(".cc") || lower.endsWith(".cxx") ||
                lower.endsWith(".hpp") || lower.endsWith(".hh") || lower.endsWith(".hxx") ||
                lower.endsWith(".h") -> CPP
            lower.endsWith(".c") -> C
            lower.endsWith(".kt") -> KOTLIN
            lower.endsWith(".java") -> JAVA
            lower.endsWith(".json") -> JSON
            lower.endsWith(".md") || lower.endsWith(".mk") || lower == "cmakelists.txt" -> MARKDOWN
            else -> return EmptyLanguage()
        }
        return runCatching { TextMateLanguage.create(scope, true) }
            .getOrElse { EmptyLanguage() }
    }
}
