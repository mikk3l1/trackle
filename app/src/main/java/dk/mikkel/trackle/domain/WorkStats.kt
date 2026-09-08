package dk.mikkel.trackle.domain

import dk.mikkel.trackle.data.EventType
import dk.mikkel.trackle.data.WorkEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Derives work time from the raw events (the source of truth).
 * Each KOM is paired with the following GÅ; an unpaired KOM (work in
 * progress) uses [nowEpochSeconds] instead of a stored end time.
 */
object WorkStats {

    fun totalSecondsForDay(
        events: List<WorkEvent>,
        day: LocalDate,
        zone: ZoneId,
        nowEpochSeconds: Long
    ): Long {
        val dayEvents = events.asSequence()
            .filter { localDayOf(it.timestamp, zone) == day }
            .sortedWith(compareBy<WorkEvent>({ it.timestamp }, { it.id }))
            .toList()

        var total = 0L
        var openKom: Long? = null
        for (event in dayEvents) {
            when (event.type) {
                EventType.KOM -> if (openKom == null) openKom = event.timestamp
                EventType.GA -> {
                    if (openKom != null) {
                        total += event.timestamp - openKom
                        openKom = null
                    }
                }
            }
        }
        if (openKom != null) {
            // Active work period: use current time as end.
            total += (nowEpochSeconds - openKom).coerceAtLeast(0L)
        }
        return total
    }

    private fun localDayOf(epochSeconds: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochSecond(epochSeconds).atZone(zone).toLocalDate()
}
