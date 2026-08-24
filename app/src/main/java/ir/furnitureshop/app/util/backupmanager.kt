package ir.furnitureshop.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import ir.furnitureshop.app.data.local.AppDatabase
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * پشتیبان‌گیری کامل از دیتابیس محلی (خروجی/بازگردانی) — بدون هیچ وابستگی خارجی.
 *
 * روش: کپی مستقیم فایل SQLite پس از فلاش WAL. همه جدول‌ها و داده‌ها یکجا منتقل می‌شوند.
 * - Export: فایل `.furniturebackup` در cacheDir/backups ساخته و قابل اشتراک‌گذاری می‌شود.
 * - Import: فایل انتخاب‌شده روی دیتابیس کپی می‌شود (سپس اپ ری‌استارت می‌شود).
 */
object BackupManager {

    const val BACKUP_EXTENSION = "furniturebackup"

    /** نام فایل بکاپ — تاریخ شمسی + timestamp (ارقام لاتین برای سازگاری فایل‌سیستم). */
    fun backupFileName(): String {
        val (jy, jm, jd) = JalaliDate.fromEpochMillis(System.currentTimeMillis())
        val mm = jm.toString().padStart(2, '0')
        val dd = jd.toString().padStart(2, '0')
        return "backup_${jy}${mm}${dd}_${System.currentTimeMillis()}.$BACKUP_EXTENSION"
    }

    private fun databaseFile(context: Context): File =
        context.getDatabasePath(AppDatabase.DATABASE_NAME)

    private fun backupDir(context: Context): File =
        File(context.cacheDir, "backups").apply { mkdirs() }

    /**
     * خروجی گرفتن: فلاش WAL (همه داده به فایل اصلی) و کپی دیتابیس.
     * در خطا null برمی‌گردد.
     */
    suspend fun export(context: Context, database: AppDatabase): File? =
        withContext(Dispatchers.IO) {
            runCatching {
                // فلاش کامل تا هیچ داده‌ای در WAL نماند
                database.openHelper.writableDatabase
                    .query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.close() }
                val src = databaseFile(context)
                val dest = File(backupDir(context), backupFileName())
                src.copyTo(dest, overwrite = true)
                dest
            }.getOrNull()
        }

    /**
     * بازگردانی: بستن دیتابیس، پاک‌کردن WAL/SHM، کپی فایل انتخابی روی دیتابیس.
     * پس از موفقیت، اپ باید ری‌استارت شود (در UI انجام می‌شود).
     */
    suspend fun import(context: Context, database: AppDatabase, uri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                // بستن اتصال Room تا فایل آزاد شود
                database.close()
                val dest = databaseFile(context)
                File(dest.absolutePath + "-wal").delete()
                File(dest.absolutePath + "-shm").delete()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext false
                true
            }.getOrDefault(false)
        }

    /** به‌اشتراک‌گذاری فایل بکاپ (واتساپ/تلگرام/بلوتوث/…) — FileProvider */
    fun shareBackup(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "ارسال فایل پشتیبان"))
    }
}
