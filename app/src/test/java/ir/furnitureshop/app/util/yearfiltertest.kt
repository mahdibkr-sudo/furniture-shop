package ir.furnitureshop.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** تست مرزهای بازه سال شمسی — شامل سال کبیسه ۱۴۰۳. */
class YearFilterTest {

    @Test
    fun `first and last day of year are in year`() {
        val first = JalaliDate.toEpochMillis(1405, 1, 1)
        val last = JalaliDate.toEpochMillis(1405, 12, JalaliDate.daysInMonth(1405, 12))
        assertTrue(YearFilter.isInYear(first, 1405))
        assertTrue(YearFilter.isInYear(last, 1405))
    }

    @Test
    fun `day before year start and day after year end are excluded`() {
        val before = JalaliDate.toEpochMillis(1404, 12, 29)
        val after = JalaliDate.toEpochMillis(1406, 1, 1)
        assertFalse(YearFilter.isInYear(before, 1405))
        assertFalse(YearFilter.isInYear(after, 1405))
    }

    @Test
    fun `leap year esfand 30 belongs to that year`() {
        // ۱۴۰۳ کبیسه است — ۳۰ اسفند باید در همان سال بماند
        assertTrue(JalaliDate.isLeap(1403))
        val esfand30 = JalaliDate.toEpochMillis(1403, 12, 30)
        assertTrue(YearFilter.isInYear(esfand30, 1403))
        assertFalse(YearFilter.isInYear(esfand30, 1404))
    }

    @Test
    fun `year range is contiguous and non-overlapping`() {
        for (year in 1400..1410) {
            assertEquals(
                YearFilter.yearStartMillis(year + 1),
                YearFilter.yearEndMillisExclusive(year)
            )
        }
    }

    @Test
    fun `currentJalaliYear derives from today`() {
        val now = System.currentTimeMillis()
        assertEquals(JalaliDate.fromEpochMillis(now).first, YearFilter.currentJalaliYear())
    }
}
