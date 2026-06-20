package bw.alphadirect.pratdroid.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import bw.alphadirect.pratdroid.data.Reminder

object ReminderScheduler {

    fun schedule(context: Context, r: Reminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, r)
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.timeMillis, pi)
        } catch (e: SecurityException) {
            // Exact-alarm permission not granted — fall back to inexact.
            am.set(AlarmManager.RTC_WAKEUP, r.timeMillis, pi)
        }
    }

    fun cancel(context: Context, r: Reminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, r))
    }

    private fun pendingIntent(context: Context, r: Reminder): PendingIntent {
        val i = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("text", r.text)
            putExtra("id", r.id)
        }
        return PendingIntent.getBroadcast(
            context,
            r.id.toInt(),
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
