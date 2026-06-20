package bw.alphadirect.pratdroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
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
import bw.alphadirect.pratdroid.data.Note
import bw.alphadirect.pratdroid.data.NoteStore
import bw.alphadirect.pratdroid.ui.theme.Accent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { NoteStore(ctx) }
    var items by remember { mutableStateOf(store.all()) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { PratTopBar("Notes", onBack) },
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
                Text("No notes yet. Tap +", color = Color(0xFF8E8E93))
            }
        } else {
            LazyColumn(
                Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { n ->
                    NoteRow(n) {
                        store.remove(n.id)
                        items = store.all()
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddNoteDialog(
            onDismiss = { showAdd = false },
            onConfirm = { text ->
                store.add(Note(store.nextId(), text, System.currentTimeMillis()))
                items = store.all()
                showAdd = false
            }
        )
    }
}

@Composable
private fun NoteRow(n: Note, onDelete: () -> Unit) {
    val date = remember(n.createdAt) {
        SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault()).format(Date(n.createdAt))
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
                Text(n.text, color = MaterialTheme.colorScheme.onSurface)
                Text(date, fontSize = 12.sp, color = Accent)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFF8E8E93))
            }
        }
    }
}

@Composable
private fun AddNoteDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("New note") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Write something…") }
            )
        }
    )
}
