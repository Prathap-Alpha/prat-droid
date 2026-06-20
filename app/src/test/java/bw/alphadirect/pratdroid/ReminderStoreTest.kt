package bw.alphadirect.pratdroid

import androidx.test.core.app.ApplicationProvider
import bw.alphadirect.pratdroid.data.Reminder
import bw.alphadirect.pratdroid.data.ReminderStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ReminderStoreTest {

    private fun freshStore() = ReminderStore(ApplicationProvider.getApplicationContext())

    @Before
    fun clear() {
        // each test starts from a clean store
        val s = freshStore()
        s.all().forEach { s.remove(it.id) }
    }

    @Test
    fun emptyByDefault() {
        assertTrue(freshStore().all().isEmpty())
    }

    @Test
    fun addsAndSortsByTimeAscending() {
        val s = freshStore()
        s.add(Reminder(1L, "Call bank", 2_000L))
        s.add(Reminder(2L, "Sign docs", 1_000L))
        val all = s.all()
        assertEquals(2, all.size)
        assertEquals("Sign docs", all[0].text)
        assertEquals("Call bank", all[1].text)
    }

    @Test
    fun persistsAcrossInstances() {
        freshStore().add(Reminder(5L, "Board pack", 100L))
        assertEquals(1, freshStore().all().size)
        assertEquals("Board pack", freshStore().all()[0].text)
    }

    @Test
    fun removesById() {
        val s = freshStore()
        s.add(Reminder(1L, "a", 1L))
        s.add(Reminder(2L, "b", 2L))
        s.remove(1L)
        val all = s.all()
        assertEquals(1, all.size)
        assertEquals(2L, all[0].id)
    }

    @Test
    fun nextIdStartsAtOneAndIncrements() {
        val s = freshStore()
        assertEquals(1L, s.nextId())
        s.add(Reminder(s.nextId(), "first", 10L))
        assertEquals(2L, s.nextId())
        s.add(Reminder(s.nextId(), "second", 20L))
        assertEquals(3L, s.nextId())
    }

    @Test
    fun nextIdNeverCollidesAcrossManyAdds() {
        val s = freshStore()
        repeat(50) { s.add(Reminder(s.nextId(), "r$it", it.toLong())) }
        val ids = s.all().map { it.id }
        assertEquals(50, ids.size)
        assertEquals(ids.size, ids.toSet().size) // all unique
    }
}
