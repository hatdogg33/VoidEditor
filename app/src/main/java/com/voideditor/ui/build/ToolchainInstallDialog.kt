package com.voideditor.ui.build

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.voideditor.build.ToolchainInstaller
import com.voideditor.build.ToolchainKind
import com.voideditor.build.ToolchainPaths
import com.voideditor.build.ToolchainPhase
import com.voideditor.build.ToolchainRelease
import com.voideditor.build.ToolchainRepository
import com.voideditor.net.ResumableDownload
import com.voideditor.ui.theme.SpringGreen
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

private val CardSurface = Color(0xFF0C242D)
private val CardBorder = Color(0x3302F5A1)
private val FieldSurface = Color(0xFF0F2830)
private val Foreground = Color(0xFFD9F3E6)
private val Muted = Color(0xFF7FA898)
private val ErrorTint = Color(0xFFFF8A80)
private val WarnTint = Color(0xFFFFC46B)
private val CardShape = RoundedCornerShape(18.dp)
private val FieldShape = RoundedCornerShape(10.dp)

@Composable
fun ToolchainInstallDialog(onDismiss: () -> Unit, onReady: () -> Unit) {
    val context = LocalContext.current
    var ndkInstalled by remember { mutableStateOf(ToolchainPaths.isInstalled(context, ToolchainKind.Ndk)) }
    var cmakeInstalled by remember { mutableStateOf(ToolchainPaths.isInstalled(context, ToolchainKind.CMake)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(CardSurface)
                .border(1.dp, CardBorder, CardShape)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Build Tools",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SpringGreen
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Install the Android NDK and CMake toolchain to build this project.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Muted
            )

            Spacer(modifier = Modifier.height(18.dp))

            ToolchainSection(
                title = "Android NDK",
                kind = ToolchainKind.Ndk,
                installed = ndkInstalled,
                onInstalled = { ndkInstalled = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            ToolchainSection(
                title = "CMake",
                kind = ToolchainKind.CMake,
                installed = cmakeInstalled,
                onInstalled = { cmakeInstalled = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Close", fontSize = 12.sp)
                }
                if (ndkInstalled && cmakeInstalled) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = onReady) {
                        Text("Build", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolchainSection(
    title: String,
    kind: ToolchainKind,
    installed: Boolean,
    onInstalled: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var releases by remember { mutableStateOf<List<ToolchainRelease>>(emptyList()) }
    var selected by remember { mutableStateOf<ToolchainRelease?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var phase by remember { mutableStateOf<ToolchainPhase>(ToolchainPhase.Idle) }
    val installedVersion = remember(installed) { ToolchainPaths.installedVersion(context, kind) }

    LaunchedEffect(kind) {
        runCatching { ToolchainRepository.fetchReleases(kind) }
            .onSuccess { list ->
                releases = list
                selected = list.firstOrNull()
                loadError = if (list.isEmpty()) "No aarch64 build found" else null
            }
            .onFailure { loadError = it.message ?: "Unable to load versions" }
    }

    var installer by remember { mutableStateOf<ToolchainInstaller?>(null) }
    val busy = phase is ToolchainPhase.Downloading ||
        phase is ToolchainPhase.Extracting ||
        phase is ToolchainPhase.Retrying
    val partialMb = remember(phase, selected) {
        val part = ResumableDownload.partFile(ToolchainPaths.downloadCache(context, kind))
        if (part.isFile) part.length() / (1024.0 * 1024.0) else 0.0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Foreground,
                modifier = Modifier.weight(1f)
            )
            if (installed) {
                Icon(
                    painter = painterResource(R.drawable.select),
                    contentDescription = null,
                    tint = SpringGreen,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = installedVersion ?: "installed",
                    fontSize = 11.sp,
                    color = SpringGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FieldShape)
                    .background(FieldSurface)
                    .border(1.dp, CardBorder, FieldShape)
                    .clickable(enabled = !busy && releases.isNotEmpty()) { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected?.tag ?: loadError ?: "loading versions...",
                    fontSize = 12.sp,
                    color = if (selected != null) Foreground else Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                selected?.let {
                    Text(
                        text = "${"%.0f".format(it.sizeMb)} MB",
                        fontSize = 11.sp,
                        color = Muted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(
                    painter = painterResource(R.drawable.chevron_down),
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(FieldSurface)
                    .heightIn(max = 260.dp)
            ) {
                releases.forEach { release ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = release.tag,
                                    fontSize = 12.sp,
                                    color = Foreground,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${"%.0f".format(release.sizeMb)} MB",
                                    fontSize = 11.sp,
                                    color = Muted
                                )
                            }
                        },
                        onClick = {
                            selected = release
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (busy) {
            OutlinedButton(
                onClick = { installer?.cancel() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", fontSize = 12.sp)
            }
        } else {
            Button(
                onClick = {
                    val release = selected ?: return@Button
                    val runner = ToolchainInstaller(context, kind)
                    installer = runner
                    phase = ToolchainPhase.Downloading(0, 0.0, release.sizeMb)
                    scope.launch {
                        runner.install(release) { newPhase ->
                            phase = newPhase
                            if (newPhase is ToolchainPhase.Done) onInstalled()
                        }
                    }
                },
                enabled = selected != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpringGreen,
                    contentColor = Color(0xFF07191E)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.download),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = downloadLabel(installed, partialMb),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        PhaseIndicator(phase)
    }
}

private fun downloadLabel(installed: Boolean, partialMb: Double): String = when {
    partialMb >= 1.0 -> "Resume (${"%.0f".format(partialMb)} MB done)"
    installed -> "Reinstall"
    else -> "Download"
}

@Composable
private fun PhaseIndicator(phase: ToolchainPhase) {
    when (phase) {
        ToolchainPhase.Idle -> {
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = SpringGreen,
                strokeCap = StrokeCap.Round
            )
        }
        is ToolchainPhase.Downloading -> {
            LinearProgressIndicator(
                progress = { phase.percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = SpringGreen,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "${phase.percent}%  ·  ${"%.1f".format(phase.receivedMb)} / ${"%.1f".format(phase.totalMb)} MB",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Muted
            )
        }
        is ToolchainPhase.Extracting -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = SpringGreen,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "extracting: ${phase.entry}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${phase.count} files",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Muted
            )
        }
        ToolchainPhase.Done -> {
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = SpringGreen,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "installed", fontSize = 11.sp, color = SpringGreen)
        }
        is ToolchainPhase.Retrying -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = WarnTint,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "retry ${phase.attempt} · ${phase.reason}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = WarnTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "keeping ${"%.1f".format(phase.receivedMb)} MB already downloaded",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Muted
            )
        }
        ToolchainPhase.Cancelled -> {
            Text(
                text = "cancelled · progress kept, press resume",
                fontSize = 11.sp,
                color = WarnTint
            )
        }
        is ToolchainPhase.Failed -> {
            Text(text = phase.message, fontSize = 11.sp, color = ErrorTint)
        }
    }
}
