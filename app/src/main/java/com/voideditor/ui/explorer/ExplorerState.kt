package com.voideditor.ui.explorer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import java.io.File

data class TreeRow(
    val file: File,
    val depth: Int,
    val isDirectory: Boolean
)

class ExplorerState(private val projectDir: File) {

    val expanded = mutableStateSetOf(projectDir.absolutePath)

    var refreshKey by mutableIntStateOf(0)
        private set

    fun refresh() {
        refreshKey++
    }

    fun toggle(file: File) {
        val path = file.absolutePath
        if (path in expanded) {
            expanded.remove(path)
        } else {
            expanded.add(path)
        }
    }

    fun expand(file: File) {
        expanded.add(file.absolutePath)
    }

    fun rows(): List<TreeRow> {
        val generation = refreshKey
        val out = mutableListOf<TreeRow>()
        appendRows(projectDir, 0, out, generation)
        return out
    }

    private fun appendRows(dir: File, depth: Int, out: MutableList<TreeRow>, generation: Int) {
        val children = dir.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: return
        for (child in children) {
            out.add(TreeRow(child, depth, child.isDirectory))
            if (child.isDirectory && child.absolutePath in expanded) {
                appendRows(child, depth + 1, out, generation)
            }
        }
    }
}
