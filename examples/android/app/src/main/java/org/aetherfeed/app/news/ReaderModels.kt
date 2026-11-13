package org.aetherfeed.app.news

enum class ArticleTarget {
    LAUNCHER,
    BROWSER,
    CUSTOM_TAB,
    ;

    companion object {
        fun parse(raw: String?): ArticleTarget =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: LAUNCHER
    }
}

enum class ReaderMode {
    READING,
    FULL,
    WEB,
}

object ReaderScale {
    const val MIN: Float = 0.85f
    const val MAX: Float = 1.6f
    const val DEFAULT: Float = 1.0f

    fun clamp(raw: Float): Float = raw.coerceIn(MIN, MAX)
}

enum class ImagePolicy {
    ALWAYS,
    WIFI,
    NEVER,
    ;

    companion object {
        fun parse(raw: String?): ImagePolicy =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: ALWAYS
    }

    fun allowImages(online: Boolean, wifi: Boolean): Boolean =
        when (this) {
            ALWAYS -> online
            WIFI -> online && wifi
            NEVER -> false
        }
}

data class ReaderSettings(
    val target: ArticleTarget = ArticleTarget.LAUNCHER,
    val bodyScale: Float = ReaderScale.DEFAULT,
    val imagePolicy: ImagePolicy = ImagePolicy.ALWAYS,
)
