package ir.furnitureshop.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// ---------------------------------------------------------------------------
// خروجی فاکتور: PDF (A4) و Image (PNG) — فارسی، RTL، خوانا
//
// متن با Canvas رسم می‌شود؛ Android Bidi خودکار، متن فارسی را راست‌به‌چپ و
// با شکل‌دهی درست حروف نمایش می‌دهد (فونت sans-serif با fallback فارسی).
// ---------------------------------------------------------------------------

object InvoiceCanvasRenderer {

    private fun paintFor(style: PrintLineStyle): Paint = when (style) {
        PrintLineStyle.HEADER -> Paint().apply {
            textSize = 38f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            color = Color.rgb(23, 88, 62)
        }
        PrintLineStyle.TITLE -> Paint().apply {
            textSize = 26f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            color = Color.rgb(0, 0, 0)
        }
        PrintLineStyle.NORMAL -> Paint().apply {
            textSize = 22f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            color = Color.rgb(30, 30, 30)
        }
        PrintLineStyle.BOLD -> Paint().apply {
            textSize = 24f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            color = Color.rgb(0, 0, 0)
        }
        PrintLineStyle.SMALL -> Paint().apply {
            textSize = 19f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            color = Color.rgb(110, 110, 110)
        }
    }

    private val headerBgPaint = Paint().apply {
        color = Color.rgb(230, 241, 234)
    }

    private val totalBgPaint = Paint().apply {
        color = Color.rgb(240, 240, 240)
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.rgb(160, 160, 160)
    }

    private fun leftPaintFor(style: PrintLineStyle): Paint = paintFor(style).apply {
        textAlign = Paint.Align.LEFT
    }

    /**
     * رسم خط‌ها روی Canvas — راست‌چین (RTL) با حاشیه.
     * startY: مختصات Y اولین خط؛ خطوط به‌ترتیب پایین می‌روند.
     * دور کل محتوا یک کادر (حاشیه) رسم می‌شود.
     */
    fun render(
        canvas: Canvas,
        lines: List<InvoicePrintLine>,
        width: Int,
        startY: Int,
        lineHeight: Int
    ) {
        if (lines.isEmpty()) return
        val margin = width / 14f
        val right = width - margin
        val totalHeight = lines.size * lineHeight
        // حاشیه دور کل فاکتور
        canvas.drawRect(margin - 10f, startY - 14f, right + 10f, startY + totalHeight + 14f, borderPaint)

        lines.forEachIndexed { index, line ->
            val y = (startY + index * lineHeight).toFloat()
            when {
                line.boxStyle == PrintBoxStyle.HEADER -> {
                    // پس‌زمینه سربرگ
                    canvas.drawRect(margin, y - lineHeight + 16f, right, y + 8f, headerBgPaint)
                    canvas.drawText(line.text, right, y, paintFor(line.style))
                }
                line.boxStyle == PrintBoxStyle.TOTAL -> {
                    // باکس مبلغ نهایی
                    canvas.drawRect(margin, y - lineHeight + 12f, right, y + 10f, totalBgPaint)
                    canvas.drawText(line.text, right, y, paintFor(line.style))
                }
                line.separator -> {
                    val paint = Paint().apply {
                        strokeWidth = 1.5f
                        color = Color.rgb(205, 205, 205)
                    }
                    canvas.drawLine(margin, y, right, y, paint)
                }
                line.rightText != null -> {
                    // ردیف جدول: نام در راست، مبلغ در چپ
                    canvas.drawText(line.rightText, right, y, paintFor(line.style))
                    canvas.drawText(line.leftText ?: "", margin, y, leftPaintFor(line.style))
                }
                else -> {
                    canvas.drawText(line.text, right, y, paintFor(line.style))
                }
            }
        }
    }
}

object InvoicePdfExporter {

    const val PAGE_WIDTH = 595   // A4
    const val PAGE_HEIGHT = 842
    private const val PAGE_MARGIN = 64
    private const val PAGE_LINE_HEIGHT = 40
    private const val MAX_LINES_PER_PAGE =
        (PAGE_HEIGHT - 2 * PAGE_MARGIN) / PAGE_LINE_HEIGHT

    suspend fun export(context: Context, data: InvoicePrintData): File =
        withContext(Dispatchers.IO) {
            val lines = InvoicePrintLayout.buildLines(data)
            val pages = InvoicePrintLayout.paginate(lines, MAX_LINES_PER_PAGE)

            val document = PdfDocument()
            pages.forEach { pageLines ->
                val pageInfo = PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pages.indexOf(pageLines) + 1
                ).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)
                InvoiceCanvasRenderer.render(
                    canvas = canvas,
                    lines = pageLines,
                    width = PAGE_WIDTH,
                    startY = PAGE_MARGIN,
                    lineHeight = PAGE_LINE_HEIGHT
                )
                document.finishPage(page)
            }

            val file = invoiceFile(context, data.invoiceNumber, "pdf")
            file.outputStream().use { document.writeTo(it) }
            document.close()
            file
        }
}

object InvoiceImageExporter {

    private const val IMAGE_WIDTH = 1080
    private const val IMAGE_LINE_HEIGHT = 72
    private const val IMAGE_TOP_PADDING = 96

    suspend fun export(context: Context, data: InvoicePrintData): File =
        withContext(Dispatchers.IO) {
            val lines = InvoicePrintLayout.buildLines(data)
            // ارتفاع بر اساس تعداد واقعی خط‌ها → هیچ محتوایی بریده نمی‌شود
            val height = InvoicePrintLayout.estimatedHeight(lines.size)

            val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            InvoiceCanvasRenderer.render(
                canvas = canvas,
                lines = lines,
                width = IMAGE_WIDTH,
                startY = IMAGE_TOP_PADDING,
                lineHeight = IMAGE_LINE_HEIGHT
            )

            val file = invoiceFile(context, data.invoiceNumber, "png")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            file
        }
}

private fun invoiceFile(context: Context, invoiceNumber: String, ext: String): File {
    val dir = File(context.cacheDir, "invoices")
    dir.mkdirs()
    return File(dir, "invoice_${invoiceNumber.replace("/", "_")}.$ext")
}

/** به‌اشتراک‌گذاری فایل تولیدشده (PDF / Image) — FileProvider */
fun shareInvoiceFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )
    val mime = when (file.extension.lowercase()) {
        "pdf" -> "application/pdf"
        "png", "jpg", "jpeg" -> "image/*"
        else -> "*/*"
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک فاکتور"))
}
