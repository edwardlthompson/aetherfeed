package org.aetherfeed.app.data.local

import androidx.room.TypeConverter
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadStatus

class VaultConverters {
    @TypeConverter
    fun moduleToString(value: ModuleKind): String = value.name.lowercase()

    @TypeConverter
    fun moduleFromString(value: String): ModuleKind =
        ModuleKind.entries.first { it.name.equals(value, ignoreCase = true) }

    @TypeConverter
    fun readStatusToString(value: ReadStatus): String =
        when (value) {
            ReadStatus.Unread -> "unread"
            ReadStatus.InProgress -> "in_progress"
            ReadStatus.Read -> "read"
        }

    @TypeConverter
    fun readStatusFromString(value: String): ReadStatus =
        when (value) {
            "in_progress" -> ReadStatus.InProgress
            "read" -> ReadStatus.Read
            else -> ReadStatus.Unread
        }
}
