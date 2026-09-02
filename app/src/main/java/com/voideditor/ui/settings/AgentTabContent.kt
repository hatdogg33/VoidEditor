package com.voideditor.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.voideditor.agent.AgentCatalog
import com.voideditor.agent.AgentInstaller
import com.voideditor.agent.AgentStatus
import com.voideditor.proot.ProotConfig

@Composable
fun AgentTabContent(
    refreshKey: Int,
    onInstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val statuses = remember { mutableStateMapOf<String, AgentStatus>() }
    val ubuntuReady = remember(refreshKey) {
        ProotConfig.isInstalled(context) && ProotConfig.isAvailable(context)
    }

    LaunchedEffect(refreshKey, ubuntuReady) {
        if (!ubuntuReady) return@LaunchedEffect
        for (spec in AgentCatalog.agents) {
            statuses[spec.id] = AgentInstaller.detect(context, spec)
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        if (!ubuntuReady) {
            item(key = "notice") {
                AgentNotice(
                    text = "Ubuntu is not installed yet. Open the terminal once to install it, " +
                        "then agents can be installed from here."
                )
            }
        }
        items(AgentCatalog.agents, key = { it.id }) { spec ->
            AgentCard(
                spec = spec,
                status = statuses[spec.id] ?: AgentStatus.Unknown,
                onInstall = {
                    AgentInstaller.scriptFor(context, spec)
                    onInstall(spec.id)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
