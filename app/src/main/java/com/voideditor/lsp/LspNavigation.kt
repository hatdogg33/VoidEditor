package com.voideditor.lsp

import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.lsp.utils.asLspPosition
import io.github.rosemoe.sora.lsp.utils.createTextDocumentIdentifier
import io.github.rosemoe.sora.text.CharPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File
import java.net.URI

data class SymbolEntry(
    val name: String,
    val detail: String,
    val kind: String,
    val line: Int,
    val column: Int,
    val depth: Int
)

data class LocationEntry(
    val file: File,
    val line: Int,
    val column: Int,
    val preview: String
)

object LspNavigation {

    private const val TimeoutMs = 6000L

    suspend fun documentSymbols(editor: LspEditor): List<SymbolEntry> =
        withContext(Dispatchers.IO) {
            val params = DocumentSymbolParams(editor.uri.createTextDocumentIdentifier())
            val future = editor.requestManager.documentSymbol(params) ?: return@withContext emptyList()
            val result = withTimeoutOrNull(TimeoutMs) { runCatching { future.await() }.getOrNull() }
                ?: return@withContext emptyList()
            val out = mutableListOf<SymbolEntry>()
            for (either in result) {
                appendSymbol(either, 0, out)
            }
            out
        }

    private fun appendSymbol(
        either: Either<SymbolInformation, DocumentSymbol>?,
        depth: Int,
        out: MutableList<SymbolEntry>
    ) {
        if (either == null) return
        if (either.isRight) {
            val symbol = either.right ?: return
            out += SymbolEntry(
                name = symbol.name.orEmpty(),
                detail = symbol.detail.orEmpty(),
                kind = symbol.kind?.name.orEmpty(),
                line = symbol.selectionRange?.start?.line ?: symbol.range?.start?.line ?: 0,
                column = symbol.selectionRange?.start?.character ?: 0,
                depth = depth
            )
            symbol.children?.forEach { child ->
                appendSymbol(Either.forRight(child), depth + 1, out)
            }
            return
        }
        val info = either.left ?: return
        out += SymbolEntry(
            name = info.name.orEmpty(),
            detail = info.containerName.orEmpty(),
            kind = info.kind?.name.orEmpty(),
            line = info.location?.range?.start?.line ?: 0,
            column = info.location?.range?.start?.character ?: 0,
            depth = depth
        )
    }

    suspend fun definition(editor: LspEditor, position: CharPosition): List<LocationEntry> =
        withContext(Dispatchers.IO) {
            val params = DefinitionParams(
                editor.uri.createTextDocumentIdentifier(),
                position.asLspPosition()
            )
            val future = editor.requestManager.definition(params) ?: return@withContext emptyList()
            val either = withTimeoutOrNull(TimeoutMs) { runCatching { future.await() }.getOrNull() }
                ?: return@withContext emptyList()
            val locations = when {
                either == null -> emptyList()
                either.isLeft -> either.left.orEmpty()
                else -> either.right.orEmpty().mapNotNull { link ->
                    val target = link.targetUri ?: return@mapNotNull null
                    Location(target, link.targetSelectionRange ?: link.targetRange)
                }
            }
            locations.mapNotNull { it.toEntry() }
        }

    suspend fun references(editor: LspEditor, position: CharPosition): List<LocationEntry> =
        withContext(Dispatchers.IO) {
            val params = ReferenceParams(
                editor.uri.createTextDocumentIdentifier(),
                position.asLspPosition(),
                ReferenceContext(true)
            )
            val future = editor.requestManager.references(params) ?: return@withContext emptyList()
            val result = withTimeoutOrNull(TimeoutMs) { runCatching { future.await() }.getOrNull() }
                ?: return@withContext emptyList()
            result.orEmpty().filterNotNull().mapNotNull { it.toEntry() }
        }

    private fun Location.toEntry(): LocationEntry? {
        val target = runCatching { File(URI(uri)) }.getOrNull() ?: return null
        if (!target.isFile) return null
        val line = range?.start?.line ?: 0
        val column = range?.start?.character ?: 0
        val preview = runCatching {
            target.useLines { lines -> lines.drop(line).firstOrNull()?.trim() }
        }.getOrNull().orEmpty()
        return LocationEntry(file = target, line = line, column = column, preview = preview)
    }
}
