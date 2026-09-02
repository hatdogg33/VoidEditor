package com.voideditor.ui.inject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voideditor.R
import com.voideditor.patch.PatchPhase
import com.voideditor.ui.dialogs.DialogCard
import com.voideditor.ui.theme.DeepOnyx
import com.voideditor.ui.theme.VoidEditorPalette
import com.voideditor.ui.theme.SpringGreen

private val ConsoleBackground = Color(0xFF061519)

@Composable
fun PatchConsoleDialog(
    lines: List<String>,
    phase: PatchPhase,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onInstall: () -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.scrollToItem(lines.size - 1)
    }
    Dialog(onDismissRequest = { if (phase !is PatchPhase.Running) onDismiss() }) {
        DialogCard {
            Text(
                text = when (phase) {
                    is PatchPhase.Running -> stringResource(R.string.patching)
                    is PatchPhase.Done -> stringResource(R.string.patch_complete)
                    is PatchPhase.Cancelled -> stringResource(R.string.patch_cancelled)
                    is PatchPhase.Failed -> stringResource(R.string.patch_failed)
                    is PatchPhase.Idle -> stringResource(R.string.patch_console)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VoidEditorPalette.textPrimary
            )
            if (phase is PatchPhase.Failed) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phase.message,
                    fontSize = 12.sp,
                    color = Color(0xFFEF6767)
                )
            }
            if (phase is PatchPhase.Done) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.patch_complete),
                    fontSize = 12.sp,
                    color = VoidEditorPalette.teal
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ConsoleBackground)
                    .padding(12.dp)
            ) {
                items(lines.size) { index ->
                    Text(
                        text = lines[index],
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = VoidEditorPalette.textPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            when (phase) {
                is PatchPhase.Running -> {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text(text = stringResource(R.string.cancel), fontWeight = FontWeight.Bold)
                    }
                }
                is PatchPhase.Done -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text(text = stringResource(R.string.close), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onInstall,
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpringGreen,
                                contentColor = DeepOnyx
                            )
                        ) {
                            Text(text = stringResource(R.string.install), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpringGreen,
                            contentColor = DeepOnyx
                        )
                    ) {
                        Text(text = stringResource(R.string.close), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
