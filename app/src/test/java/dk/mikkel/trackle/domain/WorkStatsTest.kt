package dk.mikkel.trackle.domain

import dk.mikkel.trackle.data.EventType
import dk.mikkel.trackle.data.WorkEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class WorkStatsTest {

    private val zone = ZoneId.of("Europe/Copenhagen")
    private val day = LocalDate.of(2026, 9, 8)

    private fun event(id: Long, date: LocalDate, time: LocalTime, type: EventType) =
        WorkEvent(
            id = id,
            timestamp = LocalDateTime.of(date, time).atZone(zone).toEpochSecond(),
            type = type
        )

    private fun total(events: List<WorkEvent>, nowEpochSeconds: Long): Long =
        WorkStats.totalSecondsForDay(
            events = events,
            day = day,
            zone = zone,
            nowEpochSeconds = nowEpochSeconds
        )

    private val now = event(9, day, LocalTime.of(17, 0), EventType.KOM).timestamp

    @Test
    fun `no events on the day means zero`() {
        assertEquals(0L, total(emptyList(), now))
    }

    @Test
    fun `single completed period`() {
        val events = listOf(
            event(1, day, LocalTime.of(8, 0), EventType.KOM),
            event(2, day, LocalTime.of(12, 0), EventType.GA)
        )
        assertEquals(4 * 3600L, total(events, now))
    }

    @Test
    fun `multiple periods in one day are summed`() {
        val events = listOf(
            event(1, day, LocalTime.of(8, 0), EventType.KOM),
            event(2, day, LocalTime.of(12, 0), EventType.GA),
            event(3, day, LocalTime.of(13, 0), EventType.KOM),
            event(4, day, LocalTime.of(15, 24), EventType.GA)
        )
        assertEquals(6 * 3600L + 24 * 60L, total(events, now))
    }

    @Test
    fun `active period uses current time as end`() {
        val events = listOf(
            event(1, day, LocalTime.of(8, 0), EventType.KOM),
            event(2, day, LocalTime.of(10, 0), EventType.GA),
            event(3, day, LocalTime.of(12, 0), EventType.KOM)
        )
        val nowAt14 = event(9, day, LocalTime.of(14, 0), EventType.KOM).timestamp
        assertEquals(2 * 3600L + 2 * 3600L, total(events, nowAt14))
    }

    @Test
    fun `events from other days are excluded`() {
        val events = listOf(
            event(1, day.minusDays(1), LocalTime.of(8, 0), EventType.KOM),
            event(2, day.minusDays(1), LocalTime.of(16, 0), EventType.GA),
            event(3, day, LocalTime.of(9, 0), EventType.KOM),
            event(4, day, LocalTime.of(11, 30), EventType.GA)
        )
        assertEquals(2 * 3600L + 30 * 60L, total(events, now))
    }
}
