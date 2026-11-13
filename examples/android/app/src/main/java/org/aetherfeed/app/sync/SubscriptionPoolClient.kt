package org.aetherfeed.app.sync

import android.content.ContentValues
import android.content.Context
import android.net.Uri

sealed class PoolClientResult {
    data class Ok(val document: PoolDocument) : PoolClientResult()

    data class Unavailable(val reason: String) : PoolClientResult()

    data class Denied(val reason: String) : PoolClientResult()

    data class Invalid(val reason: String) : PoolClientResult()
}

interface SubscriptionPoolGateway {
    fun read(): PoolClientResult

    fun write(document: PoolDocument): PoolClientResult
}

class SubscriptionPoolClient(
    private val context: Context,
    private val authority: String = AUTHORITY,
) : SubscriptionPoolGateway {
    override fun read(): PoolClientResult {
        if (!hermesInstalled()) {
            return PoolClientResult.Unavailable("Hermes not installed")
        }
        return try {
            val uri = Uri.parse("content://$authority/document")
            context.contentResolver.query(uri, arrayOf(COLUMN), null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) {
                    return PoolClientResult.Ok(PoolDocument())
                }
                val idx = cursor.getColumnIndex(COLUMN)
                val raw = if (idx >= 0) cursor.getString(idx).orEmpty() else ""
                if (raw.isBlank()) {
                    return PoolClientResult.Ok(PoolDocument())
                }
                try {
                    PoolClientResult.Ok(SubscriptionPoolCodec.decode(raw))
                } catch (e: PoolCodecError) {
                    PoolClientResult.Invalid(e.message ?: "invalid pool document")
                }
            } ?: PoolClientResult.Unavailable("Hermes pool empty response")
        } catch (e: SecurityException) {
            PoolClientResult.Denied(e.message ?: "permission denied")
        } catch (e: IllegalArgumentException) {
            PoolClientResult.Unavailable(e.message ?: "provider missing")
        } catch (e: Exception) {
            PoolClientResult.Unavailable(e.message ?: e.javaClass.simpleName)
        }
    }

    override fun write(document: PoolDocument): PoolClientResult {
        if (!hermesInstalled()) {
            return PoolClientResult.Unavailable("Hermes not installed")
        }
        return try {
            val raw = SubscriptionPoolCodec.encode(document)
            val values = ContentValues().apply { put(COLUMN, raw) }
            val uri = Uri.parse("content://$authority/document")
            val updated = context.contentResolver.update(uri, values, null, null)
            if (updated <= 0) {
                context.contentResolver.insert(uri, values)
                    ?: return PoolClientResult.Unavailable("write rejected")
            }
            PoolClientResult.Ok(document)
        } catch (e: SecurityException) {
            PoolClientResult.Denied(e.message ?: "permission denied")
        } catch (e: PoolCodecError) {
            PoolClientResult.Invalid(e.message ?: "invalid document")
        } catch (e: IllegalArgumentException) {
            PoolClientResult.Unavailable(e.message ?: "provider missing")
        } catch (e: Exception) {
            PoolClientResult.Unavailable(e.message ?: e.javaClass.simpleName)
        }
    }

    fun hermesInstalled(): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(HERMES_PACKAGE, 0)
            true
        }.getOrDefault(false)

    companion object {
        const val HERMES_PACKAGE = "org.hermeslauncher.app"
        const val AUTHORITY = "org.hermeslauncher.app.subscription_pool"
        const val PERMISSION = "org.hermeslauncher.app.permission.SUBSCRIPTION_POOL"
        const val COLUMN = "document"
    }
}
