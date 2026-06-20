package bw.alphadirect.pratdroid.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import bw.alphadirect.pratdroid.data.ReminderStore

/** Re-arms pending reminders after a reboot. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val now = System.currentTimeMillis()
        ReminderStore(context).all()
            .filter { it.timeMillis > now }
            .forEach { ReminderScheduler.schedule(context, it) }
    }
}
