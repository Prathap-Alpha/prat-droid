package bw.alphadirect.pratdroid.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import bw.alphadirect.pratdroid.ui.theme.Accent
import bw.alphadirect.pratdroid.util.Permissions

/**
 * Shows only when something the core features need is still off, with a one-tap path to fix it:
 *   • Notifications denied → re-request, or (if permanently denied) jump to notification settings.
 *   • Exact alarms off     → jump to the "Alarms & reminders" toggle.
 * Re-checks on every resume, so it disappears the moment the user grants from Settings.
 */
@Composable
fun PermissionBanner() {
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // Bump on resume / after a permission result so the cached checks below recompute.
    var refresh by remember { mutableStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val notifOk = remember(refresh) { Permissions.hasNotifications(ctx) }
    val alarmsOk = remember(refresh) { Permissions.canScheduleExactAlarms(ctx) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        refresh++
        val perm = Permissions.notification
        // A no-op launch (permanently denied) returns here with granted=false and no rationale —
        // that's our cue to hand the user the Settings path instead of asking forever.
        if (!granted && perm != null && activity != null &&
            !activity.shouldShowRequestPermissionRationale(perm)
        ) {
            ctx.startActivity(Permissions.notificationSettings(ctx))
        }
    }

    if (notifOk && alarmsOk) return

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Accent.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Accent)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Finish setup",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (!notifOk) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Allow notifications so your reminders actually alert you.",
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93)
                )
                TextButton(onClick = { Permissions.notification?.let { notifLauncher.launch(it) } }) {
                    Text("Turn on notifications")
                }
            }
            if (!alarmsOk) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Allow exact alarms so reminders fire on time.",
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93)
                )
                TextButton(onClick = { ctx.startActivity(Permissions.exactAlarmSettings(ctx)) }) {
                    Text("Allow exact alarms")
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}
