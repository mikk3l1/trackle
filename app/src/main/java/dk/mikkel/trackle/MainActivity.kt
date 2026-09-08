package dk.mikkel.trackle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dk.mikkel.trackle.ui.HomeScreen
import dk.mikkel.trackle.ui.theme.TrackleTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    private val workViewModel: WorkViewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            initializer { WorkViewModel(applicationContext) }
        })[WorkViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val state by workViewModel.state.collectAsState()
                    HomeScreen(
                        isWorking = state.isWorking,
                        todayTotal = state.todayTotalSeconds,
                        onExport = { /* wired up in Phase 5 */ },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/** Immutable snapshot of what the UI displays. */
data class WorkUiState(
    val isWorking: Boolean = false,
    val todayTotalSeconds: Long = 0L
)

class WorkViewModel(appContext: android.content.Context) : ViewModel() {
    private val appContext = appContext.applicationContext

    private val _state = MutableStateFlow(WorkUiState())
    val state: StateFlow<WorkUiState> = _state

    init {
        // Phase 2 will load this from Room; placeholder state for now.
        viewModelScope.launch { _state.value = WorkUiState() }
    }
}