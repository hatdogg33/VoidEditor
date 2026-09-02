package com.voideditor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voideditor.data.AppSettings
import com.voideditor.data.PreferenceSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RecentFile(
    val path: String,
    val name: String,
    val lastOpened: Long
)

data class GitStatus(
    val branch: String,
    val modifiedFiles: List<String>,
    val stagedFiles: List<String>,
    val untrackedFiles: List<String>,
    val isClean: Boolean
)

data class GitLogEntry(
    val hash: String,
    val message: String,
    val author: String,
    val date: String
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _recentFiles = MutableStateFlow<List<RecentFile>>(emptyList())
    val recentFiles: StateFlow<List<RecentFile>> = _recentFiles.asStateFlow()

    private val _gitStatus = MutableStateFlow<GitStatus?>(null)
    val gitStatus: StateFlow<GitStatus?> = _gitStatus.asStateFlow()

    private val _gitLog = MutableStateFlow<List<GitLogEntry>>(emptyList())
    val gitLog: StateFlow<List<GitLogEntry>> = _gitLog.asStateFlow()

    private val _gitOutput = MutableStateFlow<List<String>>(emptyList())
    val gitOutput: StateFlow<List<String>> = _gitOutput.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        loadRecentFiles()
    }

    fun addRecentFile(path: String) {
        val file = File(path)
        val recent = RecentFile(
            path = path,
            name = file.name,
            lastOpened = System.currentTimeMillis()
        )
        val current = _recentFiles.value.toMutableList()
        current.removeAll { it.path == path }
        current.add(0, recent)
        if (current.size > 20) current.removeLast()
        _recentFiles.value = current
        saveRecentFiles()
    }

    fun removeRecentFile(path: String) {
        _recentFiles.value = _recentFiles.value.filter { it.path != path }
        saveRecentFiles()
    }

    private fun loadRecentFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>()
                .getSharedPreferences("voideditor_settings", 0)
            val json = prefs.getString("recent_files", null) ?: return@launch
            val files = json.split("\n").filter { it.isNotBlank() }.mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size == 3) {
                    RecentFile(parts[0], parts[1], parts[2].toLongOrNull() ?: 0L)
                } else null
            }
            _recentFiles.value = files
        }
    }

    private fun saveRecentFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>()
                .getSharedPreferences("voideditor_settings", 0)
            val json = _recentFiles.value.joinToString("\n") {
                "${it.path}|${it.name}|${it.lastOpened}"
            }
            prefs.edit().putString("recent_files", json).apply()
        }
    }

    fun refreshGitStatus(projectDir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            _gitStatus.value = runCatching { executeGit(projectDir, "status", "--porcelain") }
                .getOrNull()?.let { output ->
                    val modified = mutableListOf<String>()
                    val staged = mutableListOf<String>()
                    val untracked = mutableListOf<String>()
                    output.lines().filter { it.isNotBlank() }.forEach { line ->
                        val path = line.substring(3)
                        when {
                            line[0] != ' ' && line[0] != '?' -> staged.add(path)
                            line[1] == 'M' -> modified.add(path)
                            line[0] == '?' -> untracked.add(path)
                        }
                    }
                    val branch = executeGit(projectDir, "rev-parse", "--abbrev-ref", "HEAD")
                        .trim().ifBlank { "main" }
                    GitStatus(branch, modified, staged, untracked, output.isBlank())
                }
        }
    }

    fun refreshGitLog(projectDir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            _gitLog.value = runCatching {
                val output = executeGit(projectDir, "log", "--oneline", "-20")
                output.lines().filter { it.isNotBlank() }.map { line ->
                    val parts = line.split(" ", limit = 2)
                    GitLogEntry(
                        hash = parts.getOrElse(0) { "" },
                        message = parts.getOrElse(1) { "" },
                        author = "",
                        date = ""
                    )
                }
            }.getOrDefault(emptyList())
        }
    }

    fun gitCommit(projectDir: File, message: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                executeGit(projectDir, "add", "-A")
                val output = executeGit(projectDir, "commit", "-m", message)
                refreshGitStatus(projectDir)
                refreshGitLog(projectDir)
                onComplete(true, output)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Commit failed")
            }
        }
    }

    fun gitInit(projectDir: File, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val output = executeGit(projectDir, "init")
                refreshGitStatus(projectDir)
                onComplete(true, output)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Init failed")
            }
        }
    }

    private fun executeGit(projectDir: File, vararg commands: String): String {
        val fullCommand = arrayOf("git", "-C", projectDir.absolutePath) + commands
        val process = ProcessBuilder(*fullCommand)
            .directory(projectDir)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0 && output.isBlank()) {
            throw Exception("Git command failed with exit code $exitCode")
        }
        return output
    }

    fun startAutoSave(getModifiedFiles: () -> Map<String, String>) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(30_000)
                if (AppSettings.bool(PreferenceSettings.AutoSaveOnBuild, true)) {
                    getModifiedFiles().forEach { (path, content) ->
                        runCatching {
                            File(path).writeText(content)
                        }
                    }
                }
            }
        }
    }

    fun stopAutoSave() {
        autoSaveJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}
