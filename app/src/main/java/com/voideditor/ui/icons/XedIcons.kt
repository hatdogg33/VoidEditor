package com.voideditor.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import com.voideditor.R

object XedIcons {

    @DrawableRes
    fun fileType(fileName: String): Int {
        val lower = fileName.lowercase()
        exactNames[lower]?.let { return it }
        val bare = lower.substringBeforeLast('.', lower)
        exactNames[bare]?.let { return it }
        val ext = lower.substringAfterLast('.', "")
        return extensions[ext] ?: R.drawable.file
    }

    private val exactNames: Map<String, Int> = mapOf(
        "cmakelists.txt" to R.drawable.cmake,
        "cmakepresets.json" to R.drawable.cmake,
        "cmakeuserpresets.json" to R.drawable.cmake,
        "compile_commands.json" to R.drawable.json,
        "compile_flags.txt" to R.drawable.cmake,
        "build.ninja" to R.drawable.build,
        "makefile" to R.drawable.build,
        ".clangd" to R.drawable.yaml,
        ".clang-format" to R.drawable.yaml,
        ".gitignore" to R.drawable.git,
        ".gitattributes" to R.drawable.git,
        ".gitmodules" to R.drawable.git,
        ".gitconfig" to R.drawable.git,
        "license" to R.drawable.text,
        "readme" to R.drawable.markdown,
        "gradlew" to R.drawable.gradle,
        "dockerfile" to R.drawable.bash
    )

    private val extensions: Map<String, Int> = mapOf(
        "c" to R.drawable.c,
        "h" to R.drawable.cpp,
        "cpp" to R.drawable.cpp,
        "cxx" to R.drawable.cpp,
        "cc" to R.drawable.cpp,
        "c++" to R.drawable.cpp,
        "hpp" to R.drawable.cpp,
        "hh" to R.drawable.cpp,
        "hxx" to R.drawable.cpp,
        "h++" to R.drawable.cpp,
        "inl" to R.drawable.cpp,
        "cmake" to R.drawable.cmake,
        "mk" to R.drawable.build,
        "ninja" to R.drawable.build,
        "py" to R.drawable.python,
        "pyi" to R.drawable.python,
        "java" to R.drawable.java,
        "jav" to R.drawable.java,
        "kt" to R.drawable.kotlin,
        "kts" to R.drawable.kotlin,
        "json" to R.drawable.json,
        "jsonc" to R.drawable.json,
        "jsonl" to R.drawable.json,
        "xml" to R.drawable.xml,
        "plist" to R.drawable.xml,
        "svg" to R.drawable.image,
        "md" to R.drawable.markdown,
        "markdown" to R.drawable.markdown,
        "yaml" to R.drawable.yaml,
        "yml" to R.drawable.yaml,
        "toml" to R.drawable.toml,
        "ini" to R.drawable.settings,
        "cfg" to R.drawable.settings,
        "conf" to R.drawable.settings,
        "config" to R.drawable.settings,
        "properties" to R.drawable.settings,
        "editorconfig" to R.drawable.settings,
        "sh" to R.drawable.bash,
        "bash" to R.drawable.bash,
        "zsh" to R.drawable.bash,
        "fish" to R.drawable.bash,
        "ksh" to R.drawable.bash,
        "bat" to R.drawable.bash,
        "cmd" to R.drawable.bash,
        "ps1" to R.drawable.powershell,
        "psm1" to R.drawable.powershell,
        "js" to R.drawable.javascript,
        "mjs" to R.drawable.javascript,
        "cjs" to R.drawable.javascript,
        "ts" to R.drawable.typescript,
        "mts" to R.drawable.typescript,
        "jsx" to R.drawable.react,
        "tsx" to R.drawable.react,
        "html" to R.drawable.html,
        "htm" to R.drawable.html,
        "css" to R.drawable.css,
        "scss" to R.drawable.sass,
        "sass" to R.drawable.sass,
        "less" to R.drawable.less,
        "rs" to R.drawable.rust,
        "go" to R.drawable.golang,
        "lua" to R.drawable.lua,
        "luau" to R.drawable.lua,
        "php" to R.drawable.php,
        "rb" to R.drawable.ruby,
        "swift" to R.drawable.swift,
        "dart" to R.drawable.dart,
        "cs" to R.drawable.csharp,
        "groovy" to R.drawable.apachegroovy,
        "gradle" to R.drawable.gradle,
        "sql" to R.drawable.sql,
        "r" to R.drawable.r,
        "nix" to R.drawable.nix,
        "nim" to R.drawable.nim,
        "zig" to R.drawable.zig,
        "lisp" to R.drawable.lisp,
        "clisp" to R.drawable.lisp,
        "tex" to R.drawable.latex,
        "latex" to R.drawable.latex,
        "diff" to R.drawable.diff,
        "patch" to R.drawable.diff,
        "txt" to R.drawable.text,
        "log" to R.drawable.text,
        "png" to R.drawable.image,
        "jpg" to R.drawable.image,
        "jpeg" to R.drawable.image,
        "gif" to R.drawable.image,
        "bmp" to R.drawable.image,
        "webp" to R.drawable.image,
        "ico" to R.drawable.image,
        "tiff" to R.drawable.image,
        "mp3" to R.drawable.music,
        "wav" to R.drawable.music,
        "ogg" to R.drawable.music,
        "flac" to R.drawable.music,
        "m4a" to R.drawable.music,
        "aac" to R.drawable.music,
        "opus" to R.drawable.music,
        "mp4" to R.drawable.video,
        "mkv" to R.drawable.video,
        "avi" to R.drawable.video,
        "mov" to R.drawable.video,
        "webm" to R.drawable.video,
        "zip" to R.drawable.archive,
        "rar" to R.drawable.archive,
        "7z" to R.drawable.archive,
        "tar" to R.drawable.archive,
        "gz" to R.drawable.archive,
        "xz" to R.drawable.archive,
        "bz2" to R.drawable.archive,
        "zst" to R.drawable.archive,
        "jar" to R.drawable.archive,
        "aar" to R.drawable.archive,
        "apk" to R.drawable.apk_document,
        "so" to R.drawable.lock,
        "a" to R.drawable.lock,
        "o" to R.drawable.lock,
        "obj" to R.drawable.lock,
        "bin" to R.drawable.lock,
        "elf" to R.drawable.lock,
        "exe" to R.drawable.lock,
        "lock" to R.drawable.lock,
        "asm" to R.drawable.letters,
        "s" to R.drawable.letters,
        "csv" to R.drawable.text,
        "tsv" to R.drawable.text
    )
}

@Composable
fun rememberFileIcon(fileName: String): ImageVector =
    ImageVector.vectorResource(XedIcons.fileType(fileName))

@Composable
fun fileIconPainter(fileName: String): Painter =
    painterResource(XedIcons.fileType(fileName))
