package dk.mikkel.trackle.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-renders the widget without recording any events.
 * Useful to force a refresh (e.g. after an app update) from adb or a notification.
 */
class WorkRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.Main).launch {
            WorkGlanceWidget().updateAll(context)
        }
    }
}
