package dk.mikkel.trackle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dk.mikkel.trackle.ui.HomeScreen
import dk.mikkel.trackle.ui.theme.TrackleTheme

class MainActivity : ComponentActivity() {
    private val workViewModel: WorkViewModel by viewModels {
        viewModelFactory { initializer { WorkViewModel(applicationContext) } }
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

        workViewModel.refresh()
    }
}