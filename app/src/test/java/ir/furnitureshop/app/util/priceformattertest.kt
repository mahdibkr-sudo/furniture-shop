package ir.furnitureshop.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {

    @Test
    fun `zero formats as zero`() {
        assertEquals("۰", PriceFormatter.format(0))
    }

    @Test
    fun `small number without separator`() {
        assertEquals("۹۹۹", PriceFormatter.format(999))
    }

    @Test
    fun `thousands separators with persian digits`() {
        assertEquals("۱٬۲۵۰٬۰۰۰", PriceFormatter.format(1_250_000))
    }

    @Test
    fun `exact thousands boundary`() {
        assertEquals("۱٬۰۰۰٬۰۰۰", PriceFormatter.format(1_000_000))
    }

    @Test
    fun `negative number`() {
        assertEquals("−۵۰", PriceFormatter.format(-50))
    }

    @Test
    fun `int counts get same thousand separators`() {
        assertEquals("۱٬۲۵۰", PriceFormatter.format(1250))
        assertEquals("۲", PriceFormatter.format(2))
        assertEquals("۰", PriceFormatter.format(0))
    }

    @Test
    fun `meters format with persian decimal separator`() {
        assertEquals("۳٫۵", PriceFormatter.formatMeters(3.5))
        assertEquals("۳", PriceFormatter.formatMeters(3.0))
        assertEquals("۰٫۵", PriceFormatter.formatMeters(0.5))
        assertEquals("۱۲٫۷۵", PriceFormatter.formatMeters(12.75))
    }
}
