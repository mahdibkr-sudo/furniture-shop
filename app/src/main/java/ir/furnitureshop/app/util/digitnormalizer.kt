package ir.furnitureshop.app.util

/** نرمال‌سازی ارقام فارسی/عربی به انگلیسی */
object DigitNormalizer {

    fun normalize(raw: String): String =
        raw.map { c ->
            when (c) {
                in '۰'..'۹' -> '0' + (c - '۰')
                in '٠'..'٩' -> '0' + (c - '٠')
                else -> c
            }
        }.joinToString("")

    /** فقط ارقام ۰-۹ (برای فیلدهای عددی) */
    fun digitsOnly(raw: String): String =
        normalize(raw).filter { it in '0'..'9' }
}
