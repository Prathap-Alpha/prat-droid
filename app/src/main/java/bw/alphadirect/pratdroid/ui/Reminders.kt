package bw.alphadirect.pratdroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bw.alphadirect.pratdroid.data.Reminder
import bw.alphadirect.pratdroid.data.ReminderStore
import bw.alphadirect.pratdroid.reminders.ReminderScheduler
import bw.alphadirect.pratdroid.ui.theme.Accent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { ReminderStore(ctx) }
    var items by remember { mutableStateOf(store.all()) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { PratTopBar("Reminders", onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = Accent) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { pad ->
        if (items.isEmpty()) {
            Box(
                Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No reminders yet. Tap +", color = Color(0xFF8E8E93))
            }
        } else {
            LazyColumn(
                Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { r ->
                    ReminderRow(r) {
                        ReminderScheduler.cancel(ctx, r)
                        store.remove(r.id)
                        items = store.all()
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddReminderDialog(
            onDismiss = { showAdd = false },
            onConfirm = { text, time ->
                val r = Reminder(System.currentTimeMillis(), text, time)
                store.add(r)
                ReminderScheduler.schedule(ctx, r)
                items = store.all()
                showAdd = false
            }
        )
    }
}

@Composable
fun ReminderRow(r: Reminder, onDelete: () -> Unit) {
    val time = remember(r.timeMillis) {
        SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault()).format(Date(r.timeMillis))
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    r.text,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(time, fontSize = 12.sp, color = Accent)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFF8E8E93))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onConfirm: (String, Long) -> Unit) {
    var text by remember { mutableStateOf("") }
    var offset by remember { mutableStateOf(60L) } // minutes from now

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) {
                    onConfirm(text, System.currentTimeMillis() + offset * 60_000)
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("New reminder") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("What?") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text("Remind me in:", fontSize = 13.sp, color = Color(0xFF8E8E93))
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15L to "15 min", 60L to "1 hour", 180L to "3 hours", 1440L to "1 day")
                        .forEach { (m, label) ->
                            FilterChip(
                                selected = offset == m,
                                onClick = { offset = m },
                                label = { Text(label) }
                            )
                        }
                }
            }
        }
    )
}
