package com.voideditor.ui.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voideditor.R
import com.voideditor.ui.components.VoidEditorButton
import com.voideditor.ui.theme.VoidEditorPalette

private val ErrorColor = Color(0xFFEF6767)

@Composable
fun NameInputDialog(
    title: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onSubmit: (String) -> String?
) {
    var value by remember { mutableStateOf(initialValue) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        DialogCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoidEditorPalette.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = stringResource(R.string.close),
                        tint = VoidEditorPalette.textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = value,
                onValueChange = {
                    value = it
                    error = null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error != null,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VoidEditorPalette.buttonSecondaryBackground,
                    unfocusedContainerColor = VoidEditorPalette.buttonSecondaryBackground,
                    focusedBorderColor = VoidEditorPalette.teal,
                    unfocusedBorderColor = VoidEditorPalette.buttonSecondaryBorder,
                    errorBorderColor = ErrorColor,
                    cursorColor = VoidEditorPalette.amber,
                    errorCursorColor = ErrorColor,
                    focusedTextColor = VoidEditorPalette.textPrimary,
                    unfocusedTextColor = VoidEditorPalette.textPrimary
                )
            )
            error?.let { message ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = message, fontSize = 13.sp, color = ErrorColor)
            }
            Spacer(modifier = Modifier.height(20.dp))
            VoidEditorButton(
                primary = true,
                label = stringResource(R.string.confirm),
                iconRes = R.drawable.select
            ) {
                val failure = onSubmit(value)
                if (failure != null) {
                    error = failure
                } else {
                    onDismiss()
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogCard {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VoidEditorPalette.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = VoidEditorPalette.textSecondary
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row {
                VoidEditorButton(
                    label = stringResource(R.string.cancel),
                    iconRes = R.drawable.close,
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss
                )
                Spacer(modifier = Modifier.width(10.dp))
                VoidEditorButton(
                    primary = true,
                    label = confirmLabel,
                    iconRes = R.drawable.select,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onConfirm()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun UnsavedChangesDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDontSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogCard {
            Text(
                text = stringResource(R.string.close),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VoidEditorPalette.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.unsaved_changes_message, fileName),
                fontSize = 14.sp,
                color = VoidEditorPalette.textSecondary
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row {
                VoidEditorButton(
                    label = stringResource(R.string.dont_save),
                    iconRes = R.drawable.close,
                    modifier = Modifier.weight(1f),
                    onClick = onDontSave
                )
                Spacer(modifier = Modifier.width(10.dp))
                VoidEditorButton(
                    primary = true,
                    label = stringResource(R.string.save),
                    iconRes = R.drawable.select,
                    modifier = Modifier.weight(1f),
                    onClick = onSave
                )
            }
        }
    }
}
