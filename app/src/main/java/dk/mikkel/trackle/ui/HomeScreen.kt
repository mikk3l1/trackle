package dk.mikkel.trackle.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Phase 1 skeleton. Status and totals are placeholders until Room exists (Phase 2).
 */
@Composable
fun HomeScreen(
    isWorking: Boolean,
    todayTotal: Long,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = 420.dp)
            .padding(24.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Trackle",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isWorking) "På arbejde" else "Ikke på arbejde",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "I dag: ${formatDuration(todayTotal)}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onExport) {
            Text("Eksportér CSV")
        }
    }
}

/** Formats a duration in seconds as "X t Y min". */
fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return "${hours} t ${minutes} min"
}
