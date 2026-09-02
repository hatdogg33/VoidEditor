package com.voideditor.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R
import com.voideditor.data.ProjectCreator
import com.voideditor.ui.components.VoidEditorButton
import com.voideditor.ui.theme.VoidEditorPalette
import java.io.File

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val SheetSurface = Color(0xFF0A222B)
private val SelectedBackground = Color(0x2902F5A1)
private val ItemShape = RoundedCornerShape(12.dp)
private val HandleColor = Color(0x4D02F5A1)

@Composable
fun OpenProjectSheet(
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit
) {
    val projects = remember {
        ProjectCreator.baseDir()
            .listFiles { file -> file.isDirectory }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }
    var selected by remember { mutableStateOf<String?>(null) }
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }
    BackHandler { onDismiss() }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f * entrance.value))
                .clickable(onClick = onDismiss)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = (1f - entrance.value) * 280f
                    alpha = entrance.value
                }
                .clip(SheetShape)
                .background(SheetSurface)
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(HandleColor)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.open_project),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VoidEditorPalette.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ProjectCreator.baseDir().absolutePath,
                fontSize = 13.sp,
                color = VoidEditorPalette.textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (projects.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_projects),
                    fontSize = 14.sp,
                    color = VoidEditorPalette.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    items(projects, key = { it.absolutePath }) { project ->
                        ProjectRow(
                            project = project,
                            selected = selected == project.absolutePath,
                            onClick = { selected = project.absolutePath }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
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
                    label = stringResource(R.string.ok),
                    iconRes = R.drawable.select,
                    modifier = Modifier.weight(1f),
                    enabled = selected != null,
                    onClick = { selected?.let(onOpen) }
                )
            }
        }
    }
}

@Composable
private fun ProjectRow(
    project: File,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(ItemShape)
            .background(if (selected) SelectedBackground else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.folder_code),
            contentDescription = null,
            tint = VoidEditorPalette.mint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = project.name,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = VoidEditorPalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.select),
                contentDescription = null,
                tint = VoidEditorPalette.mint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
