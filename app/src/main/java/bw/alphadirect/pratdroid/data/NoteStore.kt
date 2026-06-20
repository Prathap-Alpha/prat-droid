package bw.alphadirect.pratdroid.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** JSON-backed notes store in SharedPreferences. Newest first. */
class NoteStore(context: Context) {

    private val prefs = context.getSharedPreferences("prat_notes", Context.MODE_PRIVATE)

    fun all(): List<Note> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Note(o.getLong("id"), o.getString("text"), o.getLong("at"))
        }.sortedByDescending { it.createdAt }
    }

    private fun save(list: List<Note>) {
        val arr = JSONArray()
        list.forEach { n ->
            arr.put(JSONObject().put("id", n.id).put("text", n.text).put("at", n.createdAt))
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    fun add(n: Note) = save(all() + n)

    fun remove(id: Long) = save(all().filterNot { it.id == id })

    fun nextId(): Long = (all().maxOfOrNull { it.id } ?: 0L) + 1
}
