package com.voideditor.ui.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R
import com.voideditor.agent.AgentSpec
import com.voideditor.agent.AgentStatus

private val CardBackground = Color(0xFF0C242D)
private val CardBorder = Color(0x2602F5A1)
private val IconContainerBg = Color(0x1F133E4A)
private val TextPrimary = Color(0xFFDDF5EA)
private val TextSecondary = Color(0xFF6E9184)
private val Accent = Color(0xFF02F5A1)
private val InstalledColor = Color(0xFF6FD9AE)
private val FailedColor = Color(0xFFFF6B6B)
private val DocButtonTint = Color(0xFF8BAAA0)
private val CardShape = RoundedCornerShape(14.dp)
private val ButtonShape = RoundedCornerShape(10.dp)

@Composable
fun AgentCard(
    spec: AgentSpec,
    status: AgentStatus,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(CardShape)
            .background(CardBackground)
            .border(1.dp, CardBorder, CardShape)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Agent Brand Icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(IconContainerBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(spec.iconRes),
                contentDescription = spec.name,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = spec.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (spec.docUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { runCatching { uriHandler.openUri(spec.docUrl) } },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_outward),
                            contentDescription = "Docs",
                            tint = DocButtonTint,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = spec.subtitle,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Action Button
        StatusButton(status = status, onClick = onInstall)
    }
}

@Composable
private fun StatusButton(status: AgentStatus, onClick: () -> Unit) {
    val label = when (status) {
        AgentStatus.Installed -> "Installed"
        AgentStatus.Installing -> "Installing"
        AgentStatus.Failed -> "Retry"
        AgentStatus.NotInstalled -> "Install"
        AgentStatus.Unknown -> "Install"
    }
    val labelColor = when (status) {
        AgentStatus.Installed -> InstalledColor
        AgentStatus.Failed -> FailedColor
        else -> Color(0xFF07191E)
    }
    val background = when (status) {
        AgentStatus.Installed -> Color(0x1F02F5A1)
        AgentStatus.Failed -> Color(0x1FFF6B6B)
        AgentStatus.Installing -> Color(0x1402F5A1)
        else -> Accent
    }
    val clickable = status != AgentStatus.Installed && status != AgentStatus.Installing

    Row(
        modifier = Modifier
            .width(88.dp)
            .height(34.dp)
            .clip(ButtonShape)
            .background(background)
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            AgentStatus.Installing -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = Accent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            AgentStatus.Installed -> {
                Icon(
                    painter = painterResource(R.drawable.select),
                    contentDescription = null,
                    tint = InstalledColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            AgentStatus.Failed -> {
                Icon(
                    painter = painterResource(R.drawable.restart),
                    contentDescription = null,
                    tint = FailedColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            else -> Unit
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
    }
}
@Composable
fun AgentNotice(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(CardShape)
            .background(Color(0x14FFB74D))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Color(0xFFFFB74D)
        )
    }
}
