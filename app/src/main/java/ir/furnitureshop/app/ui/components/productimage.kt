package ir.furnitureshop.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/** حداکثر ضلع تصویر هنگام نمایش — برای جلوگیری از مصرف زیاد حافظه */
private const val MAX_DECODE_DIMENSION = 1024

/**
 * دیکد فایل تصویر محلی محصول؛ به‌ازای هر مسیر یک‌بار کش می‌شود.
 * با خواندن ابعاد واقعی، downsampling هوشمند انجام می‌شود تا حافظه کم مصرف شود
 * و اپ با تصاویر بزرگ کرش نکند.
 */
@Composable
fun rememberProductImage(path: String?): ImageBitmap? =
    remember(path) {
        if (path.isNullOrBlank()) null
        else runCatching {
            // ۱) فقط ابعاد را بخوان (بدون دیکد کامل)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            // ۲) downsampling متناسب با ابعاد هدف
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

/** تصویر محصول با fallback حرف اول نام وقتی تصویری ست نشده. */
@Composable
fun ProductThumbnail(
    name: String,
    imagePath: String?,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberProductImage(imagePath)
    val shape = RoundedCornerShape(8.dp)
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.trim().firstOrNull()?.toString() ?: "؟",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
