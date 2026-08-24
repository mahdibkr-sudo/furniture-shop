package ir.furnitureshop.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** حداکثر ضلع تصویر رسید هنگام نمایش — برای جلوگیری از مصرف زیاد حافظه */
private const val MAX_DECODE_DIMENSION = 1024

/**
 * دیکد فایل تصویر رسید محلی؛ به‌ازای هر مسیر یک‌بار کش می‌شود.
 * با downsampling هوشمند، حافظه کم مصرف می‌شود.
 */
@Composable
fun rememberReceiptImage(path: String?): ImageBitmap? =
    remember(path) {
        if (path.isNullOrBlank()) null
        else runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= MAX_DECODE_DIMENSION &&
                bounds.outHeight / (sample * 2) >= MAX_DECODE_DIMENSION
            ) {
                sample *= 2
            }
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeFile(path, options)?.asImageBitmap()
        }.getOrNull()
    }
