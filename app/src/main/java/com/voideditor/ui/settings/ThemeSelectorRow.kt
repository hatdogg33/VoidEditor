package com.voideditor.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.data.AppSettings
import com.voideditor.editor.EditorThemeName
import com.voideditor.ui.theme.SpringGreen

private val CardBackground = androidx.compose.ui.graphics.Color(0xFF0A222B)
private val TextPrimary = androidx.compose.ui.graphics.Color(0xFFDDF5EA)
private val TextSecondary = androidx.compose.ui.graphics.Color(0xFF6E9184)

@Composable
fun ThemeSelectorRow(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var current by remember { mutableStateOf(EditorThemeName.current()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(
            text = "EDITOR THEME",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFF0E2B35))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = current.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(text = "▾", color = TextSecondary, fontSize = 13.sp)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                EditorThemeName.entries.forEach { theme ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (theme == current) "● ${theme.displayName}" else theme.displayName,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            EditorThemeName.save(theme)
                            current = theme
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = current.description,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Applies to newly opened tabs",
            fontSize = 10.sp,
            color = SpringGreen
        )
    }
}
