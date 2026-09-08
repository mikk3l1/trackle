package dk.mikkel.trackle.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryWorkEventDao : WorkEventDao {
    private val events = mutableListOf<WorkEvent>()
    private var nextId = 1L

    override suspend fun insert(event: WorkEvent): Long {
        events.add(event.copy(id = nextId++))
        return nextId - 1L
    }

    override suspend fun lastEvent(): WorkEvent? =
        events.maxWithOrNull(compareBy<WorkEvent>({ it.timestamp }, { it.id }))

    override suspend fun allEvents(): List<WorkEvent> = events.toList()
}

class WorkRepositoryTest {

    @Test
    fun `first event is always recorded`() = runBlocking {
        val repo = WorkRepository(InMemoryWorkEventDao())
        assertTrue(repo.recordKom())
    }

    @Test
    fun `KOM after KOM is rejected`() = runBlocking {
        val dao = InMemoryWorkEventDao()
        val repo = WorkRepository(dao)
        assertTrue(repo.recordKom())
        assertFalse(repo.recordKom())
        assertEquals(1, dao.allEvents().size)
    }

    @Test
    fun `GA after GA is rejected`() = runBlocking {
        val dao = InMemoryWorkEventDao()
        val repo = WorkRepository(dao)
        assertTrue(repo.recordGa())
        assertFalse(repo.recordGa())
        assertEquals(1, dao.allEvents().size)
    }

    @Test
    fun `multiple periods in a day are supported`() = runBlocking {
        val dao = InMemoryWorkEventDao()
        val repo = WorkRepository(dao)
        assertTrue(repo.recordKom())
        assertTrue(repo.recordGa())
        assertTrue(repo.recordKom())
        assertTrue(repo.recordGa())
        val types = dao.allEvents().map { it.type }
        assertEquals(listOf(EventType.KOM, EventType.GA, EventType.KOM, EventType.GA), types)
    }

    @Test
    fun `stored timestamp ignores seconds`() = runBlocking {
        val dao = InMemoryWorkEventDao()
        val repo = WorkRepository(dao)
        assertTrue(repo.recordKom())
        val stored = dao.allEvents().first()
        assertTrue(stored.timestamp % 60L == 0L)
        val driftMinutes = kotlin.math.abs(System.currentTimeMillis() / 60000L - stored.timestamp / 60L)
        assertTrue(driftMinutes <= 2L)
    }
}
