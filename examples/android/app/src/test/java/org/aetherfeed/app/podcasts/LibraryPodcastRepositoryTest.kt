package org.aetherfeed.app.podcasts

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.domain.Episode
import org.aetherfeed.app.domain.PlaybackPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryPodcastRepositoryTest {
  @Test
  fun enqueueDedupesAndSavePosition() =
      runBlocking {
        val library = InMemoryLibrary()
        val repo = LibraryPodcastRepository(library)
        val episode =
            Episode(
                id = "ep-repo-1",
                showId = "show-1",
                title = "One",
                enclosureUrl = "https://example.test/1.mp3",
            )

        repo.enqueue(episode)
        repo.enqueue(episode)
        assertEquals(listOf("ep-repo-1"), repo.queueSnapshot().episodeIds)

        repo.savePosition(
            PlaybackPosition(
                episodeId = episode.id,
                positionMs = 3_000L,
                durationMs = 60_000L,
                updatedAt = 9L,
            ),
        )
        val saved = library.playback(episode.id)
        assertEquals(3_000L, saved?.positionMs)
        assertEquals(60_000L, saved?.durationMs)
      }
}
