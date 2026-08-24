package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.data.entity.InvoiceItem
import ir.furnitureshop.app.util.InvoiceImageExporter
import ir.furnitureshop.app.util.InvoicePdfExporter
import ir.furnitureshop.app.util.InvoicePrintFormatter
import ir.furnitureshop.app.util.JalaliDate
import ir.furnitureshop.app.util.PriceFormatter
import ir.furnitureshop.app.util.shareInvoiceFile
import ir.furnitureshop.app.viewmodel.InvoiceDetailViewModel
import kotlinx.coroutines.launch

/**
 * نمایش فاکتور صادرشده (فقط خواندنی) + خروجی PDF / Image.
 *
 * - همه مبالغ از snapshot خود فاکتور خوانده می‌شوند — تغییرات بعدی
 *   قیمت محصولات هیچ اثری روی این فاکتور ندارد (Historical Price).
 * - Payment و وضعیت تسویه در خروجی نمایش داده نمی‌شوند.
 * - پارچه: اگر مشتری آورده باشد مبلغی اضافه نمی‌شود؛ اگر فروشگاه تهیه
 *   کرده باشد مبلغ در همان فاکتور لحاظ شده و در خروجی نمایش داده می‌شود.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(navController: NavHostController, invoiceId: Long) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: InvoiceDetailViewModel = viewModel(
        factory = InvoiceDetailViewModel.factory(
            invoiceRepository = app.invoiceRepository,
            customerRepository = app.customerRepository,
            invoiceId = invoiceId
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فاکتور") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            !uiState.loaded -> {
                CenteredText("در حال بارگذاری…", innerPadding)
            }
            uiState.invoice == null -> {
                CenteredText("فاکتور یافت نشد", innerPadding)
            }
            else -> {
                val invoice = uiState.invoice!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = invoice.invoiceNumber,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = JalaliDate.formatDateTime(invoice.date),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "مشتری: ${uiState.customerName ?: "—"}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                uiState.customerPhone?.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        text = "شماره تماس: $it",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("اقلام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.items, key = { it.id }) { item ->
                        InvoiceItemRow(item)
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val subtotal = uiState.items.sumOf { it.unitPrice * it.quantity }
                                DetailLine("جمع اقلام", PriceFormatter.format(subtotal))
                                if (invoice.discount > 0) {
                                    DetailLine("تخفیف", "− " + PriceFormatter.format(invoice.discount))
                                }
                                DetailLine(
                                    invoice.fabricName?.takeIf { it.isNotBlank() }?.let { "پارچه ($it)" } ?: "پارچه",
                                    if (invoice.fabricCustomerProvided)
                                        "تأمین توسط مشتری"
                                    else if (invoice.fabricAmount > 0)
                                        if (invoice.fabricMeters > 0 && invoice.fabricUnitPrice > 0)
                                            "${PriceFormatter.formatMeters(invoice.fabricMeters)} متر × ${PriceFormatter.format(invoice.fabricUnitPrice)} = ${PriceFormatter.format(invoice.fabricAmount)}"
                                        else
                                            PriceFormatter.format(invoice.fabricAmount)
                                    else "—"
                                )
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "جمع کل",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = PriceFormatter.format(invoice.totalAmount) + " تومان",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // ---- خروجی PDF / Image ----
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val data = InvoicePrintFormatter.build(
                                        invoice = invoice,
                                        items = uiState.items,
                                        customer = uiState.customer
                                    )
                                    scope.launch {
                                        try {
                                            val file = InvoicePdfExporter.export(context, data)
                                            shareInvoiceFile(context, file)
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("خطا در ساخت PDF: ${e.message}")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("خروجی PDF")
                            }
                            OutlinedButton(
                                onClick = {
                                    val data = InvoicePrintFormatter.build(
                                        invoice = invoice,
                                        items = uiState.items,
                                        customer = uiState.customer
                                    )
                                    scope.launch {
                                        try {
                                            val file = InvoiceImageExporter.export(context, data)
                                            shareInvoiceFile(context, file)
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("خطا در ساخت تصویر: ${e.message}")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("خروجی Image")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceItemRow(item: InvoiceItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName.ifEmpty { "محصول #${item.productId}" },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${PriceFormatter.format(item.unitPrice)} تومان × ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = PriceFormatter.format(item.unitPrice * item.quantity),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
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
private fun CenteredText(text: String, innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}
