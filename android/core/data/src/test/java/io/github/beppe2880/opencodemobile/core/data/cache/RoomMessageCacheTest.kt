package io.github.beppe2880.opencodemobile.core.data.cache

import androidx.room.Room
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** The real Room queries (seq arithmetic, trimming, eviction) on an in-memory database. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomMessageCacheTest {

    private lateinit var db: CacheDatabase
    private lateinit var cache: RoomMessageCache
    private var now = 0L

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), CacheDatabase::class.java).build()
        cache = RoomMessageCache(db.messages(), keepSessions = 2, clock = { ++now })
    }

    @After
    fun tearDown() = db.close()

    private fun entries(vararg ids: String) = ids.map { CachedEntry(it, """{"id":"$it"}""") }

    private suspend fun ids(server: String = "s1", session: String = "ses_1") = cache.read(server, session).entries.map { it.id }

    private suspend fun cursor(server: String = "s1", session: String = "ses_1") = cache.read(server, session).olderCursor

    @Test
    fun `replaceTail stores, keeps up to the anchor and replaces the rest`() = runBlocking {
        cache.write("s1", "ses_1", null, entries("a", "b", "c"), "c1", max = 10)
        assertEquals(listOf("a", "b", "c"), ids())
        assertEquals("c1", cursor())
        assertEquals("""{"id":"b"}""", cache.read("s1", "ses_1").entries[1].json)

        cache.write("s1", "ses_1", "b", entries("c2", "d"), "c1", max = 10)
        assertEquals(listOf("a", "b", "c2", "d"), ids())
        assertEquals("c1", cursor())

        // An anchor that is not stored restarts the session.
        cache.write("s1", "ses_1", "gone", entries("x"), "c9", max = 10)
        assertEquals(listOf("x"), ids())
        assertEquals("c9", cursor())

        // No anchor: everything is replaced.
        cache.write("s1", "ses_1", null, emptyList(), null, max = 10)
        assertEquals(emptyList<String>(), ids())
        assertNull(cursor())
    }

    @Test
    fun `replaceTail beyond the cap keeps the newest entries and drops the cursor`() = runBlocking {
        cache.write("s1", "ses_1", null, entries("a", "b", "c"), "c1", max = 4)
        cache.write("s1", "ses_1", "c", entries("d", "e"), "c1", max = 4)

        assertEquals(listOf("b", "c", "d", "e"), ids())
        assertNull(cursor())

        // Exactly at the cap nothing is dropped.
        cache.write("s1", "ses_2", null, entries("a", "b"), "c2", max = 2)
        assertEquals(listOf("a", "b"), ids(session = "ses_2"))
        assertEquals("c2", cursor(session = "ses_2"))
    }

    @Test
    fun `prepend adds older entries before the stored ones`() = runBlocking {
        cache.write("s1", "ses_1", null, entries("c", "d"), "c1", max = 10)
        cache.prepend("s1", "ses_1", entries("a", "b"), "c0", max = 10)
        assertEquals(listOf("a", "b", "c", "d"), ids())
        assertEquals("c0", cursor())

        // Negative seqs keep their order through another prepend and a later sync.
        cache.prepend("s1", "ses_1", entries("y", "z"), null, max = 10)
        cache.write("s1", "ses_1", "c", entries("n"), null, max = 10)
        assertEquals(listOf("y", "z", "a", "b", "c", "n"), ids())

        // On an empty session the entries start at seq 0.
        cache.prepend("s1", "ses_2", entries("p"), "c5", max = 10)
        cache.write("s1", "ses_2", "p", entries("q"), "c5", max = 10)
        assertEquals(listOf("p", "q"), ids(session = "ses_2"))
    }

    @Test
    fun `prepend at the cap keeps the newest part of the page and drops the cursor`() = runBlocking {
        cache.write("s1", "ses_1", null, entries("c", "d"), "c1", max = 3)
        cache.prepend("s1", "ses_1", entries("a", "b"), "c0", max = 3)

        assertEquals(listOf("b", "c", "d"), ids())
        assertNull(cursor())

        // A full cache takes nothing more.
        cache.prepend("s1", "ses_1", entries("x"), "c9", max = 3)
        assertEquals(listOf("b", "c", "d"), ids())
        assertNull(cursor())
    }

    @Test
    fun `only the most recently synced sessions are kept`() = runBlocking {
        cache.write("s1", "ses_1", null, entries("a"), null, max = 10)
        cache.write("s1", "ses_2", null, entries("b"), null, max = 10)
        cache.write("s2", "ses_9", null, entries("z"), null, max = 10)
        cache.write("s1", "ses_3", null, entries("c"), null, max = 10)

        assertEquals(emptyList<String>(), ids(session = "ses_1"))
        assertEquals(listOf("b"), ids(session = "ses_2"))
        assertEquals(listOf("c"), ids(session = "ses_3"))
        // Other servers are counted separately.
        assertEquals(listOf("z"), ids(server = "s2", session = "ses_9"))
    }

    @Test
    fun `clearServer forgets only that server`() = runBlocking {
        cache.write("s1", "ses_1", null, entries("a", "b"), "c1", max = 10)
        cache.write("s2", "ses_1", null, entries("x"), "c2", max = 10)

        cache.clearServer("s1")

        assertTrue(ids().isEmpty())
        assertNull(cursor())
        assertEquals(listOf("x"), ids(server = "s2"))
        assertEquals("c2", cursor(server = "s2"))
    }
}
