package org.aetherfeed.app.sync

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import org.aetherfeed.app.domain.LibraryRepository
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadState
import org.aetherfeed.app.domain.ReadStatus

class ReadReceiptSync(
    private val context: Context,
    private val library: LibraryRepository,
    private val authority: String = SubscriptionPoolClient.AUTHORITY,
) {
    suspend fun pullAndApply() {
        val remote = readRemote() ?: return
        val local = localReceipts()
        val merged = ReadReceiptCodec.mergeLww(local, remote.reads)
        applyMerged(merged)
        writeRemote(ReadReceiptDocument(version = 1, updatedAt = System.currentTimeMillis(), reads = merged))
    }

    suspend fun publishFromLocal() {
        val prior = readRemote()?.reads.orEmpty()
        val merged = ReadReceiptCodec.mergeLww(localReceipts(), prior)
        writeRemote(ReadReceiptDocument(version = 1, updatedAt = System.currentTimeMillis(), reads = merged))
    }

    private suspend fun localReceipts(): List<ReadReceipt> {
        val articles = library.articles(null)
        return articles.mapNotNull { article ->
            val state = library.readState(article.id) ?: return@mapNotNull null
            ReadReceipt(
                url = SubscriptionPoolCodec.normalizeUrl(article.url),
                read = state.status == ReadStatus.Read,
                updatedAt = state.updatedAt,
            )
        }
    }

    private suspend fun applyMerged(merged: List<ReadReceipt>) {
        val byUrl = merged.associateBy { it.url }
        var applied = 0
        for (article in library.articles(null)) {
            val receipt = byUrl[SubscriptionPoolCodec.normalizeUrl(article.url)] ?: continue
            val status = if (receipt.read) ReadStatus.Read else ReadStatus.Unread
            val prior = library.readState(article.id)
            if (prior?.status == status && (prior.updatedAt >= receipt.updatedAt)) {
                continue
            }
            library.upsertReadState(
                ReadState(
                    targetId = article.id,
                    module = ModuleKind.News,
                    status = status,
                    updatedAt = receipt.updatedAt,
                ),
            )
            applied += 1
        }
        val articles = library.articles(null)
        val overlap =
            articles.count { byUrl.containsKey(SubscriptionPoolCodec.normalizeUrl(it.url)) }
        Log.i(
            "AetherReadSync",
            "receipts=${merged.size} articles=${articles.size} overlap=$overlap applied=$applied " +
                "sampleReceipt=${merged.firstOrNull()?.url} sampleArticle=${articles.firstOrNull()?.url}",
        )
    }

    private fun readRemote(): ReadReceiptDocument? {
        if (!SubscriptionPoolClient(context).hermesInstalled()) {
            return null
        }
        return try {
            val uri = Uri.parse("content://$authority/reads")
            context.contentResolver.query(uri, arrayOf(SubscriptionPoolClient.COLUMN), null, null, null)
                ?.use { cursor ->
                    if (!cursor.moveToFirst()) {
                        return ReadReceiptDocument()
                    }
                    val idx = cursor.getColumnIndex(SubscriptionPoolClient.COLUMN)
                    val raw = if (idx >= 0) cursor.getString(idx).orEmpty() else ""
                    if (raw.isBlank()) ReadReceiptDocument() else ReadReceiptCodec.decode(raw)
                }
        } catch (_: Exception) {
            null
        }
    }

    private fun writeRemote(doc: ReadReceiptDocument) {
        try {
            val raw = ReadReceiptCodec.encode(doc)
            val values = ContentValues().apply { put(SubscriptionPoolClient.COLUMN, raw) }
            val uri = Uri.parse("content://$authority/reads")
            val updated = context.contentResolver.update(uri, values, null, null)
            if (updated <= 0) {
                context.contentResolver.insert(uri, values)
            }
        } catch (_: Exception) {
            // Peer missing / denied — leave local unchanged.
        }
    }
}
