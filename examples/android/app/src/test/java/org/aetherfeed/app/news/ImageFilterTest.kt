package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageFilterTest {
    @Test
    fun keepsStoryPhotosAndDropsSocialIcons() {
        assertTrue(isContentImage("https://cdn.example/story.jpg"))
        assertFalse(isContentImage("https://www.facebook.com/sharer.php?u=1"))
        assertFalse(isContentImage("https://cdn.example/share.png", 16, 16, "Share"))
    }

    @Test
    fun stripsSkippedImagesAndKeepsDocumentOrder() {
        val html = """<p>Hi</p><img src="https://cdn.example/photo.jpg"><img src="https://facebook.com/share.png" width="16">"""
        val next = dropNonContentImages(html)
        assertTrue(next.contains("https://cdn.example/photo.jpg"))
        assertFalse(next.contains("facebook.com"))
        assertEquals(listOf("https://cdn.example/photo.jpg"), collectImageSrcs(next))
    }
}
