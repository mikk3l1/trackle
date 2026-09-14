package dk.mikkel.trackle

import android.os.Bundle
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dk.mikkel.trackle.ui.HomeScreen
import dk.mikkel.trackle.ui.theme.TrackleTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val workViewModel: WorkViewModel by viewModels {
        viewModelFactory { initializer { WorkViewModel(applicationContext) } }
    }

    /** Standard Android "Save as…" (SAF) for the CSV. */
    private lateinit var saveCsv: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveCsv = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
            if (uri != null) saveCsvToFile(uri)
        }
        enableEdgeToEdge()
        setContent {
            TrackleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val state by workViewModel.state.collectAsState()
                    HomeScreen(
                        state = state,
                        onToggle = { workViewModel.toggleWork() },
                        onExport = {
                            val defaultName = "trackle-" +
                                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv"
                            saveCsv.launch(defaultName)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        workViewModel.refresh()
    }

    /** The widget may have recorded events while the app was in the background. */
    override fun onResume() {
        super.onResume()
        workViewModel.refresh()
    }

    private fun saveCsvToFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(workViewModel.csvExport().toByteArray())
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Kunne ikke eksportere CSV", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}