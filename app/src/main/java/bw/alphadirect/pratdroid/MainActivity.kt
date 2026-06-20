package bw.alphadirect.pratdroid

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import bw.alphadirect.pratdroid.ui.PratApp
import bw.alphadirect.pratdroid.ui.theme.PratDroidTheme

class MainActivity : ComponentActivity() {

    private val requestNotif =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotif.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            PratDroidTheme {
                PratApp()
            }
        }
    }
}
