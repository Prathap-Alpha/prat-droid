package bw.alphadirect.pratdroid

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import bw.alphadirect.pratdroid.util.Permissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PermissionsTest {

    private val app: Application get() = ApplicationProvider.getApplicationContext()

    private fun setNotif(granted: Boolean) {
        val shadow = Shadows.shadowOf(app)
        if (granted) shadow.grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        else shadow.denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    // Robolectric 4.10.3's ShadowAlarmManager has no exact-alarm setter (added in 4.11),
    // so we read the OS value rather than force it.
    private fun osExactAlarms(): Boolean {
        val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    @Test
    fun notificationPermissionIsPostNotificationsOnApi33() {
        assertEquals(Manifest.permission.POST_NOTIFICATIONS, Permissions.notification)
    }

    @Test
    fun deniedNotificationsReportsNeedsTrue() {
        setNotif(false)
        assertFalse(Permissions.hasNotifications(app))
        assertTrue(Permissions.needsNotifications(app))
    }

    @Test
    fun grantedNotificationsReportsNeedsFalse() {
        setNotif(true)
        assertTrue(Permissions.hasNotifications(app))
        assertFalse(Permissions.needsNotifications(app))
    }

    @Test
    fun canScheduleExactAlarmsDelegatesToOs() {
        assertEquals(osExactAlarms(), Permissions.canScheduleExactAlarms(app))
    }

    @Test
    fun allEssentialGrantedRequiresBoth() {
        // Denied notifications => never essential-granted, regardless of exact-alarm state.
        setNotif(false)
        assertFalse(Permissions.allEssentialGranted(app))
        // Granted notifications => essential-granted tracks the exact-alarm state exactly.
        setNotif(true)
        assertEquals(osExactAlarms(), Permissions.allEssentialGranted(app))
    }

    @Test
    fun notificationSettingsIntentTargetsThisApp() {
        val i = Permissions.notificationSettings(app)
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, i.action)
        assertEquals(app.packageName, i.getStringExtra(Settings.EXTRA_APP_PACKAGE))
    }

    @Test
    fun exactAlarmSettingsIntentTargetsThisAppOnApi33() {
        val i = Permissions.exactAlarmSettings(app)
        assertEquals(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, i.action)
        assertEquals("package:${app.packageName}", i.data?.toString())
    }

    @Test
    fun appDetailsSettingsIntentTargetsThisApp() {
        val i = Permissions.appDetailsSettings(app)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, i.action)
        assertEquals("package:${app.packageName}", i.data?.toString())
    }
}
