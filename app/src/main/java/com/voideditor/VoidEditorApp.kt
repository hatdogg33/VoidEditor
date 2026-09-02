package com.voideditor

import android.app.Application
import com.voideditor.build.ToolchainPaths
import com.voideditor.service.TermuxService

class VoidEditorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TermuxService.createChannel(this)
        runCatching { ToolchainPaths.migrateLegacyLayout(this) }
    }
}
