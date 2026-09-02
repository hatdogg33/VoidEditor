package com.voideditor.ui.explorer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R
import com.voideditor.ui.icons.XedIcons
import com.voideditor.ui.theme.SpringGreen
import java.io.File

private val SidebarBackground = Color(0xFF0A222B)
private val SectionForeground = Color(0xFF6FD9AE)
private val ItemForeground = Color(0xFFDDF5EA)
private val MutedForeground = Color(0xFF6E9184)
private val ActiveRowBackground = Color(0x2902F5A1)
private val GuideColor = Color(0x2E02F5A1)
private val FolderTint = Color(0xFF66C6A6)
private val FileTint = Color(0xFF9BC4B4)
private val ChevronColor = Color(0xFFB7E9D3)

enum class NodeAction {
    NewFile,
    NewFolder,
    CopyPath,
    Rename,
    Delete
}

@Composable
fun ExplorerDrawerContent(
    projectDir: File,
    explorer: ExplorerState,
    activeFilePath: String?,
    onFileClick: (File) -> Unit,
    onMenuRequested: (File) -> Unit,
    onQuickAction: (NodeAction, File) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenInject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(SidebarBackground)
            .padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.explorer_title).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
                color = SectionForeground,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { onQuickAction(NodeAction.NewFile, projectDir) },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = stringResource(R.string.new_file),
                    tint = SectionForeground,
                    modifier = Modifier.size(17.dp)
                )
            }
            IconButton(
                onClick = { onQuickAction(NodeAction.NewFolder, projectDir) },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_folder),
                    contentDescription = stringResource(R.string.new_folder),
                    tint = SectionForeground,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.folder_code),
                contentDescription = null,
                tint = SpringGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = projectDir.name.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ItemForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = projectDir.absolutePath,
                    fontSize = 10.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        val rows = explorer.rows()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(rows, key = { it.file.absolutePath + ":" + it.file.lastModified() }) { row ->
                TreeRowItem(
                    row = row,
                    expanded = row.file.absolutePath in explorer.expanded,
                    isActive = row.file.absolutePath == activeFilePath,
                    onClick = {
                        if (row.isDirectory) explorer.toggle(row.file) else onFileClick(row.file)
                    },
                    onMenu = { onMenuRequested(row.file) }
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GuideColor)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterAction(
                label = stringResource(R.string.terminal),
                iconRes = R.drawable.terminal,
                onClick = onOpenTerminal,
                modifier = Modifier.weight(1f)
            )
            FooterAction(
                label = stringResource(R.string.inject),
                icon = Icons.Filled.Vaccines,
                onClick = onOpenInject,
                modifier = Modifier.weight(1f)
            )
            FooterAction(
                label = stringResource(R.string.settings),
                iconRes = R.drawable.settings,
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FooterAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SpringGreen,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = ItemForeground,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible
        )
    }
}

@Composable
private fun FooterAction(
    label: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = SpringGreen,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = ItemForeground,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TreeRowItem(
    row: TreeRow,
    expanded: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    onMenu: () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "chevron"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(if (isActive) ActiveRowBackground else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onMenu)
            .padding(start = 8.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(row.depth) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(GuideColor)
                )
            }
        }
        if (row.isDirectory) {
            Icon(
                painter = painterResource(R.drawable.chevron_right),
                contentDescription = null,
                tint = ChevronColor,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = chevronRotation }
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                painter = painterResource(
                    if (expanded) R.drawable.folder_managed else R.drawable.folder
                ),
                contentDescription = null,
                tint = FolderTint,
                modifier = Modifier.size(17.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(18.dp))
            Icon(
                painter = painterResource(XedIcons.fileType(row.file.name)),
                contentDescription = null,
                tint = FileTint,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = row.file.name,
            fontSize = 13.sp,
            color = if (isActive) Color.White else ItemForeground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onMenu, modifier = Modifier.size(26.dp)) {
            Icon(
                painter = painterResource(R.drawable.drag_indicator),
                contentDescription = null,
                tint = MutedForeground,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
