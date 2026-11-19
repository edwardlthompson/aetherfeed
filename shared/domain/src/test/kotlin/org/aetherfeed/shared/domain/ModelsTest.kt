package org.aetherfeed.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ModelsTest {
    @Test
    fun moduleKindHasThreeValues() {
        assertEquals(3, ModuleKind.entries.size)
    }

    @Test
    fun feedRoundTripFields() {
        val feed =
            Feed(
                id = "f1",
                title = "Example",
                url = "https://example.test/feed.xml",
                kind = ModuleKind.News,
                updatedAt = 1L,
            )
        assertEquals(ModuleKind.News, feed.kind)
        assertEquals("https://example.test/feed.xml", feed.url)
    }

    @Test
    fun newsHttpConstantsMatchInventory() {
        assertEquals(12_000, NEWS_HTTP_TIMEOUT_MS)
        assertEquals("AetherFeed/1", NEWS_HTTP_USER_AGENT)
        assertEquals(12, NEWS_HTTP_FEED_CAP)
    }

    @Test
    fun readerSettingsDefaultsMatchAndroid() {
        val settings = ReaderSettings()
        assertEquals(ArticleTarget.LAUNCHER, settings.target)
        assertEquals(ReaderScale.DEFAULT, settings.bodyScale)
        assertEquals(ImagePolicy.ALWAYS, settings.imagePolicy)
        assertEquals(ArticleTarget.LAUNCHER, ArticleTarget.parse(null))
        assertEquals(ImagePolicy.WIFI, ImagePolicy.parse("wifi"))
    }
}
