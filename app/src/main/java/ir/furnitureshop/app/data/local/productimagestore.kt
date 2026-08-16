package ir.furnitureshop.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * ذخیره‌سازی محلی تصاویر محصولات در حافظه داخلی اپ
 * (بدون نیاز به هیچ permission — با content:// کپی می‌شود).
 * تصویر قبل از ذخیره به حداکثر ۱۰۲۴ پیکسل کوچک می‌شود تا حافظه کم مصرف شود.
 */
class ProductImageStore(private val context: Context) {

    companion object {
        private const val MAX_DIMENSION = 1024
        private const val JPEG_QUALITY = 85
    }

    private val imagesDir: File
        get() = File(context.filesDir, "product_images").apply { mkdirs() }

    /**
     * کپی تصویر از content:// به حافظه داخلی (با کوچک‌سازی) و برگرداندن مسیر مطلق؛
     * در خطا null برمی‌گردد.
     */
    fun saveImage(uri: Uri): String? = runCatching {
        val fileName = "product_${System.currentTimeMillis()}.jpg"
        val dest = File(imagesDir, fileName)
        val input = context.contentResolver.openInputStream(uri) ?: return null
        input.use { ins ->
            // ۱) فقط ابعاد را بخوان
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(ins, null, bounds)
        }
        val (width, height) = boundsSize(uri)
        if (width <= 0 || height <= 0) return null

        // ۲) نسبت کوچک‌سازی
        val maxSide = maxOf(width, height)
        var sample = 1
        while (maxSide / (sample * 2) >= MAX_DIMENSION) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { ins2 ->
            BitmapFactory.decodeStream(ins2, null, options)
        } ?: return null

        // ۳) اگر هنوز بزرگ‌تر از حد است، مقیاس کن (برای ابعاد غیرقابل تقسیم بر ۲)
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

    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}
