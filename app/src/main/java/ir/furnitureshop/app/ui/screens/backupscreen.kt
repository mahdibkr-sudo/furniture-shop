package ir.furnitureshop.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.MainActivity
import ir.furnitureshop.app.util.BackupManager
import kotlinx.coroutines.launch

/**
 * پشتیبان‌گیری (Backup) — خروجی کامل دیتابیس و بازگردانی آن.
 *
 * - «خروجی گرفتن»: فایل پشتیبان ساخته و برای ارسال (واتساپ/تلگرام/بلوتوث/…) باز می‌شود.
 * - «بازگردانی»: فایل پشتیبان انتخاب و دیتابیس جایگزین می‌شود؛ سپس اپ ری‌استارت می‌شود.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as FurnitureApplication
    val scope = rememberCoroutineScope()

    var exporting by remember { mutableStateOf(false) }
    var importing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showRestartDialog by remember { mutableStateOf(false) }

    val importPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                importing = true
                message = null
                val ok = BackupManager.import(context, app.database, uri)
                importing = false
                if (ok) showRestartDialog = true
                else message = "بازگردانی ناموفق بود — فایل معتبر نیست یا خوانده نشد."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پشتیبان‌گیری") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "خروجی گرفتن (Export)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "یک نسخه کامل از همه داده‌ها (محصولات، مشتریان، فاکتورها، پرداخت‌ها و…) به‌صورت یک فایل می‌سازد که می‌توانید هفتگی برای خودتان بفرستید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                exporting = true
                                message = null
                                val file = BackupManager.export(context, app.database)
                                exporting = false
                                if (file != null) {
                                    BackupManager.shareBackup(context, file)
                                } else {
                                    message = "ساخت فایل پشتیبان ناموفق بود."
                                }
                            }
                        },
                        enabled = !exporting && !importing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (exporting) "در حال ساخت…" else "خروجی گرفتن و ارسال")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "بازگردانی (Import)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "فایل پشتیبان را انتخاب کنید تا همه داده‌ها با آن جایگزین شود. این کار داده‌های فعلی را بازنویسی می‌کند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { importPicker.launch("*/*") },
                        enabled = !exporting && !importing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(if (importing) "در حال بازگردانی…" else "انتخاب فایل پشتیبان")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "نکته: بازگردانی همه داده‌های فعلی را با محتوای فایل پشتیبان جایگزین می‌کند. قبل از بازگردانی، در صورت نیاز یک خروجی از داده‌های فعلی بگیرید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            message?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("بازگردانی کامل شد") },
            text = { Text("برای اعمال داده‌های جدید، اپ باید دوباره راه‌اندازی شود. اکنون راه‌اندازی شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    restartApp(context)
                }) {
                    Text("راه‌اندازی مجدد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("بعداً")
                }
            }
        )
    }
}

private fun restartApp(context: android.content.Context) {
    val intent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}
