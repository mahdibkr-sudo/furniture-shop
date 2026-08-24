package ir.furnitureshop.app.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * سال مالی انتخابی (شمسی) — سراسری و ماندگار.
 * همه آمار و لیست‌های مالی بر اساس سال انتخابی فیلتر می‌شوند؛
 * مشتریان/تأمین‌کنندگان/اجناس بین سال‌ها مشترک‌اند.
 */
object YearFilter {

    private const val PREFS_NAME = "year_filter"
    private const val KEY_SELECTED_YEAR = "selected_year"

    private var prefs: SharedPreferences? = null

    private val _selectedYear = MutableStateFlow(currentJalaliYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    /** در Application.onCreate صدا زده می‌شود تا سال ذخیره‌شده بازیابی شود. */
    fun init(context: Context) {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        _selectedYear.value = p.getInt(KEY_SELECTED_YEAR, currentJalaliYear())
    }

    fun switchYear(year: Int) {
        val clamped = year.coerceIn(JalaliDate.MIN_YEAR, JalaliDate.MAX_YEAR)
        _selectedYear.value = clamped
        prefs?.edit()?.putInt(KEY_SELECTED_YEAR, clamped)?.apply()
    }

    fun currentJalaliYear(): Int = JalaliDate.fromEpochMillis(System.currentTimeMillis()).first

    /** ابتدای روز اول فروردین سال (نیمه‌شب، برای شامل‌شدن کامل روز اول). */
    fun yearStartMillis(year: Int): Long = JalaliDate.toEpochMillis(year, 1, 1, 0, 0)

    /** ابتدای روز اول فروردین سال بعد (مرز انحصاری پایان سال). */
    fun yearEndMillisExclusive(year: Int): Long = JalaliDate.toEpochMillis(year + 1, 1, 1, 0, 0)

    fun isInYear(epochMillis: Long, year: Int): Boolean =
        epochMillis >= yearStartMillis(year) && epochMillis < yearEndMillisExclusive(year)
}
