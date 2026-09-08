package dk.mikkel.trackle.data

/**
 * Keeps the event sequence valid: a new event may only be recorded if it
 * differs from the last one (no KOM→KOM or GÅ→GÅ).
 */
class WorkRepository(private val dao: WorkEventDao) {

    suspend fun recordKom(): Boolean = record(EventType.KOM)

    suspend fun recordGa(): Boolean = record(EventType.GA)

    suspend fun allEvents(): List<WorkEvent> = dao.allEvents()

    private suspend fun record(type: EventType): Boolean {
        val last = dao.lastEvent()
        if (last != null && last.type == type) return false
        dao.insert(
            WorkEvent(
                timestamp = currentMinuteTimestampSeconds(),
                type = type
            )
        )
        return true
    }

    companion object {
        /** "Now" with the seconds ignored, as epoch seconds. */
        fun currentMinuteTimestampSeconds(): Long = (System.currentTimeMillis() / 1000L / 60L) * 60L
    }
}
