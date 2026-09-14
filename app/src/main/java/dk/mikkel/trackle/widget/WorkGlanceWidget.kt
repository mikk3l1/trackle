package dk.mikkel.trackle.widget

import android.content.Context
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Spacer
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dk.mikkel.trackle.data.EventType
import dk.mikkel.trackle.data.WorkDatabase
import dk.mikkel.trackle.data.WorkEvent
import dk.mikkel.trackle.data.WorkRepository
import dk.mikkel.trackle.domain.WorkStats
import dk.mikkel.trackle.ui.formatDuration
import dk.mikkel.trackle.ui.theme.CardSurfaceDark
import dk.mikkel.trackle.ui.theme.CardSurfaceLight
import dk.mikkel.trackle.ui.theme.ClockOutRed
import dk.mikkel.trackle.ui.theme.TrackleBlue
import dk.mikkel.trackle.ui.theme.TrackleBlueDark
import dk.mikkel.trackle.ui.theme.WorkingGreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Home-screen widget: current work status, today's total,
 * and KOM / GÅ buttons (recorded directly from the widget).
 */
class WorkGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = WorkRepository(WorkDatabase.get(context).workEventDao()).allEvents()
        render(events)
    }

    private suspend fun render(events: List<WorkEvent>) {
        val zone = ZoneId.systemDefault()
        val last = events.lastOrNull()
        val isWorking = last?.type == EventType.KOM
        val now = WorkRepository.currentMinuteTimestampSeconds()
        val todayText = formatDuration(
            WorkStats.totalSecondsForDay(
                events = events,
                day = LocalDate.now(zone),
                zone = zone,
                nowEpochSeconds = now
            )
        )
        val sessionStartText = if (isWorking && last != null) {
            "Siden " + timeFormatter().format(
                Instant.ofEpochSecond(last.timestamp).atZone(zone).toLocalTime()
            )
        } else null
        provideContent {
            AppWidgetContent(
                isWorking = isWorking,
                todayText = todayText,
                sessionStartText = sessionStartText
            )
        }
    }

    private fun timeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @Composable
    private fun AppWidgetContent(
        isWorking: Boolean,
        todayText: String,
        sessionStartText: String?
    ) {
        GlanceTheme(
            colors = ColorProviders(
                lightColorScheme(
                    primary = TrackleBlue,
                    onSurface = Color(0xFF1F2430),
                    surfaceVariant = CardSurfaceLight,
                    onSurfaceVariant = Color(0xFF4A5165)
                ),
                darkColorScheme(
                    primary = TrackleBlueDark,
                    onSurface = Color(0xFFE8ECF7),
                    surfaceVariant = CardSurfaceDark,
                    onSurfaceVariant = Color(0xFFA5ADC2)
                )
            )
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isWorking) "PÅ ARBEJDE" else "HJEMME",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWorking) ColorProvider(WorkingGreen)
                        else GlanceTheme.colors.onSurface
                    ),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "I dag · $todayText",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    ),
                    maxLines = 1
                )
                if (sessionStartText != null) {
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = sessionStartText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
                Spacer(GlanceModifier.height(10.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(
                        label = "KOM",
                        background = ColorProvider(WorkingGreen), // brand green = clock in
                        onClick = actionSendBroadcast<WorkKomReceiver>(),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    ActionButton(
                        label = "GÅ",
                        background = ColorProvider(ClockOutRed), // brand red = clock out
                        onClick = actionSendBroadcast<WorkGaReceiver>(),
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }

    @Composable
    private fun ActionButton(
        label: String,
        background: ColorProvider,
        onClick: androidx.glance.action.Action,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Box(
            modifier = modifier
                .height(36.dp)
                .background(background)
                .clickable(onClick = onClick)
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                ),
                maxLines = 1
            )
        }
    }
}
