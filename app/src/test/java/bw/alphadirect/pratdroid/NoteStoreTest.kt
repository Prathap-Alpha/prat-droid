package bw.alphadirect.pratdroid

import androidx.test.core.app.ApplicationProvider
import bw.alphadirect.pratdroid.data.Note
import bw.alphadirect.pratdroid.data.NoteStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NoteStoreTest {

    private fun store() = NoteStore(ApplicationProvider.getApplicationContext())

    @Before
    fun clear() {
        val s = store()
        s.all().forEach { s.remove(it.id) }
    }

    @Test
    fun emptyByDefault() {
        assertTrue(store().all().isEmpty())
    }

    @Test
    fun newestFirst() {
        val s = store()
        s.add(Note(1L, "older", 1_000L))
        s.add(Note(2L, "newer", 2_000L))
        val all = s.all()
        assertEquals("newer", all[0].text)
        assertEquals("older", all[1].text)
    }

    @Test
    fun persistsAndRemoves() {
        store().add(Note(7L, "keep", 5L))
        assertEquals(1, store().all().size)
        store().remove(7L)
        assertTrue(store().all().isEmpty())
    }

    @Test
    fun nextIdIncrements() {
        val s = store()
        assertEquals(1L, s.nextId())
        s.add(Note(s.nextId(), "a", 1L))
        assertEquals(2L, s.nextId())
    }
}
