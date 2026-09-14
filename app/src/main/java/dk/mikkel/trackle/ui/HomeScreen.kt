package dk.mikkel.trackle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.mikkel.trackle.WorkUiState
import dk.mikkel.trackle.data.EventType
import dk.mikkel.trackle.data.WorkEvent
import dk.mikkel.trackle.ui.theme.CardSurfaceDark
import dk.mikkel.trackle.ui.theme.CardSurfaceLight
import dk.mikkel.trackle.ui.theme.ClockOutRed
import dk.mikkel.trackle.ui.theme.ClockOutRedDark
import dk.mikkel.trackle.ui.theme.WorkingGreen
import dk.mikkel.trackle.ui.theme.WorkingGreenDark
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The main screen: hero status, the big clock-in/out action, week/today
 * stats and a list of recent events.
 */
@Composable
fun HomeScreen(
    state: WorkUiState,
    onToggle: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            Header()
            Spacer(modifier = Modifier.height(20.dp))
            HeroCard(state)
            Spacer(modifier = Modifier.height(16.dp))
            ClockButton(isWorking = state.isWorking, onClick = onToggle)
            Spacer(modifier = Modifier.height(24.dp))
            StatRow(weekTotal = state.weekTotalSeconds, todayCount = state.todayEventCount)
            Spacer(modifier = Modifier.height(28.dp))
            RecentEvents(events = state.recentEvents)
            Spacer(modifier = Modifier.height(24.dp))
            ExportButton(onExport)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Header() {
    val date = LocalDate
        .now(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE, d. MMMM").withLocale(Locale.forLanguageTag("da")))
    Column {
        Text(
            text = "Trackle",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HeroCard(state: WorkUiState) {
    val dark = isSystemInDarkTheme()
    val working = state.isWorking

    val container = when {
        working -> (if (dark) WorkingGreenDark else WorkingGreen).copy(alpha = 0.14f)
        else -> if (dark) CardSurfaceDark else CardSurfaceLight
    }
    val statusColor = if (working) {
        if (dark) WorkingGreenDark else WorkingGreen
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulseDot(active = working, color = statusColor)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (working) "PÅ ARBEJDE" else "HJEMME",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = formatDuration(state.todayTotalSeconds),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "I dag",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = heroCaption(state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun heroCaption(state: WorkUiState): String {
    return when {
        state.isWorking && state.currentSessionStartEpochSeconds != null ->
            "Kom kl. ${clockTime(state.currentSessionStartEpochSeconds!!)}"
        state.lastEventType == EventType.GA && state.lastEventEpochSeconds != null ->
            "Gik kl. ${clockTime(state.lastEventEpochSeconds!!)}"
        state.lastEventEpochSeconds == null -> "Ikke startet endnu"
        else -> ""
    }
}

@Composable
private fun PulseDot(active: Boolean, color: Color) {
    val dotColor = if (active) color else MaterialTheme.colorScheme.outline
    Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
        if (active) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color.copy(alpha = 0.25f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(dotColor, CircleShape)
        )
    }
}

@Composable
private fun ClockButton(isWorking: Boolean, onClick: () -> Unit) {
    val dark = isSystemInDarkTheme()
    if (isWorking) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) ClockOutRedDark else ClockOutRed,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "GÅ HJEM",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dark) WorkingGreenDark else WorkingGreen,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "KOM PÅ ARBEJDE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatRow(weekTotal: Long, todayCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Denne uge",
            value = formatDuration(weekTotal),
            icon = Icons.Filled.CalendarMonth
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Aktiver i dag",
            value = todayCount.toString(),
            icon = Icons.Filled.History
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    value: String,
    icon: ImageVector
) {
    val dark = isSystemInDarkTheme()
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (dark) CardSurfaceDark else CardSurfaceLight,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentEvents(events: List<WorkEvent>) {
    Column {
        Text(
            text = "Seneste aktiver",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        val dark = isSystemInDarkTheme()
        val surfaceColor = if (dark) CardSurfaceDark else CardSurfaceLight

        Surface(
            shape = MaterialTheme.shapes.large,
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (events.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.WorkOutline,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ingen aktiver endnu. Tryk på “KOM PÅ ARBEJDE” for at starte.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    events.forEachIndexed { index, event ->
                        EventRow(event = event)
                        if (index < events.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: WorkEvent) {
    val isKom = event.type == EventType.KOM
    val badge = if (isKom) WorkingGreen else ClockOutRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(badge.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isKom) Icons.Filled.PlayArrow else Icons.Filled.Stop,
                contentDescription = null,
                tint = badge,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isKom) "KOM" else "GÅ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = fullTimestamp(event.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExportButton(onExport: () -> Unit) {
    OutlinedButton(
        onClick = onExport,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Icon(Icons.Filled.Download, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        Text("Eksportér CSV", style = MaterialTheme.typography.titleMedium)
    }
}

/** Formats a duration in seconds as "X t Y min". */
fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return "${hours} t ${minutes} min"
}

private fun clockTime(epochSeconds: Long): String =
    Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault())
        .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

private fun fullTimestamp(epochSeconds: Long): String =
    Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault())
        .toLocalDateTime().format(
            DateTimeFormatter.ofPattern("d. MMM yyyy, HH:mm").withLocale(Locale.forLanguageTag("da"))
        )
