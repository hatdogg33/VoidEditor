package com.voideditor.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.voideditor.ui.theme.VoidEditorPalette

private val CardShape = RoundedCornerShape(28.dp)

@Composable
fun DialogCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(VoidEditorPalette.abyss)
            .border(1.dp, Color(0x3302F5A1), CardShape)
            .padding(24.dp)
            .graphicsLayer {
                alpha = entrance.value
                scaleX = 0.9f + 0.1f * entrance.value
                scaleY = 0.9f + 0.1f * entrance.value
                translationY = (1f - entrance.value) * 24f
            },
        content = content
    )
}
