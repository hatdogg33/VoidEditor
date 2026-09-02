package com.voideditor.ui.lsp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R
import com.voideditor.lsp.LocationEntry
import com.voideditor.lsp.SymbolEntry

private val PanelBackground = Color(0xFF08202A)
private val Accent = Color(0xFF02F5A1)
private val TextPrimary = Color(0xFFDDF5EA)
private val TextSecondary = Color(0xFF6E9184)
private val DividerColor = Color(0xFF11333F)

@Composable
fun SymbolListPanel(
    title: String,
    symbols: List<SymbolEntry>,
    onSelect: (SymbolEntry) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Panel(title = title, count = symbols.size, onClose = onClose, modifier = modifier) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(symbols) { index, symbol ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(symbol) }
                        .padding(
                            start = (12 + symbol.depth * 14).dp,
                            end = 12.dp,
                            top = 7.dp,
                            bottom = 7.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = symbol.name,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (symbol.detail.isNotEmpty() || symbol.kind.isNotEmpty()) {
                            Text(
                                text = listOf(symbol.kind.lowercase(), symbol.detail)
                                    .filter { it.isNotEmpty() }
                                    .joinToString(" · "),
                                fontSize = 9.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        text = "${symbol.line + 1}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
                if (index < symbols.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DividerColor)
                    )
                }
            }
        }
    }
}

@Composable
fun LocationListPanel(
    title: String,
    locations: List<LocationEntry>,
    projectDir: java.io.File,
    onSelect: (LocationEntry) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Panel(title = title, count = locations.size, onClose = onClose, modifier = modifier) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(locations) { index, entry ->
                val relative = entry.file.absolutePath
                    .removePrefix(projectDir.absolutePath)
                    .trimStart('/')
                    .ifEmpty { entry.file.name }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(entry) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "$relative:${entry.line + 1}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.preview.isNotEmpty()) {
                        Text(
                            text = entry.preview,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (index < locations.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DividerColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun Panel(
    title: String,
    count: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = count.toString(),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Accent,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor)
        )
        content()
    }
}
