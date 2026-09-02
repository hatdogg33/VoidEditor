package com.voideditor.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voideditor.R
import com.voideditor.data.AppSettings
import com.voideditor.data.BoolSpec
import com.voideditor.data.ChoiceSpec
import com.voideditor.data.EditorSettings
import com.voideditor.data.FloatSpec
import com.voideditor.data.HeaderSpec
import com.voideditor.data.IntSpec
import com.voideditor.data.PreferenceSettings
import com.voideditor.data.SettingSpec
import kotlin.math.roundToInt

private val Background = Color(0xFF07191E)
private val CardBackground = Color(0xFF0A222B)
private val Accent = Color(0xFF02F5A1)
private val TextPrimary = Color(0xFFDDF5EA)
private val TextSecondary = Color(0xFF6E9184)
private val HeaderColor = Color(0xFF6FD9AE)
private val Divider = Color(0xFF11333F)
private val TabInactive = Color(0xFF0E2B35)

private enum class SettingsTab(val label: String) {
    Preference("Preference"),
    Editor("Editor"),
    Agent("Agent")
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onInstallAgent: (String) -> Unit = {}) {
    var tab by remember { mutableStateOf(SettingsTab.Preference) }
    val specs = when (tab) {
        SettingsTab.Preference -> PreferenceSettings.specs
        SettingsTab.Editor -> EditorSettings.specs
        SettingsTab.Agent -> emptyList()
    }
    var revision by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.chevron_left),
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (tab != SettingsTab.Agent) {
                IconButton(
                    onClick = {
                        AppSettings.resetAll(specs)
                        revision++
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.restart),
                        contentDescription = "Reset",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (entry in SettingsTab.entries) {
                TabChip(
                    label = entry.label,
                    selected = entry == tab,
                    onClick = {
                        tab = entry
                        revision++
                    }
                )
            }
        }

        if (tab == SettingsTab.Agent) {
            AgentTabContent(
                refreshKey = revision,
                onInstall = onInstallAgent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp
                )
            ) {
                items(specs, key = { it.key }) { spec ->
                    SettingRow(spec = spec, revision = revision)
                }
                if (tab == SettingsTab.Editor) {
                    item(key = "editor_theme") {
                        ThemeSelectorRow()
                    }
                }
            }
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Accent else TabInactive)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color(0xFF07191E) else TextSecondary
        )
    }
}

@Composable
private fun SettingRow(spec: SettingSpec, revision: Int) {
    when (spec) {
        is HeaderSpec -> SectionHeader(spec.title)
        is BoolSpec -> BoolRow(spec, revision)
        is IntSpec -> IntRow(spec, revision)
        is FloatSpec -> FloatRow(spec, revision)
        is ChoiceSpec -> ChoiceRow(spec, revision)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = HeaderColor
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun RowCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        content()
    }
}

@Composable
private fun TitleAndSummary(title: String, summary: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Text(
            text = summary,
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BoolRow(spec: BoolSpec, revision: Int) {
    var value by remember(spec.key, revision) {
        mutableStateOf(AppSettings.bool(spec.key, spec.default))
    }
    RowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TitleAndSummary(spec.title, spec.summary, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = value,
                onCheckedChange = {
                    value = it
                    AppSettings.putBool(spec.key, it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF07191E),
                    checkedTrackColor = Accent,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = Divider,
                    uncheckedBorderColor = Divider
                )
            )
        }
    }
}

@Composable
private fun IntRow(spec: IntSpec, revision: Int) {
    var value by remember(spec.key, revision) {
        mutableIntStateOf(AppSettings.int(spec.key, spec.default))
    }
    val steps = ((spec.max - spec.min) / spec.step - 1).coerceAtLeast(0)
    RowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TitleAndSummary(spec.title, spec.summary, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$value${spec.unit}",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Accent
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { raw ->
                val snapped = spec.min + ((raw - spec.min) / spec.step).roundToInt() * spec.step
                value = snapped.coerceIn(spec.min, spec.max)
            },
            onValueChangeFinished = { AppSettings.putInt(spec.key, value) },
            valueRange = spec.min.toFloat()..spec.max.toFloat(),
            steps = steps.coerceAtMost(60),
            colors = sliderColors()
        )
    }
}

@Composable
private fun FloatRow(spec: FloatSpec, revision: Int) {
    var value by remember(spec.key, revision) {
        mutableStateOf(AppSettings.float(spec.key, spec.default))
    }
    RowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TitleAndSummary(spec.title, spec.summary, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = formatFloat(value, spec.decimals) + spec.unit,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Accent
            )
        }
        Slider(
            value = value,
            onValueChange = { value = it },
            onValueChangeFinished = { AppSettings.putFloat(spec.key, value) },
            valueRange = spec.min..spec.max,
            colors = sliderColors()
        )
    }
}

@Composable
private fun ChoiceRow(spec: ChoiceSpec, revision: Int) {
    var value by remember(spec.key, revision) {
        mutableIntStateOf(AppSettings.int(spec.key, spec.default))
    }
    RowCard {
        TitleAndSummary(spec.title, spec.summary)
        Spacer(modifier = Modifier.height(9.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            for ((optionValue, label) in spec.options) {
                val selected = optionValue == value
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Accent else Divider)
                        .clickable {
                            value = optionValue
                            AppSettings.putInt(spec.key, optionValue)
                        }
                        .padding(horizontal = 13.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color(0xFF07191E) else TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = Accent,
    activeTrackColor = Accent,
    inactiveTrackColor = Divider
)

private fun formatFloat(value: Float, decimals: Int): String = when (decimals) {
    0 -> value.roundToInt().toString()
    1 -> String.format("%.1f", value)
    else -> String.format("%.2f", value)
}
