package ir.furnitureshop.app.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ثبت خطاهای ناگهانی (crash) در فایل محلی تا قابل مشاهده/ارسال باشد.
 *
 * وقتی اپ کرش می‌کند، معمولاً هیچ پیامی در UI دیده نمی‌شود.
 * این کلاس stacktrace را در `filesDir/crash_log.txt` ذخیره می‌کند
 * و صفحه اصلی آن را با دکمه کپی به کاربر نشان می‌دهد.
 */
object CrashLogger {

    private const val FILE_NAME = "crash_log.txt"

    private var context: Context? = null
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    /** نصب handler سراسری — در Application.onCreate صدا زده می‌شود. */
    fun install(appContext: Context) {
        context = appContext.applicationContext
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            write(thread, throwable)
            // ادامه کرش عادی (سیستم اپ را می‌بندد)
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun write(thread: Thread, throwable: Throwable) {
        val ctx = context ?: return
        runCatching {
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val entry = "===== $time | thread=${thread.name} =====\n" +
                throwable.stackTraceToString() + "\n\n"
            val file = File(ctx.filesDir, FILE_NAME)
            file.appendText(entry)
        }
    }

    /** متن کامل آخرین خطاها؛ null یعنی خطایی ثبت نشده است. */
    fun readLog(): String? {
        val ctx = context ?: return null
        return runCatching {
            val file = File(ctx.filesDir, FILE_NAME)
            if (file.exists()) file.readText().trim().ifEmpty { null } else null
        }.getOrNull()
    }

    fun clear() {
        val ctx = context ?: return
        runCatching { File(ctx.filesDir, FILE_NAME).delete() }
    }
}
