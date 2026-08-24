package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.ui.components.YearSwitcher
import ir.furnitureshop.app.navigation.HOME_MODULES
import ir.furnitureshop.app.navigation.HomeModuleData
import ir.furnitureshop.app.navigation.Routes
import ir.furnitureshop.app.util.CrashLogger
import ir.furnitureshop.app.viewmodel.HomeViewModel

/**
 * آیکون هر ماژول — فقط در لایه UI (داده‌ی خالص در navigation/HomeModules.kt است).
 */
private fun HomeModuleData.icon(): ImageVector = when (route) {
    Routes.PRODUCTS -> Icons.AutoMirrored.Filled.List
    Routes.CUSTOMERS -> Icons.Filled.Person
    Routes.SUPPLIERS -> Icons.Filled.Face
    Routes.INVOICES -> Icons.Filled.Create
    Routes.CHECKS -> Icons.Filled.DateRange
    Routes.CASH_PAYMENTS -> Icons.Filled.AccountBalanceWallet
    Routes.EXPENSES -> Icons.Filled.ShoppingCart
    Routes.FINANCIAL_OVERVIEW -> Icons.Filled.DateRange
    Routes.BACKUP -> Icons.Filled.Settings
    else -> Icons.Filled.Create
}

/**
 * صفحه اصلی — Dashboard سبک و فقط ناوبری به ۷ بخش اصلی (بدون آمار).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- نمایش خطاهای ناگهانی قبلی (CrashLogger) ---
    val crashLog = remember { CrashLogger.readLog() }
    var showCrashLog by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val appContext = LocalContext.current.applicationContext

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.appName) },
                actions = {
                    // سوییچ بین سال‌های مالی — آمار هر سال جدا محاسبه می‌شود
                    YearSwitcher()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (crashLog != null) {
                item {
                    CrashLogCard(
                        onClick = { showCrashLog = true }
                    )
                }
            }
            items(HOME_MODULES, key = { it.route }) { module ->
                HomeModuleCard(
                    module = module,
                    onClick = { navController.navigate(module.route) }
                )
            }
        }
    }

    if (showCrashLog && crashLog != null) {
        AlertDialog(
            onDismissRequest = { showCrashLog = false },
            title = { Text("گزارش خطا", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = crashLog,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 14,
                    overflow = TextOverflow.Ellipsis
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(crashLog))
                }) {
                    Text("کپی متن خطا")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    CrashLogger.clear()
                    showCrashLog = false
                }) {
                    Text("پاک کردن")
                }
            }
        )
    }
}

@Composable
private fun CrashLogCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "اپ در اجرای قبلی خطای ناگهانی داشته",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "برای ارسال گزارش خطا لمس کنید",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun HomeModuleCard(
    module: HomeModuleData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = module.icon(),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = module.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
