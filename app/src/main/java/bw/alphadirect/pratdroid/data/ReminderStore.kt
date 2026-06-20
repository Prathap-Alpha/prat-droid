package bw.alphadirect.pratdroid.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Tiny JSON-backed store in SharedPreferences. No external deps. */
class ReminderStore(context: Context) {

    private val prefs =
        context.getSharedPreferences("prat_reminders", Context.MODE_PRIVATE)

    fun all(): List<Reminder> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Reminder(o.getLong("id"), o.getString("text"), o.getLong("time"))
        }.sortedBy { it.timeMillis }
    }

    private fun save(list: List<Reminder>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("id", r.id)
                    .put("text", r.text)
                    .put("time", r.timeMillis)
            )
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    fun add(r: Reminder) = save(all() + r)

    fun remove(id: Long) = save(all().filterNot { it.id == id })
}
