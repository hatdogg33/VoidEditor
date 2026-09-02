package com.voideditor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FormatIndentIncrease
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R

private val MenuBackground = Color(0xFF0A222B)
private val TextPrimary = Color(0xFFDDF5EA)
private val IconTint = Color(0xFF6E9184)

@Composable
fun EditorToolsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onFormat: () -> Unit,
    onSymbols: () -> Unit,
    onDefinition: () -> Unit,
    onReferences: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(MenuBackground)
    ) {
        Entry("Format code", R.drawable.auto_fix, onFormat)
        Entry("Document symbols", R.drawable.jump_to_element, onSymbols)
        Entry("Go to definition", R.drawable.arrow_outward, onDefinition)
        Entry("Find references", R.drawable.manage_search, onReferences)
    }
}

@Composable
private fun Entry(
    label: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = label, fontSize = 13.sp, color = TextPrimary) },
        leadingIcon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = IconTint,
                modifier = Modifier.size(17.dp)
            )
        },
        onClick = onClick
    )
}
