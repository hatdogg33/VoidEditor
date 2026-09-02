package com.voideditor.ui.inject

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voideditor.R
import com.voideditor.patch.ApkPatcher
import com.voideditor.patch.PatchLibVariant
import com.voideditor.ui.dialogs.DialogCard
import com.voideditor.ui.theme.DeepOnyx
import com.voideditor.ui.theme.VoidEditorPalette
import com.voideditor.ui.theme.SpringGreen
import java.io.File

private val FieldShape = RoundedCornerShape(12.dp)
private val FieldBackground = Color(0x1402F5A1)

@Composable
fun InjectDialog(
    projectDir: File,
    onDismiss: () -> Unit,
    onPatch: (String, PatchLibVariant) -> Unit
) {
    var apkPath by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val libraries = remember(projectDir) { ApkPatcher.scanLibraries(projectDir) }
    var selected by remember { mutableStateOf<PatchLibVariant?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        DialogCard {
            Text(
                text = stringResource(R.string.inject_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VoidEditorPalette.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.apk_path),
                fontSize = 12.sp,
                color = VoidEditorPalette.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = apkPath,
                    onValueChange = { apkPath = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "/storage/emulated/0/target.apk",
                            fontSize = 12.sp,
                            color = VoidEditorPalette.textSecondary
                        )
                    },
                    shape = FieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpringGreen,
                        unfocusedBorderColor = Color(0x4002F5A1),
                        cursorColor = SpringGreen,
                        focusedTextColor = VoidEditorPalette.textPrimary,
                        unfocusedTextColor = VoidEditorPalette.textPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showPicker = true }) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = SpringGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.select_library),
                fontSize = 12.sp,
                color = VoidEditorPalette.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(FieldShape)
                        .background(FieldBackground)
                        .clickable { expanded = true }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selected?.let { it.name + " (" + it.abi + ")" } ?: stringResource(R.string.no_libraries),
                        fontSize = 13.sp,
                        color = if (selected != null) VoidEditorPalette.textPrimary else VoidEditorPalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = VoidEditorPalette.textSecondary
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (libraries.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.no_libraries), fontSize = 13.sp) },
                            onClick = { expanded = false }
                        )
                    }
                    for (variant in libraries) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = variant.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = variant.abi,
                                        fontSize = 10.sp,
                                        color = VoidEditorPalette.textSecondary
                                    )
                                }
                            },
                            onClick = {
                                selected = variant
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onPatch(apkPath.trim(), selected ?: return@Button) },
                enabled = apkPath.isNotBlank() && selected != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = FieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpringGreen,
                    contentColor = DeepOnyx
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.patch),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    if (showPicker) {
        StoragePickerDialog(
            onDismiss = { showPicker = false },
            onPick = {
                apkPath = it
                showPicker = false
            }
        )
    }
}
