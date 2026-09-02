package com.voideditor.agent

import android.content.Context
import com.voideditor.proot.ProotConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class AgentStatus {
    Unknown,
    Installed,
    NotInstalled,
    Installing,
    Failed
}

object AgentInstaller {

    private const val ScriptDirName = "agent-scripts"

    fun scriptFor(context: Context, spec: AgentSpec): File {
        val dir = File(ProotConfig.rootfsDir(context), "root/$ScriptDirName").apply { mkdirs() }
        val body = buildString {
            append(spec.installScript.trimStart())
            append('\n')
        }
        return File(dir, "${spec.id}.sh").apply {
            writeText(body)
            setExecutable(true)
        }
    }

    fun guestScriptPath(spec: AgentSpec): String = "/root/$ScriptDirName/${spec.id}.sh"

    suspend fun detect(context: Context, spec: AgentSpec): AgentStatus =
        withContext(Dispatchers.IO) {
            if (!ProotConfig.isInstalled(context) || !ProotConfig.isAvailable(context)) {
                return@withContext AgentStatus.Unknown
            }
            val args = ProotConfig.commandArgs(
                context = context,
                script = "command -v ${spec.binary} >/dev/null 2>&1 && echo FOUND || echo MISSING",
                guestCwd = "/root"
            )
            val output = runCatching {
                val builder = ProcessBuilder(args)
                builder.redirectErrorStream(true)
                builder.environment().putAll(ProotConfig.prootEnvMap(context))
                val process = builder.start()
                val text = process.inputStream.bufferedReader().use { it.readText() }
                process.waitFor()
                text
            }.getOrNull() ?: return@withContext AgentStatus.Unknown
            if (output.contains("FOUND")) AgentStatus.Installed else AgentStatus.NotInstalled
        }
}
