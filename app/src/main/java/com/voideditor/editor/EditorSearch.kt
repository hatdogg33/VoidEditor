package com.voideditor.editor

import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.EditorSearcher

object EditorSearch {

    fun search(
        editor: CodeEditor,
        query: String,
        caseSensitive: Boolean,
        wholeWord: Boolean,
        regex: Boolean
    ): Boolean {
        if (query.isEmpty()) {
            stop(editor)
            return false
        }
        val type = when {
            regex -> EditorSearcher.SearchOptions.TYPE_REGULAR_EXPRESSION
            wholeWord -> EditorSearcher.SearchOptions.TYPE_WHOLE_WORD
            else -> EditorSearcher.SearchOptions.TYPE_NORMAL
        }
        return runCatching {
            editor.searcher.search(
                query,
                EditorSearcher.SearchOptions(type, !caseSensitive)
            )
        }.isSuccess
    }

    fun stop(editor: CodeEditor) {
        runCatching { editor.searcher.stopSearch() }
    }

    fun next(editor: CodeEditor) {
        runCatching { editor.searcher.gotoNext() }
    }

    fun previous(editor: CodeEditor) {
        runCatching { editor.searcher.gotoPrevious() }
    }

    fun replaceCurrent(editor: CodeEditor, replacement: String) {
        runCatching { editor.searcher.replaceCurrentMatch(replacement) }
    }

    fun replaceAll(editor: CodeEditor, replacement: String) {
        runCatching { editor.searcher.replaceAll(replacement) }
    }

    fun matchCount(editor: CodeEditor): Int =
        runCatching { editor.searcher.matchedPositionCount }.getOrDefault(0)

    fun currentIndex(editor: CodeEditor): Int =
        runCatching { editor.searcher.currentMatchedPositionIndex + 1 }
            .getOrDefault(0)
            .coerceAtLeast(0)

    fun format(editor: CodeEditor): Boolean =
        runCatching { editor.formatCodeAsync() }.getOrDefault(false)
}
