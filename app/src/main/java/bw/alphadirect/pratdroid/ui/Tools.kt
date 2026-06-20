package bw.alphadirect.pratdroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bw.alphadirect.pratdroid.data.ReminderStore
import bw.alphadirect.pratdroid.ui.theme.Accent
import bw.alphadirect.pratdroid.util.Actions

@Composable
fun QuickSendScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val templates = listOf(
        "Running 10 min late, on my way.",
        "In a meeting, will call you back shortly.",
        "Approved. Please proceed.",
        "Please send me the latest numbers before EOD.",
        "Thank you, received with thanks."
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { PratTopBar("Quick Send", onBack) }
    ) { pad ->
        LazyColumn(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(templates) { t ->
                Surface(
                    onClick = { Actions.sendMessage(ctx, t) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, tint = Accent)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            t,
                            Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayPlanScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { ReminderStore(ctx) }
    val today = remember { store.all() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { PratTopBar("Day Plan", onBack) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    Actions.newCalendarEvent(
                        ctx,
                        "New event",
                        System.currentTimeMillis() + 3_600_000
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.White)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add calendar event")
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Today's reminders",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            if (today.isEmpty()) {
                Text("Nothing scheduled.", color = Color(0xFF8E8E93))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(today) { r -> ReminderRow(r) {} }
                }
            }
        }
    }
}
