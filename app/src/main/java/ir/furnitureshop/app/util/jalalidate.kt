package ir.furnitureshop.app.util

import java.util.Calendar

/**
 * تاریخ شمسی (جلالی) — پیاده‌سازی خالص و بدون وابستگی خارجی.
 * الگوریتم استاندارد ۳۳ ساله (الگوریتم رایج جلالی، دقیق برای سال‌های ۱۲۴۴ تا ۱۴۷۸ شمسی).
 * ذخیره‌سازی همچنان epoch millis است؛ فقط نمایش/انتخاب شمسی می‌شود.
 */
object JalaliDate {

    /** نام ماه‌های شمسی (۱ تا ۱۲). */
    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    private val FA = "۰۱۲۳۴۵۶۷۸۹"

    /** سال‌های مجاز برای انتخاب تاریخ (شمسی). */
    const val MIN_YEAR = 1380
    const val MAX_YEAR = 1415

    /** روزهای تجمعی میلادی (برای تبدیل‌ها). */
    private val G_DAYS_IN_MONTH = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

    // ---------- تبدیل میلادی ↔ شمسی ----------

    /** تبدیل تاریخ میلادی به شمسی. */
    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gy2 = if (gm > 2) gy + 1 else gy
        val days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 +
            gd + G_DAYS_IN_MONTH[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        var days2 = days % 12053
        jy += 4 * (days2 / 1461)
        days2 %= 1461
        if (days2 > 365) {
            jy += (days2 - 1) / 365
            days2 = (days2 - 1) % 365
        }
        val jm = if (days2 < 186) 1 + days2 / 31 else 7 + (days2 - 186) / 30
        val jd = 1 + (if (days2 < 186) days2 % 31 else (days2 - 186) % 30)
        return Triple(jy, jm, jd)
    }

    /** تبدیل تاریخ شمسی به میلادی. */
    fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        val days = -355668 + 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33) + 3) / 4 + jd +
            if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186
        var gy = 400 * (days / 146097)
        var days2 = days % 146097
        gy += 100 * (days2 / 36524)
        days2 %= 36524
        gy += 4 * (days2 / 1461)
        days2 %= 1461
        if (days2 > 365) {
            gy += (days2 - 1) / 365
            days2 = (days2 - 1) % 365
        }
        var gm = 1
        for (i in 0 until 12) {
            if (days2 < G_DAYS_IN_MONTH[i + 1]) {
                gm = i + 1
                break
            }
        }
        val gd = days2 - G_DAYS_IN_MONTH[gm - 1] + 1
        return Triple(gy, gm, gd)
    }

    // ---------- ابزارها ----------

    /** روزشمار میلادی (برای مقایسه فاصله روزها). */
    private fun dayNumber(gy: Int, gm: Int, gd: Int): Int =
        365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400 + G_DAYS_IN_MONTH[gm - 1] + gd

    /** آیا سال شمسی کبیسه است؟ (بر اساس وجود ۳۰ اسفند). */
    fun isLeap(jy: Int): Boolean {
        val a = toGregorian(jy, 12, 29)
        val b = toGregorian(jy + 1, 1, 1)
        return dayNumber(b.first, b.second, b.third) - dayNumber(a.first, a.second, a.third) == 2
    }

    /** تعداد روزهای ماه شمسی. */
    fun daysInMonth(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isLeap(jy)) 30 else 29
    }

    /** تبدیل epoch millis به تاریخ شمسی (بر اساس timezone دستگاه). */
    fun fromEpochMillis(epochMillis: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        return toJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** تبدیل تاریخ شمسی به epoch millis (ظهر همان روز، برای اجتناب از خطای مرزی روز). */
    fun toEpochMillis(jy: Int, jm: Int, jd: Int, hour: Int = 12, minute: Int = 0): Long {
        val g = toGregorian(jy, jm, jd)
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(g.first, g.second - 1, g.third, hour, minute, 0)
        return cal.timeInMillis
    }

    /** «۱۴۰۵/۰۵/۲۵» — نمایش تاریخ شمسی با ارقام فارسی. */
    fun formatDate(epochMillis: Long): String {
        val (jy, jm, jd) = fromEpochMillis(epochMillis)
        return "${fa(jy)}/${two(jm)}/${two(jd)}"
    }

    /** «۱۴۰۵/۰۵/۲۵ ۱۴:۳۰» — نمایش تاریخ و ساعت شمسی. */
    fun formatDateTime(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        return "${formatDate(epochMillis)} ${two(h)}:${two(m)}"
    }

    /** تبدیل عدد به ارقام فارسی (بدون جداکننده هزارگان). */
    fun fa(n: Int): String = buildString {
        for (c in n.toString()) append(FA[c - '0'])
    }

    private fun two(n: Int): String {
        val s = n.toString().padStart(2, '0')
        return buildString { for (c in s) append(FA[c - '0']) }
    }
}
