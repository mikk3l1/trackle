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
import java.time.LocalDate
import java.time.ZoneId

/** Immutable snapshot of what the UI displays. */
data class WorkUiState(
    val isWorking: Boolean = false,
    val todayTotalSeconds: Long = 0L
)

class WorkViewModel(appContext: Context) : ViewModel() {

    private val appContext = appContext.applicationContext
    private val dao = WorkDatabase.get(appContext).workEventDao()

    private val _state = MutableStateFlow(WorkUiState())
    val state: StateFlow<WorkUiState> = _state

    init {
        refresh()
    }

    /**
     * Recompute UI state from Room. Called when the screen resumes (the widget
     * may have recorded events in the meantime).
     */
    fun refresh() {
        viewModelScope.launch {
            val events = dao.allEvents()
            val zone = ZoneId.systemDefault()
            _state.value = WorkUiState(
                isWorking = events.lastOrNull()?.type == EventType.KOM,
                todayTotalSeconds = WorkStats.totalSecondsForDay(
                    events = events,
                    day = LocalDate.now(zone),
                    zone = zone,
                    nowEpochSeconds = WorkRepository.currentMinuteTimestampSeconds()
                )
            )
        }
    }

    /** All events for CSV export. */
    suspend fun allEvents(): List<WorkEvent> = dao.allEvents()

    /** CSV of all raw events (one row per event), ready to be written to a file. */
    suspend fun csvExport(): String = WorkCsv.toCsv(dao.allEvents(), ZoneId.systemDefault())
}
