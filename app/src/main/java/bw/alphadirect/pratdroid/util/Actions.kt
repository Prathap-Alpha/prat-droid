package bw.alphadirect.pratdroid.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import android.widget.Toast

/** Thin wrappers over system intents — calendar, share, Thusa, cast. */
object Actions {

    const val THUSA_URL = "https://prathap-alpha.github.io/thusa/"

    fun openThusa(ctx: Context) = openUrl(ctx, THUSA_URL)

    fun openUrl(ctx: Context, url: String) {
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /** Opens the device calendar composer pre-filled. Saves to whichever
     *  account is signed in (e.g. the Outlook / M365 account), so it syncs. */
    fun newCalendarEvent(ctx: Context, title: String, beginMillis: Long) {
        val i = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, title)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, beginMillis + 60 * 60 * 1000)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ctx.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            toast(ctx, "No calendar app found")
        }
    }

    /** Share-sheet send: user picks WhatsApp / SMS / email and the recipient. */
    fun sendMessage(ctx: Context, text: String) {
        val i = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
        ctx.startActivity(
            Intent.createChooser(i, "Send via")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /** Opens system cast / screen-mirror picker (Smart View / Chromecast). */
    fun openCastSettings(ctx: Context) {
        try {
            ctx.startActivity(
                Intent(Settings.ACTION_CAST_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            toast(ctx, "Cast not available on this device")
        }
    }

    private fun toast(ctx: Context, m: String) =
        Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show()
}
