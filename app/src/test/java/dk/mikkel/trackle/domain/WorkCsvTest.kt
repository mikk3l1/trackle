package dk.mikkel.trackle.domain

import dk.mikkel.trackle.data.EventType
import dk.mikkel.trackle.data.WorkEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class WorkCsvTest {

    private val zone = ZoneId.of("Europe/Copenhagen")

    private fun event(id: Long, date: LocalDate, time: LocalTime, type: EventType) =
        WorkEvent(
            id = id,
            timestamp = java.time.LocalDateTime.of(date, time).atZone(zone).toEpochSecond(),
            type = type
        )

    @Test
    fun `one row per event with header`() {
        val events = listOf(
            event(1, LocalDate.of(2026, 9, 7), LocalTime.of(7, 9), EventType.KOM),
            event(2, LocalDate.of(2026, 9, 7), LocalTime.of(14, 45), EventType.GA),
            event(3, LocalDate.of(2026, 9, 8), LocalTime.of(8, 0), EventType.KOM)
        )
        assertEquals(
            "ID,Timestamp,Type\n" +
                "1,2026-09-07T07:09,KOM\n" +
                "2,2026-09-07T14:45,GÅ\n" +
                "3,2026-09-08T08:00,KOM\n",
            WorkCsv.toCsv(events, zone)
        )
    }

    @Test
    fun `empty event list is header only`() {
        assertEquals("ID,Timestamp,Type\n", WorkCsv.toCsv(emptyList(), zone))
    }
}
