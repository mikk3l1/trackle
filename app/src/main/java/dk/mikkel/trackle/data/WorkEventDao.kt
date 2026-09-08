package dk.mikkel.trackle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkEventDao {

    @Insert
    suspend fun insert(event: WorkEvent): Long

    /** Most recent event, or null if none. Used to reject KOM→KOM and GÅ→GÅ. */
    @Query("SELECT * FROM work_events ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun lastEvent(): WorkEvent?

    /** All events in insertion order (for CSV export). */
    @Query("SELECT * FROM work_events ORDER BY id ASC")
    suspend fun allEvents(): List<WorkEvent>
}
