package com.voideditor.ui.inject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
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
import androidx.compose.ui.window.DialogProperties
import com.voideditor.R
import com.voideditor.ui.theme.VoidEditorPalette
import java.io.File

private val PickerShape = RoundedCornerShape(24.dp)

@Composable
fun StoragePickerDialog(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val root = remember { File("/storage/emulated/0") }
    var current by remember { mutableStateOf(root) }
    val entries = remember(current) {
        val list = current.listFiles { file ->
            (file.isDirectory && !file.isHidden) || (file.isFile && file.name.endsWith(".apk", true))
        }
        (list ?: emptyArray()).sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }
    val parent = current.parentFile
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(PickerShape)
                .background(VoidEditorPalette.abyss)
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.select_apk),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VoidEditorPalette.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = current.absolutePath,
                fontSize = 11.sp,
                color = VoidEditorPalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (parent != null && parent != current && parent.canRead()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { current = parent }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = VoidEditorPalette.teal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = parent.absolutePath,
                        fontSize = 12.sp,
                        color = VoidEditorPalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                items(entries, key = { it.absolutePath }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                when {
                                    entry.isDirectory -> Modifier.clickable { current = entry }
                                    entry.isFile -> Modifier.clickable { onPick(entry.absolutePath) }
                                    else -> Modifier
                                }
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (entry.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = VoidEditorPalette.teal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = entry.name,
                            fontSize = 13.sp,
                            color = VoidEditorPalette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.close),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = VoidEditorPalette.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onDismiss() }
                    .padding(vertical = 10.dp)
            )
        }
    }
}
