package com.voideditor.data

import java.io.File

object FileOps {

    private val namePattern = Regex("^[A-Za-z0-9._][A-Za-z0-9._-]{0,79}$")

    fun createFile(parent: File, name: String): Result<File> = runCatching {
        val clean = name.trim()
        require(namePattern.matches(clean)) { "Invalid file name" }
        val target = File(parent, clean)
        // Remove TOCTOU race: just check if creation succeeds
        require(target.createNewFile()) { "Unable to create file or file already exists" }
        target
    }

    fun createFolder(parent: File, name: String): Result<File> = runCatching {
        val clean = name.trim()
        require(namePattern.matches(clean)) { "Invalid folder name" }
        val target = File(parent, clean)
        require(!target.exists()) { "A file or folder named $clean already exists" }
        require(target.mkdirs()) { "Unable to create the folder" }
        target
    }

    fun rename(target: File, newName: String): Result<File> = runCatching {
        val clean = newName.trim()
        require(namePattern.matches(clean)) { "Invalid name" }
        val destination = File(target.parentFile, clean)
        require(!destination.exists()) { "A file or folder named $clean already exists" }
        require(target.renameTo(destination)) { "Unable to rename" }
        destination
    }

    fun delete(target: File): Result<Unit> = runCatching {
        if (target.isDirectory) {
            var failed = false
            target.walkBottomUp().forEach { file ->
                if (!file.delete()) {
                    file.setWritable(true)
                    file.setExecutable(true)
                    if (!file.delete()) failed = true
                }
            }
            require(!failed) { "Unable to delete the folder" }
        } else {
            if (!target.delete()) {
                target.setWritable(true)
                require(target.delete()) { "Unable to delete the file" }
            }
        }
    }
}
