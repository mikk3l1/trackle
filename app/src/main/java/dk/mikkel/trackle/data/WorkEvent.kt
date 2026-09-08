package dk.mikkel.trackle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One KOM or GÅ press. Raw events are the source of truth (see plan Phase 5). */
enum class EventType(val display: String) {
    KOM("KOM"),
    GA("GÅ")
}

@Entity(tableName = "work_events")
data class WorkEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** Epoch seconds, truncated to the minute (seconds are ignored per plan). */
    val timestamp: Long,
    val type: EventType
)
