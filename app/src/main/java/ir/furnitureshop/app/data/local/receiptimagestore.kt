package ir.furnitureshop.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * ذخیره‌سازی محلی تصویر رسید پرداخت نقدی تأمین‌کننده در حافظه داخلی اپ.
 *
 * الگوی دقیقاً مشابه ProductImageStore است (معماری فعلی پروژه):
 * - بدون permission اضافه — تصویر با content:// کپی می‌شود.
 * - قبل از ذخیره downsampling می‌شود تا با تصاویر بزرگ کرش یا OOM نشود.
 * - در صورت خطا null برمی‌گردد و تراکنش مالی نامعتبر نمی‌شود.
 *
 * تصویر اختیاری است؛ حذف/عدم‌دسترسی فایل اثری روی خودِ پرداخت ندارد.
 */
class ReceiptImageStore(private val context: Context) {

    companion object {
        private const val MAX_DIMENSION = 1024
        private const val JPEG_QUALITY = 85
    }

    private val imagesDir: File
        get() = File(context.filesDir, "receipt_images").apply { mkdirs() }

    /**
     * کپی تصویر از content:// به حافظ آپ (با کوچک‌سازی) و برگرداندن مسیر مطلق؛
     * در خطا null برمی‌گردد.
     */
    fun saveImage(uri: Uri): String? = runCatching {
        val fileName = "receipt_${System.currentTimeMillis()}.jpg"
        val dest = File(imagesDir, fileName)
        val (width, height) = boundsSize(uri)
        if (width <= 0 || height <= 0) return null

        // نسبت کوچک‌سازی (فقط قدرت‌های ۲)
        val maxSide = maxOf(width, height)
        var sample = 1
        while (maxSide / (sample * 2) >= MAX_DIMENSION) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { ins ->
            BitmapFactory.decodeStream(ins, null, options)
        } ?: return null

        // اگر هنوز بزرگتر از حد است، مقیاس کن (ابعاد غیرقابل تقسیم بر ۲)
        val scaled = if (maxOf(bitmap.width, bitmap.height) > MAX_DIMENSION) {
            val scale = MAX_DIMENSION.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true
            ).also { if (it != bitmap) bitmap.recycle() }
        } else bitmap

        dest.outputStream().use { outs ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outs)
        }
        if (scaled != bitmap) scaled.recycle()
        dest.absolutePath
    }.getOrNull()

    /** خواندن ابعاد واقعی تصویر (بدون دیکد کامل). */
    private fun boundsSize(uri: Uri): Pair<Int, Int> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { ins ->
            BitmapFactory.decodeStream(ins, null, bounds)
        }
        return bounds.outWidth to bounds.outHeight
    }

    /**
     * حذف فایل تصویر رسید.
     * مسیر خالی/نال نادیده گرفته می‌شود و هیچ خطایی پرتاب نمی‌شود.
     * حذف فایل تصویر اثری روی خودِ رکورد پرداخت ندارد.
     */
    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}
