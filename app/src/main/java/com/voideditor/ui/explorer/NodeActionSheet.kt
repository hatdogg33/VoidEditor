package com.voideditor.ui.explorer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voideditor.R
import com.voideditor.ui.dialogs.DialogCard
import com.voideditor.ui.theme.VoidEditorPalette
import androidx.compose.ui.graphics.Color
import java.io.File

private val DangerColor = Color(0xFFEF6767)

@Composable
fun NodeActionSheet(
    target: File,
    onDismiss: () -> Unit,
    onAction: (NodeAction) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogCard {
            Text(
                text = target.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = VoidEditorPalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (target.isDirectory) {
                ActionRow(
                    label = stringResource(R.string.new_file),
                    iconRes = R.drawable.add,
                    onClick = { onAction(NodeAction.NewFile) }
                )
                ActionRow(
                    label = stringResource(R.string.new_folder),
                    iconRes = R.drawable.outline_folder,
                    onClick = { onAction(NodeAction.NewFolder) }
                )
            }
            ActionRow(
                label = stringResource(R.string.copy_path),
                iconRes = R.drawable.copy,
                onClick = { onAction(NodeAction.CopyPath) }
            )
            ActionRow(
                label = stringResource(R.string.rename),
                iconRes = R.drawable.edit,
                onClick = { onAction(NodeAction.Rename) }
            )
            ActionRow(
                label = stringResource(R.string.delete),
                iconRes = R.drawable.close,
                danger = true,
                onClick = { onAction(NodeAction.Delete) }
            )
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (danger) DangerColor else VoidEditorPalette.mint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = VoidEditorPalette.textPrimary
        )
    }
}
