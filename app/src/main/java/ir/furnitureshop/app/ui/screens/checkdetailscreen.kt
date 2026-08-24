package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.navigation.Routes
import ir.furnitureshop.app.ui.components.CopyButton
import ir.furnitureshop.app.util.JalaliDate
import ir.furnitureshop.app.util.PriceFormatter
import ir.furnitureshop.app.viewmodel.CheckDetailViewModel

/**
 * جزئیات چک + دکمه انتقال (فقط وقتی در دست فروشگاه است).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckDetailScreen(navController: NavHostController, checkId: Long) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: CheckDetailViewModel = viewModel(
        factory = CheckDetailViewModel.factory(
            checkRepository = app.checkRepository,
            customerRepository = app.customerRepository,
            supplierRepository = app.supplierRepository,
            checkId = checkId
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("جزئیات چک") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { innerPadding ->
        val check = uiState.check
        if (!uiState.loaded) {
            CenteredCheckText("در حال بارگذاری…", innerPadding)
        } else if (check == null) {
            CenteredCheckText("چک یافت نشد", innerPadding)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "چک ${check.checkNumber}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            CopyButton(
                                text = check.checkNumber,
                                contentDescription = "کپی شماره چک"
                            )
                            Text(
                                text = CheckStatus.fromStored(check.status)?.label ?: check.status,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (check.status == CheckStatus.TRANSFERRED.stored)
                                    MaterialTheme.colorScheme.tertiary
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        DetailCheckLine("مبلغ", PriceFormatter.format(check.amount) + " تومان")
                        DetailCheckLine("بانک", check.bank ?: "—")
                        DetailCheckLine(
                            "تاریخ دریافت",
                            JalaliDate.formatDate(check.receiveDate)
                        )
                        DetailCheckLine(
                            "سررسید",
                            check.dueDate?.let {
                                JalaliDate.formatDate(it)
                            } ?: "—"
                        )
                        DetailCheckLine("دریافت از", uiState.receivedFromName ?: "—")
                        DetailCheckLine(
                            "صاحب فعلی",
                            CheckFlow.holderLabel(check.currentHolder, uiState.currentHolderName)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "قوانین تراز",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "• چک به تنهایی Balance را تغییر نمی‌دهد؛ Payment باعث تغییر Balance است.\n" +
                                "• «خرج شده» و «انتقال یافته» یک مفهوم هستند.\n" +
                                "• هنگام انتقال به تأمین‌کننده، همان چک منتقل می‌شود (چک جدید ساخته نمی‌شود).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (CheckFlow.canTransfer(check)) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(Routes.checkTransfer(checkId = check.id)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text("انتقال به تأمین‌کننده", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailCheckLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CenteredCheckText(text: String, innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}
