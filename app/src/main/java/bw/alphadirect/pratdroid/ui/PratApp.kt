package bw.alphadirect.pratdroid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bw.alphadirect.pratdroid.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import bw.alphadirect.pratdroid.cast.ScreenRecordService
import bw.alphadirect.pratdroid.video.VideoActivity
import bw.alphadirect.pratdroid.ui.theme.Accent
import bw.alphadirect.pratdroid.util.Actions

enum class Screen { Splash, Home, Reminders, DayPlan, QuickSend, Notes, Status }

@Composable
fun PratApp() {
    var screen by remember { mutableStateOf(Screen.Splash) }
    when (screen) {
        Screen.Splash -> SplashScreen { screen = Screen.Home }
        Screen.Home -> HomeScreen(onOpen = { screen = it })
        Screen.Reminders -> RemindersScreen(onBack = { screen = Screen.Home })
        Screen.DayPlan -> DayPlanScreen(onBack = { screen = Screen.Home })
        Screen.QuickSend -> QuickSendScreen(onBack = { screen = Screen.Home })
        Screen.Notes -> NotesScreen(onBack = { screen = Screen.Home })
        Screen.Status -> DeviceStatusScreen(onBack = { screen = Screen.Home })
    }
}

private fun setTorch(ctx: Context, on: Boolean): Boolean {
    return try {
        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = cm.cameraIdList.firstOrNull {
            cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return false
        cm.setTorchMode(id, on)
        true
    } catch (e: Exception) {
        false
    }
}

private data class Tile(
    val title: String,
    val sub: String,
    val icon: ImageVector,
    val tint: Color,
    val action: () -> Unit
)

@Composable
fun HomeScreen(onOpen: (Screen) -> Unit) {
    val ctx = LocalContext.current
    val recordLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val i = Intent(ctx, ScreenRecordService::class.java).apply {
                action = ScreenRecordService.ACTION_START
                putExtra(ScreenRecordService.EXTRA_CODE, result.resultCode)
                putExtra(ScreenRecordService.EXTRA_DATA, result.data)
            }
            ContextCompat.startForegroundService(ctx, i)
        }
    }
    var torchOn by remember { mutableStateOf(false) }
    val tiles = listOf(
        Tile("Reminders", "Set & get notified", Icons.Filled.NotificationsActive, Accent) {
            onOpen(Screen.Reminders)
        },
        Tile("Day Plan", "Plan today", Icons.Filled.Today, Color(0xFF34C759)) {
            onOpen(Screen.DayPlan)
        },
        Tile("Schedule Meeting", "Open Thusa", Icons.Filled.EventAvailable, Color(0xFF007AFF)) {
            Actions.openThusa(ctx)
        },
        Tile("Quick Send", "Message templates", Icons.Filled.Send, Color(0xFF5856D6)) {
            onOpen(Screen.QuickSend)
        },
        Tile("Add to Calendar", "Syncs to Outlook", Icons.Filled.CalendarMonth, Color(0xFFFF9500)) {
            Actions.newCalendarEvent(ctx, "New event", System.currentTimeMillis() + 3_600_000)
        },
        Tile("Record Screen", "Capture to a video", Icons.Filled.Cast, Color(0xFFFF2D55)) {
            val mpm = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            recordLauncher.launch(mpm.createScreenCaptureIntent())
        },
        Tile("Notes", "Quick scratchpad", Icons.Filled.EditNote, Color(0xFF00B8A9)) {
            onOpen(Screen.Notes)
        },
        Tile("Flashlight", if (torchOn) "On" else "Off", Icons.Filled.FlashlightOn, Color(0xFFFFB300)) {
            if (setTorch(ctx, !torchOn)) torchOn = !torchOn
        },
        Tile("Device", "Battery & storage", Icons.Filled.PhoneAndroid, Color(0xFF607D8B)) {
            onOpen(Screen.Status)
        },
        Tile("Parked Video", "Phone only", Icons.Filled.Movie, Color(0xFFEC407A)) {
            ctx.startActivity(Intent(ctx, VideoActivity::class.java))
        }
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.splash_photo),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp).clip(CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Dumela, Prathap",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text("Let's plan the day", color = Color(0xFF8E8E93), fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(tiles) { t ->
                    FeatureCard(t.title, t.sub, t.icon, t.tint, t.action)
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    sub: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .height(132.dp)
            .fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(sub, fontSize = 12.sp, color = Color(0xFF8E8E93))
            }
        }
    }
}
