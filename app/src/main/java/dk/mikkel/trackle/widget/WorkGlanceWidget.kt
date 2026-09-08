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
import java.time.LocalDate
import java.time.ZoneId

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
        val isWorking = events.lastOrNull()?.type == EventType.KOM
        val now = WorkRepository.currentMinuteTimestampSeconds()
        val todayText = formatDuration(
            WorkStats.totalSecondsForDay(
                events = events,
                day = LocalDate.now(zone),
                zone = zone,
                nowEpochSeconds = now
            )
        )
        provideContent {
            AppWidgetContent(isWorking = isWorking, todayText = todayText)
        }
    }

    @Composable
    private fun AppWidgetContent(isWorking: Boolean, todayText: String) {
        GlanceTheme(
            colors = ColorProviders(lightColorScheme(), darkColorScheme())
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.widgetBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isWorking) "På arbejde" else "Ikke på arbejde",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "I dag: $todayText",
                    style = TextStyle(fontSize = 13.sp),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(8.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(
                        label = "KOM",
                        background = ColorProvider(Color(0xFF2E7D32)), // green = starte (start)
                        onClick = actionSendBroadcast<WorkKomReceiver>(),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    ActionButton(
                        label = "GÅ",
                        background = ColorProvider(Color(0xFFC62828)), // red = slutte (stop)
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
