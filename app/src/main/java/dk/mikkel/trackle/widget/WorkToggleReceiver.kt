package dk.mikkel.trackle.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import dk.mikkel.trackle.data.EventType
import dk.mikkel.trackle.data.WorkDatabase
import dk.mikkel.trackle.data.WorkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Receiver for the KOM widget button. */
class WorkKomReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Glance delivers widget button presses as an explicit broadcast targeting
        // this component (no action set). Ignore anything else.
        if (intent.component?.className != WorkKomReceiver::class.java.name) return
        recordEvent(context, EventType.KOM)
    }
}

/** Receiver for the GÅ widget button. */
class WorkGaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.component?.className != WorkGaReceiver::class.java.name) return
        recordEvent(context, EventType.GA)
    }
}

private fun recordEvent(context: Context, type: EventType) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val repository = WorkRepository(WorkDatabase.get(context).workEventDao())
            if (type == EventType.KOM) repository.recordKom() else repository.recordGa()
        } finally {
            WorkGlanceWidget().updateAll(context)
        }
    }
}
