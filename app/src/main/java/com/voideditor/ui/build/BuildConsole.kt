package com.voideditor.ui.build

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R
import androidx.compose.foundation.text.selection.SelectionContainer
import com.voideditor.ui.theme.SpringGreen

private val ConsoleBackground = Color(0xFF071A20)
private val ConsoleHeader = Color(0xFF0E2A33)
private val ConsoleBorder = Color(0x3302F5A1)
private val ConsoleForeground = Color(0xFFD4EFE2)
private val ErrorForeground = Color(0xFFFF8A80)
private val SuccessForeground = SpringGreen
private val ConsoleShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)

data class ConsoleLine(val text: String, val kind: ConsoleLineKind)

enum class ConsoleLineKind {
    Normal,
    Error,
    Success
}

@Composable
fun BuildConsole(
    lines: List<ConsoleLine>,
    running: Boolean,
    maximized: Boolean,
    onToggleMaximize: () -> Unit,
    onCopy: () -> Unit,
    onStop: () -> Unit,

    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ConsoleShape)
            .background(ConsoleBackground)
            .border(1.dp, ConsoleBorder, ConsoleShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ConsoleHeader)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BUILD",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = SpringGreen,
                modifier = Modifier.width(58.dp)
            )
            Text(
                text = if (running) "running" else "idle",
                fontSize = 11.sp,
                color = ConsoleForeground.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            if (running) {
                IconButton(onClick = onStop, modifier = Modifier.size(30.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.stop),
                        contentDescription = "Stop build",
                        tint = ErrorForeground,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "Copy output",
                    tint = ConsoleForeground,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onToggleMaximize, modifier = Modifier.size(30.dp)) {
                Icon(
                    painter = painterResource(
                        if (maximized) R.drawable.chevron_down else R.drawable.chevron_up
                    ),
                    contentDescription = "Toggle size",
                    tint = ConsoleForeground,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Close console",
                    tint = ConsoleForeground,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        if (running) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = SpringGreen,
                strokeCap = StrokeCap.Square
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (lines.isEmpty()) {
                Text(
                    text = "no output yet",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ConsoleForeground.copy(alpha = 0.45f),
                    modifier = Modifier.padding(14.dp)
                )
            } else {
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        items(lines) { line ->
                            Text(
                                text = line.text,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                color = when (line.kind) {
                                    ConsoleLineKind.Error -> ErrorForeground
                                    ConsoleLineKind.Success -> SuccessForeground
                                    ConsoleLineKind.Normal -> ConsoleForeground
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

