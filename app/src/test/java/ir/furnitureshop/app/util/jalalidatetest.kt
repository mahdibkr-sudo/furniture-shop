package ir.furnitureshop.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class JalaliDateTest {

    /** ساخت epoch millis محلی (هم‌زمان‌zone با formatDate تا تست روی هر دستگاه پایدار باشد). */
    private fun millis(y: Int, m: Int, d: Int, h: Int = 12, minute: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(y, m - 1, d, h, minute, 0)
        return cal.timeInMillis
    }

    @Test
    fun `known gregorian to jalali dates`() {
        // ۱ فروردین ۱۴۰۴ = ۲۱ مارس ۲۰۲۵
        assertEquals(Triple(1404, 1, 1), JalaliDate.toJalali(2025, 3, 21))
        // ۱ فروردین ۱۴۰۵ = ۲۱ مارس ۲۰۲۶
        assertEquals(Triple(1405, 1, 1), JalaliDate.toJalali(2026, 3, 21))
        // ۲۵ مرداد ۱۴۰۵ = ۱۶ اوت ۲۰۲۶
        assertEquals(Triple(1405, 5, 25), JalaliDate.toJalali(2026, 8, 16))
        // ۱ فروردین ۱۴۰۳ (سال کبیسه) = ۲۰ مارس ۲۰۲۴
        assertEquals(Triple(1403, 1, 1), JalaliDate.toJalali(2024, 3, 20))
        // ۳۰ اسفند ۱۴۰۳ = ۲۰ مارس ۲۰۲۵ (اسفند کبیسه)
        assertEquals(Triple(1403, 12, 30), JalaliDate.toJalali(2025, 3, 20))
        // ۲۹ اسفند ۱۴۰۳ = ۱۹ مارس ۲۰۲۵
        assertEquals(Triple(1403, 12, 29), JalaliDate.toJalali(2025, 3, 19))
        // ۲۹ اسفند ۱۴۰۴ = ۲۰ مارس ۲۰۲۶ (سال عادی)
        assertEquals(Triple(1404, 12, 29), JalaliDate.toJalali(2026, 3, 20))
    }

    @Test
    fun `jalali to gregorian roundtrip`() {
        // دور کامل: شمسی → میلادی → شمسی
        val back = JalaliDate.toGregorian(1405, 5, 25)
        assertEquals(Triple(2026, 8, 16), back)
        assertEquals(Triple(1405, 5, 25), JalaliDate.toJalali(back.first, back.second, back.third))
    }

    @Test
    fun `formatDate produces persian jalali text`() {
        assertEquals("۱۴۰۵/۰۵/۲۵", JalaliDate.formatDate(millis(2026, 8, 16)))
        assertEquals("۱۴۰۴/۰۱/۰۱", JalaliDate.formatDate(millis(2025, 3, 21)))
        assertEquals("۱۴۰۳/۱۲/۳۰", JalaliDate.formatDate(millis(2025, 3, 20)))
    }

    @Test
    fun `formatDateTime includes time`() {
        val s = JalaliDate.formatDateTime(millis(2026, 8, 16, 14, 30))
        assertTrue(s.startsWith("۱۴۰۵/۰۵/۲۵"))
        assertTrue(s.contains("۱۴:۳۰"))
    }

    @Test
    fun `leap years and month lengths`() {
        assertTrue(JalaliDate.isLeap(1403))
        assertFalse(JalaliDate.isLeap(1404))
        assertFalse(JalaliDate.isLeap(1405))

        assertEquals(30, JalaliDate.daysInMonth(1403, 12)) // اسفند کبیسه
        assertEquals(29, JalaliDate.daysInMonth(1404, 12))
        assertEquals(31, JalaliDate.daysInMonth(1405, 1))
        assertEquals(31, JalaliDate.daysInMonth(1405, 6))
        assertEquals(30, JalaliDate.daysInMonth(1405, 7))
        assertEquals(30, JalaliDate.daysInMonth(1405, 11))
    }

    @Test
    fun `month names are 12 persian names`() {
        assertEquals(12, JalaliDate.MONTH_NAMES.size)
        assertEquals("فروردین", JalaliDate.MONTH_NAMES[0])
        assertEquals("اسفند", JalaliDate.MONTH_NAMES[11])
    }

    @Test
    fun `epoch roundtrip keeps same day`() {
        val original = millis(2026, 8, 16)
        val (jy, jm, jd) = JalaliDate.fromEpochMillis(original)
        val back = JalaliDate.toEpochMillis(jy, jm, jd)
        assertEquals("۱۴۰۵/۰۵/۲۵", JalaliDate.formatDate(back))
    }
}
