package com.voideditor.ui.git

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.viewmodel.GitLogEntry
import com.voideditor.viewmodel.GitStatus

private val GitBackground = Color(0xFF0A222B)
private val GitText = Color(0xFFF2FFFA)
private val GitMuted = Color(0xFF7FA898)
private val GitBranch = Color(0xFF02F5A1)
private val GitModified = Color(0xFFE8B44A)
private val GitStaged = Color(0xFF4AE84A)
private val GitUntracked = Color(0xFF7FA898)
private val GitCommitHash = Color(0xFF02F5A1).copy(alpha = 0.7f)

@Composable
fun GitStatusPanel(
    status: GitStatus,
    onCommitClick: () -> Unit,
    onInitClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GitBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Git", color = GitText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(status.branch, color = GitBranch, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }

            Row {
                IconButton(onClick = onRefreshClick, modifier = Modifier.size(28.dp)) {
                    Text("↻", color = GitMuted, fontSize = 16.sp)
                }
                if (status.isClean) {
                    IconButton(onClick = onCommitClick, modifier = Modifier.size(28.dp)) {
                        Text("+", color = GitBranch, fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!status.isClean) {
            status.stagedFiles.forEach { file ->
                GitFileRow(file, "staged", GitStaged)
            }
            status.modifiedFiles.forEach { file ->
                GitFileRow(file, "modified", GitModified)
            }
            status.untrackedFiles.forEach { file ->
                GitFileRow(file, "untracked", GitUntracked)
            }
        } else {
            Text(
                "Working tree clean",
                color = GitMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun GitFileRow(
    file: String,
    status: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            status.first().uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .padding(2.dp),
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            file,
            color = GitText,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GitLogPanel(
    log: List<GitLogEntry>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GitBackground)
            .padding(12.dp)
    ) {
        Text(
            "Commit History",
            color = GitText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (log.isEmpty()) {
            Text(
                "No commits yet",
                color = GitMuted,
                fontSize = 12.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp)
            ) {
                items(log) { entry ->
                    GitLogItem(entry)
                }
            }
        }
    }
}

@Composable
private fun GitLogItem(entry: GitLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            entry.hash.take(7),
            color = GitCommitHash,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.message,
                color = GitText,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (entry.author.isNotBlank()) {
                Text(
                    entry.author,
                    color = GitMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun GitCommitDialog(
    onCommit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var message = androidx.compose.runtime.mutableStateOf("")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GitBackground)
            .padding(16.dp)
    ) {
        Text(
            "Commit Changes",
            color = GitText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        androidx.compose.foundation.text.BasicTextField(
            value = message.value,
            onValueChange = { message.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0E2A33))
                .padding(12.dp),
            textStyle = TextStyle(
                color = GitText,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            ),
            decorationBox = { innerTextField ->
                if (message.value.isEmpty()) {
                    Text("Commit message...", color = GitMuted, fontSize = 14.sp)
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "Cancel",
                color = GitMuted,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Commit",
                color = GitBranch,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        if (message.value.isNotBlank()) {
                            onCommit(message.value)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
