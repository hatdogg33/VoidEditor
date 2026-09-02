package com.voideditor.editor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Typeface
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun EditorPane(
    onEditorCreated: (CodeEditor) -> Unit,
    onEditorReleased: () -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            TextMateSetup.ensureInitialized(viewContext)
            CodeEditor(viewContext).apply {
                EditorThemeName.current().apply(this)
                runCatching {
                    typefaceText = Typeface.createFromAsset(
                        viewContext.assets,
                        "fonts/JetBrainsMono-Regular.ttf"
                    )
                }
            }.also(onEditorCreated)
        },
        onRelease = { editor ->
            onEditorReleased()
            runCatching { editor.release() }
        }
    )
}
