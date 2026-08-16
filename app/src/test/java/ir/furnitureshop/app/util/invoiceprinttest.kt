package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست خروجی فاکتور (PDF / Image) — لایه خالص محتوا.
 *
 * پوشش سناریوها: چند محصول، تخفیف، پارچه (دو حالت)، متن فارسی،
 * قیمت تاریخی (تغییر محصول بعد از صدور فاکتور اثری ندارد)،
 * عدم نمایش Payment/تسویه، چیدمان و صفحه‌بندی بدون بریدگی.
 */
class InvoicePrintTest {

    private val customer = Customer(id = 1, name = "علی رضایی", phone = "09121234567")

    private fun invoice(
        fabricCustomerProvided: Boolean = false,
        fabricMeters: Double = 0.0,
        fabricUnitPrice: Long = 0,
        fabricAmount: Long = 0,
        discount: Long = 0,
        totalAmount: Long
    ) = Invoice(
        id = 1,
        customerId = 1,
        invoiceNumber = "INV-0001",
        date = 1_700_000_000_000L,
        discount = discount,
        fabricCustomerProvided = fabricCustomerProvided,
        fabricMeters = fabricMeters,
        fabricUnitPrice = fabricUnitPrice,
        fabricAmount = fabricAmount,
        totalAmount = totalAmount
    )

    /** همه متن‌های خط (شامل ستون راست/چپ جدول اقلام). */
    private fun List<InvoicePrintLine>.allText(): String =
        joinToString("\n") { listOfNotNull(it.text, it.rightText, it.leftText).joinToString(" ") }

    // ---- تاریخ فاکتور در خروجی باید شمسی باشد (۱۴۰۲/۰۸/۲۴ = ۱۵ نوامبر ۲۰۲۳) ----

    @Test
    fun `print date is jalali`() {
        val data = InvoicePrintFormatter.build(invoice(totalAmount = 100_000L), emptyList(), null)
        assertEquals("۱۴۰۲/۰۸/۲۴", data.dateText)
        val allText = InvoicePrintLayout.buildLines(data).allText()
        assertTrue(allText.contains("۱۴۰۲/۰۸/۲۴"))
    }

    // ---- سربرگ مینیمال با نام فروشگاه «میراکل» ----

    @Test
    fun `print header has shop name mirakel and minimal layout`() {
        val data = InvoicePrintFormatter.build(invoice(totalAmount = 100_000L), emptyList(), null)
        val lines = InvoicePrintLayout.buildLines(data)
        val allText = lines.allText()

        // نام فروشگاه در سربرگ
        assertTrue(allText.contains("میراکل"))
        assertTrue(allText.contains("فروشگاه مبل و میز"))
        assertTrue(allText.contains("فاکتور فروش"))

        // ترتیب مینیمال: میراکل → فروشگاه مبل و میز → فاکتور فروش
        val first = lines[0].text
        assertEquals("میراکل", first)
        assertEquals("فروشگاه مبل و میز", lines[1].text)
        assertEquals("فاکتور فروش", lines[3].text)
    }

    // ---- سناریوی اصلی: چند محصول + تخفیف + پارچه (ما تهیه کرده‌ایم) + فارسی ----

    @Test
    fun `print data with multiple products discount and shop fabric`() {
        val inv = invoice(
            fabricAmount = 2_000_000,
            discount = 1_000_000,
            totalAmount = 21_000_000
        )
        val items = listOf(
            InvoiceItem(id = 1, invoiceId = 1, productId = 10, productName = "مبل راحتی", quantity = 1, unitPrice = 12_000_000),
            InvoiceItem(id = 2, invoiceId = 1, productId = 20, productName = "میز ناهارخوری", quantity = 2, unitPrice = 4_000_000)
        )

        val data = InvoicePrintFormatter.build(inv, items, customer)

        // اقلام: مبلغ آیتم = قیمت واحد × تعداد (Historical Price از snapshot)
        assertEquals(2, data.items.size)
        assertEquals("مبل راحتی", data.items[0].name)
        assertEquals(12_000_000L, data.items[0].unitPrice)
        assertEquals(12_000_000L, data.items[0].itemTotal)
        assertEquals("میز ناهارخوری", data.items[1].name)
        assertEquals(4_000_000L, data.items[1].unitPrice)
        assertEquals(8_000_000L, data.items[1].itemTotal)

        // پارچه: چون فروشگاه تهیه کرده، خط پارچه با مبلغ نمایش داده می‌شود
        assertNotNull(data.fabricLine)
        assertTrue(data.fabricLine!!.contains("۲٬۰۰۰٬۰۰۰"))

        assertEquals(1_000_000L, data.discount)
        assertEquals(21_000_000L, data.totalAmount)
        assertEquals("INV-0001", data.invoiceNumber)
        assertEquals("علی رضایی", data.customerName)
        assertEquals("09121234567", data.customerPhone)

        // خطوط خروجی (همان خطوطی که PDF و Image رسم می‌کنند)
        val lines = InvoicePrintLayout.buildLines(data)
        val allText = lines.allText()

        assertTrue(allText.contains("شماره فاکتور: INV-0001"))
        assertTrue(allText.contains("مشتری: علی رضایی"))
        assertTrue(allText.contains("شماره تماس: 09121234567"))
        assertTrue(allText.contains("مبل راحتی"))
        assertTrue(allText.contains("میز ناهارخوری"))
        assertTrue(allText.contains("قیمت واحد"))
        assertTrue(allText.contains("پارچه"))
        assertTrue(allText.contains("تخفیف"))
        assertTrue(allText.contains("مبلغ نهایی: ۲۱٬۰۰۰٬۰۰۰ تومان"))
        // متن فارسی
        assertTrue(allText.contains("تومان"))
        assertTrue(allText.contains("فاکتور فروش"))

        // جدول اقلام: نام در ستون راست و مبلغ در ستون چپ همان خط
        val itemRow = lines.first { it.rightText == "مبل راحتی" }
        assertEquals("۱۲٬۰۰۰٬۰۰۰", itemRow.leftText)
    }

    // ---- Payment و وضعیت تسویه در خروجی نیست ----

    @Test
    fun `no payment or settlement line in output`() {
        val inv = invoice(fabricAmount = 2_000_000, discount = 1_000_000, totalAmount = 21_000_000)
        val items = listOf(
            InvoiceItem(id = 1, invoiceId = 1, productId = 10, productName = "مبل راحتی", quantity = 1, unitPrice = 12_000_000)
        )
        val data = InvoicePrintFormatter.build(inv, items, customer)
        val lines = InvoicePrintLayout.buildLines(data)
        val allText = lines.allText()

        assertFalse(allText.contains("پرداخت"))
        assertFalse(allText.contains("تسویه"))
        assertFalse(allText.contains("بدهی"))
        assertFalse(allText.contains("مانده"))
    }

    // ---- پارچه: مشتری آورده → مبلغی اضافه نمی‌شود و نمایش هم نمی‌دهیم ----

    @Test
    fun `customer provided fabric adds no line and no amount`() {
        val inv = invoice(
            fabricCustomerProvided = true,
            fabricAmount = 5_000_000, // ثبت‌شده ولی نباید لحاظ/نمایش داده شود
            totalAmount = 10_000_000
        )
        val items = listOf(
            InvoiceItem(id = 1, invoiceId = 1, productId = 10, productName = "مبل راحتی", quantity = 1, unitPrice = 10_000_000)
        )
        val data = InvoicePrintFormatter.build(inv, items, customer)

        assertNull(data.fabricLine)
        // مبلغ نهایی بدون پارچه است (پارچه اضافه نشده)
        assertEquals(10_000_000L, data.totalAmount)

        val allText = InvoicePrintLayout.buildLines(data).allText()
        assertFalse(allText.contains("پارچه"))
    }

    // ---- بدون پارچه و بدون تخفیف ----

    @Test
    fun `no fabric and no discount produce clean output`() {
        val inv = invoice(totalAmount = 12_000_000)
        val items = listOf(
            InvoiceItem(id = 1, invoiceId = 1, productId = 10, productName = "مبل راحتی", quantity = 1, unitPrice = 12_000_000)
        )
        val data = InvoicePrintFormatter.build(inv, items, customer)

        assertNull(data.fabricLine)
        assertEquals(0L, data.discount)

        val allText = InvoicePrintLayout.buildLines(data).allText()
        assertFalse(allText.contains("پارچه"))
        assertFalse(allText.contains("تخفیف"))
        assertTrue(allText.contains("مبلغ نهایی: ۱۲٬۰۰۰٬۰۰۰ تومان"))
    }

    // ---- Historical Price: فاکتورهای قدیمی بعد از تغییر محصول تغییر نمی‌کنند ----

    @Test
    fun `old invoices keep their snapshot prices after product price change`() {
        // همان productId=10 ولی دو فاکتور در دو زمان با قیمت‌های متفاوت
        val oldItem = InvoiceItem(id = 1, invoiceId = 1, productId = 10, productName = "مبل راحتی", quantity = 1, unitPrice = 10_000_000)
        val newItem = InvoiceItem(id = 2, invoiceId = 2, productId = 10, productName = "مبل راحتی", quantity = 1, unitPrice = 12_500_000)

        val oldData = InvoicePrintFormatter.build(
            invoice(totalAmount = 10_000_000), listOf(oldItem), customer
        )
        val newData = InvoicePrintFormatter.build(
            invoice(totalAmount = 12_500_000), listOf(newItem), customer
        )

        // هر فاکتور دقیقاً قیمت لحظه صدور خودش را حفظ کرده است
        assertEquals(10_000_000L, oldData.items[0].unitPrice)
        assertEquals(10_000_000L, oldData.items[0].itemTotal)
        assertEquals(12_500_000L, newData.items[0].unitPrice)
        assertEquals(12_500_000L, newData.items[0].itemTotal)
    }

    // ---- چیدمان و صفحه‌بندی ----

    @Test
    fun `layout has header item lines and total`() {
        val inv = invoice(fabricAmount = 2_000_000, discount = 1_000_000, totalAmount = 21_000_000)
        val items = listOf(
            InvoiceItem(id = 1, invoiceId = 1, productId = 10, productName = "مبل راحتی", quantity = 1, unitPrice = 12_000_000),
            InvoiceItem(id = 2, invoiceId = 1, productId = 20, productName = "میز ناهارخوری", quantity = 2, unitPrice = 4_000_000)
        )
        val lines = InvoicePrintLayout.buildLines(InvoicePrintFormatter.build(inv, items, customer))

        // هدر + عنوان + اطلاعات + هر آیتم ۲ خط + جداکننده‌ها + جمع
        assertEquals(PrintLineStyle.HEADER, lines[0].style)
        assertEquals("میراکل", lines[0].text)
        assertEquals("فروشگاه مبل و میز", lines[1].text)
        assertEquals("فاکتور فروش", lines[3].text)

        val itemLines = lines.filter { it.style == PrintLineStyle.BOLD }
        // نام دو محصول (در ستون راست) + «مبلغ نهایی» BOLD هستند
        assertEquals(3, itemLines.size)
        assertTrue(itemLines.any { it.rightText == "مبل راحتی" })
        assertTrue(itemLines.any { it.rightText == "میز ناهارخوری" })
        assertTrue(itemLines.any { it.text.contains("مبلغ نهایی") })

        // سربرگ و مبلغ نهایی باکس دارند؛ بقیه بدون باکس
        assertEquals(PrintBoxStyle.HEADER, lines[0].boxStyle)
        assertTrue(lines.any { it.boxStyle == PrintBoxStyle.TOTAL && it.text.contains("مبلغ نهایی") })

        // بدون بریدگی: ارتفاع تخمینی همیشه همه خط‌ها را جا می‌دهد
        val height = InvoicePrintLayout.estimatedHeight(lines.size)
        assertTrue(height >= lines.size * InvoicePrintLayout.LINE_HEIGHT)
    }

    @Test
    fun `paginate splits long invoices into pages without losing lines`() {
        val lines = (1..30).map { InvoicePrintLine("خط $it", PrintLineStyle.NORMAL) }
        val pages = InvoicePrintLayout.paginate(lines, maxLinesPerPage = 15)

        assertEquals(2, pages.size)
        assertEquals(15, pages[0].size)
        assertEquals(15, pages[1].size)
        // ترتیب و تعداد کل حفظ شده (چیزی گم نمی‌شود)
        val flattened = pages.flatten()
        assertEquals(lines.size, flattened.size)
        assertEquals(lines, flattened)
    }

    @Test
    fun `empty or invalid pagination input is safe`() {
        assertEquals(emptyList<List<InvoicePrintLine>>(), InvoicePrintLayout.paginate(emptyList(), 10))
        assertEquals(emptyList<List<InvoicePrintLine>>(), InvoicePrintLayout.paginate(listOf(InvoicePrintLine("x", PrintLineStyle.NORMAL)), 0))
    }

    // ---- پارچه با متراژ و قیمت واحد (فاکتورهای جدید) ----

    @Test
    fun `print fabric line shows meters times unit price`() {
        val inv = invoice(
            fabricMeters = 3.5,
            fabricUnitPrice = 500_000,
            fabricAmount = 1_750_000,
            totalAmount = 21_750_000
        )
        val items = listOf(
            InvoiceItem(id = 1, invoiceId = 1, productId = 10, productName = "مبل مدل A", quantity = 1, unitPrice = 20_000_000)
        )
        val data = InvoicePrintFormatter.build(inv, items, null)
        assertNotNull(data.fabricLine)
        val allText = InvoicePrintLayout.buildLines(data).allText()
        assertTrue(allText.contains("۳٫۵ متر × ۵۰۰٬۰۰۰"))
        assertTrue(allText.contains("۱٬۷۵۰٬۰۰۰"))
    }

    @Test
    fun `print fabric legacy invoice without meters falls back to total amount`() {
        // فاکتورهای قدیمی: فقط مبلغ کل پارچه ذخیره شده
        val inv = invoice(
            fabricMeters = 0.0,
            fabricUnitPrice = 0,
            fabricAmount = 2_000_000,
            totalAmount = 22_000_000
        )
        val data = InvoicePrintFormatter.build(inv, emptyList(), null)
        assertNotNull(data.fabricLine)
        val allText = InvoicePrintLayout.buildLines(data).allText()
        assertTrue(allText.contains("۲٬۰۰۰٬۰۰۰ تومان"))
        assertFalse(allText.contains("متر ×"))
    }
}
