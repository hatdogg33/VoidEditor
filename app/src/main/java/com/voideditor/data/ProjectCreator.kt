package com.voideditor.data

import android.os.Environment
import java.io.File

object ProjectCreator {

    const val MAIN_SOURCE_NAME = "main.cpp"

    private val namePattern = Regex("^[A-Za-z0-9][A-Za-z0-9_-]{0,39}$")

    fun baseDir(): File = File(Environment.getExternalStorageDirectory(), "EditorEs")

    fun create(folderName: String, libraryName: String): Result<String> = runCatching {
        val folder = folderName.trim()
        val library = libraryName.trim()
        require(namePattern.matches(folder)) { "Folder name may only contain letters, numbers, - and _" }
        require(namePattern.matches(library)) { "Library name may only contain letters, numbers, - and _" }
        val projectDir = File(baseDir(), folder)
        require(!projectDir.exists()) { "A folder named $folder already exists" }
        require(projectDir.mkdirs()) { "Unable to create the project folder" }
        File(projectDir, MAIN_SOURCE_NAME).writeText(mainCppTemplate)
        File(projectDir, "CMakeLists.txt").writeText(cmakeTemplate(library))
        projectDir.absolutePath
    }

    private val cmakeTemplate: (String) -> String = { library ->
        buildString {
            appendLine("cmake_minimum_required(VERSION 3.22.1)")
            appendLine()
            appendLine("project(\"$library\")")
            appendLine()
            appendLine("add_library($library SHARED $MAIN_SOURCE_NAME)")
            appendLine()
            appendLine("find_library(log-lib log)")
            appendLine()
            appendLine("target_link_libraries($library \${log-lib})")
        }
    }

    private val mainCppTemplate = buildString {
        appendLine("#include <iostream>")
        appendLine()
        appendLine("int main(int argc, char **argv) {")
        appendLine("    std::cout << \"Hello from EditorEs\" << std::endl;")
        appendLine("    return 0;")
        appendLine("}")
    }
}
