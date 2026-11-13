package org.aetherfeed.app.sync

import kotlinx.coroutines.runBlocking
import org.aetherfeed.app.data.InMemoryLibrary
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakePoolGateway(
    var readResult: PoolClientResult = PoolClientResult.Unavailable("Hermes not installed"),
    var lastWrite: PoolDocument? = null,
) : SubscriptionPoolGateway {
    override fun read(): PoolClientResult = readResult

    override fun write(document: PoolDocument): PoolClientResult {
        lastWrite = document
        return PoolClientResult.Ok(document)
    }
}

class SubscriptionPoolSyncTest {
    @Test
    fun hermesAbsentIsGraceful() =
        runBlocking {
            val library = InMemoryLibrary()
            val gateway = FakePoolGateway()
            val sync = SubscriptionPoolSync(library, gateway)
            sync.pullAndMerge()
            assertTrue(library.feeds().isEmpty())
            assertEquals(null, gateway.lastWrite)
            assertTrue(sync.status.value.message.contains("Hermes"))
        }

    @Test
    fun pullMergesNewsAndBooru() =
        runBlocking {
            val library = InMemoryLibrary()
            library.upsertFeed(
                Feed(
                    id = "booru-1",
                    title = "Dan",
                    url = "https://booru.example",
                    kind = ModuleKind.Booru,
                    updatedAt = 1L,
                ),
            )
            val gateway =
                FakePoolGateway(
                    PoolClientResult.Ok(
                        PoolDocument(
                            updatedAt = 2L,
                            subs =
                                listOf(
                                    PoolSub(
                                        url = "https://news.example/rss",
                                        title = "News",
                                        kind = "news",
                                        updatedAt = 5L,
                                    ),
                                    PoolSub(
                                        url = "https://other.booru/example",
                                        title = "Other",
                                        kind = "booru",
                                        updatedAt = 9L,
                                    ),
                                ),
                        ),
                    ),
                )
            SubscriptionPoolSync(library, gateway).pullAndMerge()
            assertEquals(1, library.feeds().count { it.kind == ModuleKind.News })
            assertEquals(2, library.feeds().count { it.kind == ModuleKind.Booru })
            assertTrue(gateway.lastWrite!!.subs.any { it.kind == "booru" })
        }

    @Test
    fun publishIncludesBooru() =
        runBlocking {
            val library = InMemoryLibrary()
            library.upsertFeed(
                Feed(
                    id = "n1",
                    title = "N",
                    url = "https://n.example/rss",
                    kind = ModuleKind.News,
                    updatedAt = 1L,
                ),
            )
            library.upsertFeed(
                Feed(
                    id = "b1",
                    title = "B",
                    url = "https://b.example",
                    kind = ModuleKind.Booru,
                    updatedAt = 1L,
                ),
            )
            val gateway = FakePoolGateway(PoolClientResult.Ok(PoolDocument()))
            SubscriptionPoolSync(library, gateway).publishFromLocal()
            assertEquals(setOf("news", "booru"), gateway.lastWrite!!.subs.map { it.kind }.toSet())
        }
}
