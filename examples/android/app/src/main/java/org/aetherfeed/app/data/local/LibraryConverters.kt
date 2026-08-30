package org.aetherfeed.app.data.local

import androidx.room.TypeConverter
import org.aetherfeed.app.domain.ModuleKind
import org.aetherfeed.app.domain.ReadStatus

class LibraryConverters {
    @TypeConverter
    fun moduleKindToWire(value: ModuleKind): String = when (value) {
        ModuleKind.News -> "news"
        ModuleKind.Podcast -> "podcast"
        ModuleKind.Booru -> "booru"
    }

    @TypeConverter
    fun moduleKindFromWire(value: String): ModuleKind = when (value) {
        "podcast" -> ModuleKind.Podcast
        "booru" -> ModuleKind.Booru
        else -> ModuleKind.News
    }

    @TypeConverter
    fun readStatusToWire(value: ReadStatus): String = when (value) {
        ReadStatus.Unread -> "unread"
        ReadStatus.InProgress -> "in_progress"
        ReadStatus.Read -> "read"
    }

    @TypeConverter
    fun readStatusFromWire(value: String): ReadStatus = when (value) {
        "in_progress" -> ReadStatus.InProgress
        "read" -> ReadStatus.Read
        else -> ReadStatus.Unread
    }
}
