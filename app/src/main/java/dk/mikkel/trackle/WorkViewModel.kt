package dk.mikkel.trackle

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.mikkel.trackle.data.EventType
import dk.mikkel.trackle.data.WorkDatabase
import dk.mikkel.trackle.data.WorkEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
            val last = dao.lastEvent()
            _state.value = WorkUiState(isWorking = last?.type == EventType.KOM)
        }
    }

    /** All events for CSV export (Phase 5). */
    suspend fun allEvents(): List<WorkEvent> = dao.allEvents()
}
