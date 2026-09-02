package com.voideditor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val SpringGreen = Color(0xFF02F5A1)
val DeepOnyx = Color(0xFF07191E)

@Immutable
data class EditorEsColors(
    val abyss: Color,
    val teal: Color,
    val mint: Color,
    val amber: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val buttonPrimaryBackground: Color,
    val buttonPrimaryContent: Color,
    val buttonSecondaryBackground: Color,
    val buttonSecondaryBorder: Color,
    val buttonSecondaryContent: Color
)

val VoidEditorPalette = EditorEsColors(
    abyss = DeepOnyx,
    teal = SpringGreen,
    mint = SpringGreen,
    amber = SpringGreen,
    textPrimary = Color(0xFFF2FFFA),
    textSecondary = Color(0xA6B8D9CC),
    buttonPrimaryBackground = SpringGreen,
    buttonPrimaryContent = DeepOnyx,
    buttonSecondaryBackground = Color(0x1402F5A1),
    buttonSecondaryBorder = Color(0x4002F5A1),
    buttonSecondaryContent = Color(0xFFEFFFF7)
)

private val EditorEsColorScheme = darkColorScheme(
    primary = SpringGreen,
    onPrimary = DeepOnyx,
    secondary = SpringGreen,
    tertiary = SpringGreen,
    background = DeepOnyx,
    surface = DeepOnyx,
    onBackground = Color(0xFFF2FFFA),
    onSurface = Color(0xFFF2FFFA)
)

private val EditorEsShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun VoidEditorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EditorEsColorScheme,
        shapes = EditorEsShapes,
        content = content
    )
}
