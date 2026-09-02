package com.voideditor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.voideditor.data.AppSettings
import com.voideditor.ui.navigation.VoidEditorNavHost
import com.voideditor.ui.screens.StorageGateScreen
import com.voideditor.ui.theme.VoidEditorTheme
import java.io.File

private val RootBrush = Brush.linearGradient(
    0f to Color(0xFF07191E),
    0.45f to Color(0xFF0A242C),
    0.75f to Color(0xFF062B24),
    1f to Color(0xFF02F5A1),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

class MainActivity : ComponentActivity() {

    private var storageReady by mutableStateOf(false)

    private val legacyPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) prepareStorage()
        }

    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkStorage(autoRequest = false)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSettings.init(this)
        enableEdgeToEdge()
        setContent {
            VoidEditorTheme {
                Box(modifier = Modifier.fillMaxSize().background(RootBrush)) {
                    if (storageReady) {
                        VoidEditorNavHost()
                    } else {
                        StorageGateScreen(onGrant = { requestStorageAccess() })
                    }
                }
            }
        }
        checkStorage(autoRequest = true)
    }

    override fun onResume() {
        super.onResume()
        checkStorage(autoRequest = false)
    }

    private fun checkStorage(autoRequest: Boolean) {
        if (hasStorageAccess()) {
            prepareStorage()
        } else if (autoRequest) {
            requestStorageAccess()
        }
    }

    private fun prepareStorage() {
        val base = File(Environment.getExternalStorageDirectory(), getString(R.string.projects_folder_name))
        if (!base.exists()) base.mkdirs()
        storageReady = base.isDirectory
    }

    private fun hasStorageAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val target = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:$packageName")
            )
            val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            try {
                settingsLauncher.launch(target)
            } catch (exception: Exception) {
                settingsLauncher.launch(fallback)
            }
        } else {
            legacyPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}
