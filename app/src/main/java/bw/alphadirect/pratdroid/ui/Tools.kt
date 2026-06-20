package bw.alphadirect.pratdroid.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import bw.alphadirect.pratdroid.data.ReminderStore
import bw.alphadirect.pratdroid.ui.theme.Accent
import bw.alphadirect.pratdroid.util.Actions
import java.util.Calendar

@Composable
fun QuickSendScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val text = pending
        pending = null
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null && text != null) {
            val number = readNumber(ctx, uri)
            if (number != null) sendMessageTo(ctx, number, text)
            else Toast.makeText(ctx, "Couldn't read that contact's number", Toast.LENGTH_SHORT).show()
        }
    }

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
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Tap a message, pick a contact — it opens WhatsApp (or SMS) ready to send.",
                color = Color(0xFF8E8E93)
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(templates) { t ->
                    Surface(
                        onClick = {
                            pending = t
                            picker.launch(
                                Intent(
                                    Intent.ACTION_PICK,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                )
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Send, contentDescription = null, tint = Accent)
                            Spacer(Modifier.width(14.dp))
                            Text(t, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

private fun readNumber(ctx: Context, uri: Uri): String? {
    ctx.contentResolver.query(
        uri, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, null
    )?.use { c -> if (c.moveToFirst()) return c.getString(0) }
    return null
}

/** Open WhatsApp pre-filled to the chosen number; fall back to SMS if WhatsApp is absent. */
private fun sendMessageTo(ctx: Context, rawNumber: String, text: String) {
    val intl = rawNumber.filter { it.isDigit() }
    val whatsapp = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://wa.me/$intl?text=" + Uri.encode(text))
    )
    try {
        ctx.startActivity(whatsapp)
    } catch (e: Exception) {
        ctx.startActivity(
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$rawNumber")).putExtra("sms_body", text)
        )
    }
}

@Composable
fun DayPlanScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { ReminderStore(ctx) }
    val today = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 24L * 60 * 60 * 1000
        store.all().filter { it.timeMillis in start until end }
    }
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
                    Actions.newCalendarEvent(ctx, "New event", System.currentTimeMillis() + 3_600_000)
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
