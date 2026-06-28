package bw.alphadirect.pratdroid.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * One place for every runtime / special permission Prat-Droid actually needs, so the
 * app can ask up-front and offer a one-tap path to fix anything the user denied —
 * instead of dead-ending on a "requires permission" message.
 *
 *  • POST_NOTIFICATIONS (Android 13+) — runtime permission; without it reminders are silent.
 *  • Exact alarms        (Android 12+) — special access; without it reminders fire late.
 *
 * Every feature that needs more (e.g. MediaProjection for Record Screen) uses a system
 * consent dialog of its own and needs nothing here.
 */
object Permissions {

    /** The runtime notification permission, or null below Android 13 (auto-granted there). */
    val notification: String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.POST_NOTIFICATIONS
        else null

    fun hasNotifications(ctx: Context): Boolean {
        val perm = notification ?: return true
        return ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
    }

    fun needsNotifications(ctx: Context): Boolean = !hasNotifications(ctx)

    /** True when the OS will let us post exact alarms, so reminders fire on time. */
    fun canScheduleExactAlarms(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    /** Everything the core feature (reminders) needs to work without surprises. */
    fun allEssentialGranted(ctx: Context): Boolean =
        hasNotifications(ctx) && canScheduleExactAlarms(ctx)

    /** Deep-link straight to this app's notification settings (Android 8+). */
    fun notificationSettings(ctx: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Deep-link to the per-app "Alarms & reminders" toggle (Android 12+), else app details. */
    fun exactAlarmSettings(ctx: Context): Intent {
        val i = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData(Uri.fromParts("package", ctx.packageName, null))
        else appDetailsSettings(ctx)
        return i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /** Fallback: the app's own entry in system Settings. */
    fun appDetailsSettings(ctx: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", ctx.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
