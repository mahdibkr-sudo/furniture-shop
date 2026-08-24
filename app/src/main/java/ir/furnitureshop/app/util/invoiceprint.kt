package ir.furnitureshop.app.util

import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem

// ---------------------------------------------------------------------------
// مدل خروجی فاکتور (PDF / Image) — لایه خالص و قابل تست واحد
//
// همه مبالغ از snapshot خود فاکتور می‌آیند (InvoiceItem.unitPrice تاریخی و
// Invoice.totalAmount نهایی)؛ تغییرات بعدی محصولات هیچ اثری روی خروجی ندارد.
// Payment و وضعیت تسویه در خروجی نمایش داده نمی‌شوند.
// ---------------------------------------------------------------------------

data class InvoicePrintItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Long,
    val itemTotal: Long
)

data class InvoicePrintData(
    val invoiceNumber: String,
    val dateText: String,
    val customerName: String,
    val customerPhone: String?,
    val items: List<InvoicePrintItem>,
    /** خط پارچه — وقتی فروشگاه تهیه کرده (مبلغ + نام) یا وقتی مشتری آورده ولی نام پارچه ثبت شده */
    val fabricLine: String?,
    val discount: Long,
    val totalAmount: Long
)

object InvoicePrintFormatter {

    fun build(invoice: Invoice, items: List<InvoiceItem>, customer: Customer?): InvoicePrintData {
        val printItems = items.map { item ->
            InvoicePrintItem(
                name = item.productName.ifEmpty { "محصول #${item.productId}" },
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                itemTotal = item.unitPrice * item.quantity
            )
        }

        // نام پارچه (در صورت ثبت) در هر دو حالت تهیه در خروجی می‌آید.
        // تهیه توسط فروشگاه: «متراژ × قیمت هر متر»؛ فاکتورهای قدیمی فقط مبلغ کل دارند.
        // تهیه توسط مشتری: مبلغی اضافه نمی‌شود؛ فقط اگر نام پارچه ثبت شده باشد خط نام می‌آید.
        val fabricName = invoice.fabricName?.takeIf { it.isNotBlank() }
        val fabricLine = when {
            !invoice.fabricCustomerProvided && invoice.fabricAmount > 0 -> {
                val label = "پارچه" + (fabricName?.let { " ($it)" } ?: "") + " (تهیه‌شده توسط فروشگاه)"
                if (invoice.fabricMeters > 0 && invoice.fabricUnitPrice > 0) {
                    "$label: ${PriceFormatter.formatMeters(invoice.fabricMeters)} متر × ${PriceFormatter.format(invoice.fabricUnitPrice)} تومان = ${PriceFormatter.format(invoice.fabricAmount)} تومان"
                } else {
                    "$label: ${PriceFormatter.format(invoice.fabricAmount)} تومان"
                }
            }
            invoice.fabricCustomerProvided && fabricName != null ->
                "پارچه ($fabricName) — تأمین توسط مشتری"
            else -> null
        }

        return InvoicePrintData(
            invoiceNumber = invoice.invoiceNumber,
            dateText = JalaliDate.formatDate(invoice.date),
            customerName = customer?.name ?: "—",
            customerPhone = customer?.phone?.takeIf { it.isNotBlank() },
            items = printItems,
            fabricLine = fabricLine,
            discount = invoice.discount,
            totalAmount = invoice.totalAmount
        )
    }
}

// ---------------------------------------------------------------------------
// چیدمان خطوط خروجی — مستقل از Canvas تا قابل تست باشد
// ---------------------------------------------------------------------------

enum class PrintLineStyle { HEADER, TITLE, NORMAL, BOLD, SMALL }

/** نوع باکس/پس‌زمینه دور خط */
enum class PrintBoxStyle { NONE, HEADER, TOTAL }

data class InvoicePrintLine(
    val text: String,
    val style: PrintLineStyle,
    val separator: Boolean = false,
    val boxStyle: PrintBoxStyle = PrintBoxStyle.NONE,
    /** متن ستون راست (برای ردیف‌های جدول اقلام) */
    val rightText: String? = null,
    /** متن ستون چپ (برای ردیف‌های جدول اقلام) */
    val leftText: String? = null
)

object InvoicePrintLayout {

    const val LINE_HEIGHT = 44
    const val TOP_PADDING = 64
    const val BOTTOM_PADDING = 64

    fun buildLines(data: InvoicePrintData): List<InvoicePrintLine> = buildList {
        // سربرگ: نام فروشگاه + زیرنویس (در کادر سبز)
        add(InvoicePrintLine("میراکل", PrintLineStyle.HEADER, boxStyle = PrintBoxStyle.HEADER))
        add(InvoicePrintLine("فروشگاه مبل و میز", PrintLineStyle.SMALL))
        add(InvoicePrintLine("", PrintLineStyle.SMALL, separator = true))
        add(InvoicePrintLine("فاکتور فروش", PrintLineStyle.TITLE))
        add(InvoicePrintLine("", PrintLineStyle.SMALL, separator = true))
        add(InvoicePrintLine("شماره فاکتور: ${data.invoiceNumber}", PrintLineStyle.NORMAL))
        add(InvoicePrintLine("تاریخ: ${data.dateText}", PrintLineStyle.NORMAL))
        add(InvoicePrintLine("مشتری: ${data.customerName}", PrintLineStyle.NORMAL))
        data.customerPhone?.let {
            add(InvoicePrintLine("شماره تماس: $it", PrintLineStyle.NORMAL))
        }
        add(InvoicePrintLine("", PrintLineStyle.SMALL, separator = true))

        // جدول اقلام: نام در راست، مبلغ در چپ
        data.items.forEach { item ->
            add(
                InvoicePrintLine(
                    text = "",
                    style = PrintLineStyle.BOLD,
                    rightText = item.name,
                    leftText = PriceFormatter.format(item.itemTotal)
                )
            )
            add(
                InvoicePrintLine(
                    "تعداد: ${PriceFormatter.format(item.quantity)}  —  قیمت واحد: ${PriceFormatter.format(item.unitPrice)}",
                    PrintLineStyle.SMALL
                )
            )
        }

        add(InvoicePrintLine("", PrintLineStyle.SMALL, separator = true))
        data.fabricLine?.let {
            add(InvoicePrintLine(it, PrintLineStyle.NORMAL))
        }
        if (data.discount > 0) {
            add(InvoicePrintLine("تخفیف: ${PriceFormatter.format(data.discount)} تومان", PrintLineStyle.NORMAL))
        }
        add(InvoicePrintLine("مبلغ نهایی: ${PriceFormatter.format(data.totalAmount)} تومان", PrintLineStyle.BOLD, boxStyle = PrintBoxStyle.TOTAL))
    }

    /** ارتفاع موردنیاز برای نمایش همه خط‌ها بدون بریدگی (برای Image) */
    fun estimatedHeight(lineCount: Int): Int =
        TOP_PADDING + lineCount * LINE_HEIGHT + BOTTOM_PADDING

    /** تقسیم خط‌ها به صفحات (برای PDF) */
    fun paginate(lines: List<InvoicePrintLine>, maxLinesPerPage: Int): List<List<InvoicePrintLine>> {
        if (lines.isEmpty() || maxLinesPerPage <= 0) return emptyList()
        return lines.chunked(maxLinesPerPage)
    }
}
