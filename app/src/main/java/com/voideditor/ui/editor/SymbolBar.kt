package com.voideditor.ui.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rosemoe.sora.widget.CodeEditor

private val SymbolBarBackground = Color(0xFF0A222B)
private val SymbolForeground = Color(0xFFD9F3E6)
private val SymbolDivider = Color(0x2E02F5A1)

private data class SymbolKey(val label: String, val insert: String, val offset: Int)

private val FirstRowSymbols = listOf(
    SymbolKey("→", "", 0),
    SymbolKey("{", "{}", 1),
    SymbolKey("}", "}", 1),
    SymbolKey("(", "()", 1),
    SymbolKey(")", ")", 1),
    SymbolKey("[", "[]", 1),
    SymbolKey("]", "]", 1),
    SymbolKey("\"", "\"\"", 1),
    SymbolKey("'", "''", 1),
    SymbolKey(";", ";", 1)
)

private val SecondRowSymbols = listOf(
    SymbolKey(",", ",", 1),
    SymbolKey(".", ".", 1),
    SymbolKey(":", ":", 1),
    SymbolKey("+", "+", 1),
    SymbolKey("-", "-", 1),
    SymbolKey("/", "/", 1),
    SymbolKey("=", "=", 1),
    SymbolKey("<", "<", 1),
    SymbolKey(">", ">", 1)
)

@Composable
fun SymbolBar(editor: CodeEditor?) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SymbolBarBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SymbolDivider)
        )
        SymbolRow(symbols = FirstRowSymbols, scrollState = scrollState, editor = editor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SymbolDivider)
        )
        SymbolRow(symbols = SecondRowSymbols, scrollState = scrollState, editor = editor)
    }
}

@Composable
private fun SymbolRow(
    symbols: List<SymbolKey>,
    scrollState: ScrollState,
    editor: CodeEditor?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        symbols.forEach { symbol ->
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .fillMaxHeight()
                    .clickable { insertSymbol(editor, symbol) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SymbolForeground
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

private fun insertSymbol(editor: CodeEditor?, symbol: SymbolKey) {
    if (editor == null || !editor.isEditable) return
    if (symbol.label == "→") {
        if (editor.snippetController.isInSnippet()) {
            editor.snippetController.shiftToNextTabStop()
        } else {
            editor.indentOrCommitTab()
        }
    } else {
        editor.insertText(symbol.insert, symbol.offset)
    }
}
