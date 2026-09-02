package com.voideditor.storage

import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Point
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import android.webkit.MimeTypeMap
import com.voideditor.R
import java.io.File
import java.io.FileNotFoundException

class VoidEditorDocumentsProvider : DocumentsProvider() {

    companion object {
        private const val AllMimeTypes = "*/*"
        private const val RootId = "editores"

        private val DefaultRootProjection = arrayOf(
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_AVAILABLE_BYTES
        )

        private val DefaultDocumentProjection = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE,
            Document.COLUMN_ICON
        )

        private fun mimeType(file: File): String {
            if (file.isDirectory) return Document.MIME_TYPE_DIR
            val dot = file.name.lastIndexOf('.')
            if (dot >= 0) {
                val extension = file.name.substring(dot + 1).lowercase()
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.let { return it }
            }
            return "application/octet-stream"
        }
    }

    private val baseDir: File by lazy { context!!.filesDir }

    override fun onCreate(): Boolean = true

    private fun docIdOf(file: File): String {
        val base = baseDir.canonicalPath
        val path = file.canonicalPath
        if (path == base) return RootId
        if (!path.startsWith("$base/")) throw FileNotFoundException("Outside root: $path")
        return RootId + path.removePrefix(base)
    }

    private fun fileOf(documentId: String): File {
        if (documentId == RootId) return baseDir
        if (!documentId.startsWith("$RootId/")) throw FileNotFoundException(documentId)
        val relative = documentId.removePrefix("$RootId/")
        val file = File(baseDir, relative)
        if (!file.canonicalPath.startsWith(baseDir.canonicalPath)) {
            throw FileNotFoundException("Access denied")
        }
        return file
    }

    private fun existingFileOf(documentId: String): File {
        val file = fileOf(documentId)
        if (!file.exists()) throw FileNotFoundException(documentId)
        return file
    }

    override fun queryRoots(projection: Array<String>?): Cursor {
        val columns = projection ?: DefaultRootProjection
        val result = MatrixCursor(columns)
        if (!baseDir.exists()) baseDir.mkdirs()
        val values = mapOf(
            Root.COLUMN_ROOT_ID to RootId,
            Root.COLUMN_DOCUMENT_ID to RootId,
            Root.COLUMN_TITLE to context!!.getString(R.string.app_name),
            Root.COLUMN_SUMMARY to "Internal app storage",
            Root.COLUMN_FLAGS to (
                Root.FLAG_SUPPORTS_CREATE or
                    Root.FLAG_SUPPORTS_SEARCH or
                    Root.FLAG_SUPPORTS_IS_CHILD or
                    Root.FLAG_LOCAL_ONLY
                ),
            Root.COLUMN_MIME_TYPES to AllMimeTypes,
            Root.COLUMN_AVAILABLE_BYTES to baseDir.freeSpace,
            Root.COLUMN_ICON to R.mipmap.ic_launcher
        )
        addRow(result, columns, values)
        return result
    }

    override fun queryDocument(documentId: String, projection: Array<String>?): Cursor {
        val columns = projection ?: DefaultDocumentProjection
        val result = MatrixCursor(columns)
        includeFile(result, columns, existingFileOf(documentId))
        return result
    }

    override fun queryChildDocuments(
        parentDocumentId: String,
        projection: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val columns = projection ?: DefaultDocumentProjection
        val result = MatrixCursor(columns)
        val parent = existingFileOf(parentDocumentId)
        parent.listFiles()?.sortedWith(
            compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() }
        )?.forEach { includeFile(result, columns, it) }
        return result
    }

    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        val file = existingFileOf(documentId)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode))
    }

    override fun openDocumentThumbnail(
        documentId: String,
        sizeHint: Point,
        signal: CancellationSignal?
    ): AssetFileDescriptor {
        val file = existingFileOf(documentId)
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        return AssetFileDescriptor(pfd, 0, file.length())
    }

    override fun createDocument(
        parentDocumentId: String,
        mimeType: String?,
        displayName: String
    ): String {
        val parent = existingFileOf(parentDocumentId)
        var target = File(parent, displayName)
        var conflict = 2
        while (target.exists()) {
            target = File(parent, "$displayName ($conflict)")
            conflict++
        }
        val created = if (Document.MIME_TYPE_DIR == mimeType) {
            target.mkdir()
        } else {
            runCatching { target.createNewFile() }.getOrDefault(false)
        }
        if (!created) throw FileNotFoundException("Failed to create $displayName")
        return docIdOf(target)
    }

    override fun renameDocument(documentId: String, displayName: String): String {
        val file = existingFileOf(documentId)
        val target = File(file.parentFile, displayName)
        if (target.exists()) throw FileNotFoundException("$displayName already exists")
        if (!file.renameTo(target)) throw FileNotFoundException("Failed to rename $documentId")
        return docIdOf(target)
    }

    override fun deleteDocument(documentId: String) {
        val file = existingFileOf(documentId)
        if (!deleteTree(file)) throw FileNotFoundException("Failed to delete $documentId")
    }

    private fun deleteTree(file: File): Boolean {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteTree(it) }
        }
        if (file.delete()) return true
        file.setWritable(true)
        file.setExecutable(true)
        return file.delete()
    }

    override fun getDocumentType(documentId: String): String = mimeType(existingFileOf(documentId))

    override fun querySearchDocuments(
        rootId: String,
        query: String,
        projection: Array<String>?
    ): Cursor {
        val columns = projection ?: DefaultDocumentProjection
        val result = MatrixCursor(columns)
        val needle = query.lowercase()
        val pending = ArrayDeque<File>().apply { add(baseDir) }
        var matches = 0
        while (pending.isNotEmpty() && matches < 64) {
            val file = pending.removeFirst()
            if (file.isDirectory) {
                file.listFiles()?.forEach { pending.add(it) }
            } else if (file.name.lowercase().contains(needle)) {
                includeFile(result, columns, file)
                matches++
            }
        }
        return result
    }

    override fun isChildDocument(parentDocumentId: String, documentId: String): Boolean {
        val parent = runCatching { fileOf(parentDocumentId) }.getOrNull() ?: return false
        val child = runCatching { fileOf(documentId) }.getOrNull() ?: return false
        return child.canonicalPath.startsWith(parent.canonicalPath + "/")
    }

    private fun includeFile(result: MatrixCursor, columns: Array<String>, file: File) {
        var flags = 0
        if (file.isDirectory) {
            if (file.canWrite()) flags = flags or Document.FLAG_DIR_SUPPORTS_CREATE
        } else if (file.canWrite()) {
            flags = flags or Document.FLAG_SUPPORTS_WRITE
        }
        if (file.parentFile?.canWrite() == true) {
            flags = flags or Document.FLAG_SUPPORTS_DELETE or Document.FLAG_SUPPORTS_RENAME
        }
        val type = mimeType(file)
        if (type.startsWith("image/")) flags = flags or Document.FLAG_SUPPORTS_THUMBNAIL
        val name = if (file.canonicalPath == baseDir.canonicalPath) {
            context!!.getString(R.string.app_name)
        } else {
            file.name
        }
        val values = mapOf(
            Document.COLUMN_DOCUMENT_ID to docIdOf(file),
            Document.COLUMN_DISPLAY_NAME to name,
            Document.COLUMN_SIZE to file.length(),
            Document.COLUMN_MIME_TYPE to type,
            Document.COLUMN_LAST_MODIFIED to file.lastModified(),
            Document.COLUMN_FLAGS to flags,
            Document.COLUMN_ICON to R.mipmap.ic_launcher
        )
        addRow(result, columns, values)
    }

    private fun addRow(result: MatrixCursor, columns: Array<String>, values: Map<String, Any?>) {
        val row = result.newRow()
        for (column in columns) {
            row.add(column, values[column])
        }
    }
}
