package com.voideditor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R

private val BarBackground = Color(0xFF0A222B)
private val Accent = Color(0xFF02F5A1)
private val TextPrimary = Color(0xFFDDF5EA)
private val TextSecondary = Color(0xFF6E9184)
private val FieldBackground = Color(0xFF0E2B35)
private val DividerColor = Color(0xFF11333F)

data class FindState(
    val query: String = "",
    val replacement: String = "",
    val caseSensitive: Boolean = false,
    val wholeWord: Boolean = false,
    val regex: Boolean = false,
    val showReplace: Boolean = false,
    val current: Int = 0,
    val total: Int = 0
)

@Composable
fun FindReplaceBar(
    state: FindState,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onReplacementChange: (String) -> Unit,
    onToggleOption: (String) -> Unit,
    onToggleReplace: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onReplace: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BarBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchField(
                value = state.query,
                placeholder = "Find",
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (state.total == 0) "0/0" else "${state.current}/${state.total}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (state.total == 0) TextSecondary else Accent
            )
            IconButton(onClick = onPrevious, modifier = Modifier.size(34.dp)) {
                Icon(
                    painter = painterResource(R.drawable.chevron_up),
                    contentDescription = "Previous",
                    tint = TextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(34.dp)) {
                Icon(
                    painter = painterResource(R.drawable.chevron_down),
                    contentDescription = "Next",
                    tint = TextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(34.dp)) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Chip("Aa", state.caseSensitive) { onToggleOption("case") }
            Chip("W", state.wholeWord) { onToggleOption("word") }
            Chip(".*", state.regex) { onToggleOption("regex") }
            Spacer(modifier = Modifier.weight(1f))
            Chip(if (state.showReplace) "Hide replace" else "Replace", state.showReplace) {
                onToggleReplace()
            }
        }

        if (state.showReplace) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SearchField(
                    value = state.replacement,
                    placeholder = "Replace with",
                    onValueChange = onReplacementChange,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Chip("One", false, onClick = onReplace)
                Spacer(modifier = Modifier.width(6.dp))
                Chip("All", false, onClick = onReplaceAll)
            }
        }
    }
}

@Composable
private fun Chip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (active) Accent else DividerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) Color(0xFF07191E) else TextPrimary
        )
    }
}

@Composable
private fun SearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(48.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary
        ),
        placeholder = {
            Text(text = placeholder, fontSize = 12.sp, color = TextSecondary)
        },
        shape = RoundedCornerShape(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FieldBackground,
            unfocusedContainerColor = FieldBackground,
            focusedBorderColor = Accent,
            unfocusedBorderColor = DividerColor,
            cursorColor = Accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}
