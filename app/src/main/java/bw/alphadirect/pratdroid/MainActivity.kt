package bw.alphadirect.pratdroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import bw.alphadirect.pratdroid.ui.PratApp
import bw.alphadirect.pratdroid.ui.theme.PratDroidTheme
import bw.alphadirect.pratdroid.util.Permissions

class MainActivity : ComponentActivity() {

    private val requestNotif =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ask for the one runtime permission up-front (only if not already granted).
        // Anything still missing/denied is recoverable in-app via PermissionBanner.
        if (Permissions.needsNotifications(this)) {
            Permissions.notification?.let { requestNotif.launch(it) }
        }
        setContent {
            PratDroidTheme {
                PratApp()
            }
        }
    }
}
