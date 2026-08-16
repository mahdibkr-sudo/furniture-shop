package ir.furnitureshop.app.util

/**
 * فرمت‌کردن مبالغ با ارقام فارسی و جداکننده هزارگان.
 * قطعی و بدون وابستگی به Locale تا قابل تست واحد باشد.
 */
object PriceFormatter {

    private val FA_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

    fun format(amount: Long): String {
        val negative = amount < 0
        val digits = kotlin.math.abs(amount).toString()
        val sb = StringBuilder()
        for (i in digits.indices) {
            if (i > 0 && (digits.length - i) % 3 == 0) sb.append('٬')
            sb.append(FA_DIGITS[digits[i] - '0'])
        }
        return (if (negative) "−" else "") + sb.toString()
    }

    /** تعداد/شمارش‌ها هم با همان جداکننده هزارگان نمایش داده می‌شوند. */
    fun format(value: Int): String = format(value.toLong())

    /** متراژ اعشاری با ارقام فارسی: ۳٫۵ ، ۳ (بدون جداکننده هزارگان). */
    fun formatMeters(meters: Double): String {
        val s = if (meters == meters.toLong().toDouble()) {
            meters.toLong().toString()
        } else {
            var t = String.format(java.util.Locale.US, "%.2f", meters).trimEnd('0').trimEnd('.')
            if (t == "-0") t = "0"
            t
        }
        return buildString {
            for (c in s) {
                append(when (c) {
                    '.' -> '٫'
                    '-' -> '−'
                    else -> FA_DIGITS[c - '0']
                })
            }
        }
    }
}
