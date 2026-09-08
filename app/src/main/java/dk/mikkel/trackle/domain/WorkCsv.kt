package dk.mikkel.trackle.domain

import dk.mikkel.trackle.data.WorkEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * CSV of the raw events — one row per KOM/GÅ event (the source of truth).
 * Format: ID,Timestamp,Type  e.g.  1,2026-09-07T07:09,KOM
 */
object WorkCsv {

    private val timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    fun toCsv(events: List<WorkEvent>, zone: ZoneId): String {
        val sb = StringBuilder("ID,Timestamp,Type\n")
        for (event in events) {
            val timestamp = timestampFormat.format(
                Instant.ofEpochSecond(event.timestamp).atZone(zone).toLocalDateTime()
            )
            sb.append(event.id)
              .append(',')
              .append(timestamp)
              .append(',')
              .append(event.type.display)
              .append('\n')
        }
        return sb.toString()
    }
}
