package dk.mikkel.trackle

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.mikkel.trackle.data.EventType
import dk.mikkel.trackle.data.WorkDatabase
import dk.mikkel.trackle.data.WorkEvent
import dk.mikkel.trackle.data.WorkRepository
import dk.mikkel.trackle.domain.WorkCsv
import dk.mikkel.trackle.domain.WorkStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** Immutable snapshot of what the UI displays. */
data class WorkUiState(
    val isWorking: Boolean = false,
    val todayTotalSeconds: Long = 0L,
    val weekTotalSeconds: Long = 0L,
    val currentSessionStartEpochSeconds: Long? = null,
    val lastEventEpochSeconds: Long? = null,
    val lastEventType: EventType? = null,
    val recentEvents: List<WorkEvent> = emptyList(),
    val todayEventCount: Int = 0
)

class WorkViewModel(appContext: Context) : ViewModel() {

    private val appContext = appContext.applicationContext
    private val dao = WorkDatabase.get(appContext).workEventDao()
    private val repository = WorkRepository(dao)

    private val _state = MutableStateFlow(WorkUiState())
    val state: StateFlow<WorkUiState> = _state

    init {
        refresh()
    }

    /** Toggles between clock-in (KOM) and clock-out (GÅ), honoring the repository's sequence guard. */
    fun toggleWork() {
        viewModelScope.launch {
            val succeeded = if (_state.value.isWorking) {
                repository.recordGa()
            } else {
                repository.recordKom()
            }
            if (succeeded) refresh()
        }
    }

    /**
     * Recompute UI state from Room. Called when the screen resumes (the widget
     * may have recorded events in the meantime).
     */
    fun refresh() {
        viewModelScope.launch {
            val events = dao.allEvents()
            val zone = ZoneId.systemDefault()
            val now = WorkRepository.currentMinuteTimestampSeconds()
            val today = LocalDate.now(zone)
            val last = events.lastOrNull()
            val isWorking = last?.type == EventType.KOM

            // Week total = sum of the current ISO week (Mon–Sun).
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            var weekTotal = 0L
            for (offset in 0L..6L) {
                weekTotal += WorkStats.totalSecondsForDay(events, weekStart.plusDays(offset), zone, now)
            }

            val todayEventCount = events.count { localDayOf(it.timestamp, zone) == today }
            val recentEvents = events.sortedByDescending { it.id }.take(8)

            _state.value = WorkUiState(
                isWorking = isWorking,
                todayTotalSeconds = WorkStats.totalSecondsForDay(events, today, zone, now),
                weekTotalSeconds = weekTotal,
                currentSessionStartEpochSeconds = if (isWorking) last?.timestamp else null,
                lastEventEpochSeconds = last?.timestamp,
                lastEventType = last?.type,
                recentEvents = recentEvents,
                todayEventCount = todayEventCount
            )
        }
    }

    /** All events for CSV export. */
    suspend fun allEvents(): List<WorkEvent> = dao.allEvents()

    /** CSV of all raw events (one row per event), ready to be written to a file. */
    suspend fun csvExport(): String = WorkCsv.toCsv(dao.allEvents(), ZoneId.systemDefault())

    private fun localDayOf(epochSeconds: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochSecond(epochSeconds).atZone(zone).toLocalDate()
}
