package org.aetherfeed.app.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleNavTest {
    @Test
    fun stepsWithinBounds() {
        assertEquals(0, ArticleNav.step(1, 3, -1))
        assertEquals(2, ArticleNav.step(1, 3, 1))
        assertNull(ArticleNav.step(0, 3, -1))
        assertNull(ArticleNav.step(2, 3, 1))
        assertNull(ArticleNav.step(0, 0, 1))
    }
}
