package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.navigation.Routes
import ir.furnitureshop.app.ui.components.CopyButton
import ir.furnitureshop.app.util.JalaliDate
import ir.furnitureshop.app.util.PriceFormatter
import ir.furnitureshop.app.viewmodel.CustomerProfileViewModel

/**
 * پروفایل مشتری: اطلاعات، سوابق خرید، فاکتورها و بدهی/بستانکاری.
 * پرداخت‌ها و چک‌های دریافتی فعلاً به‌صورت ساختار آماده نمایش داده می‌شوند
 * و در تسک‌های بعدی منطق کامل می‌گیرند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(navController: NavHostController, customerId: Long) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: CustomerProfileViewModel = viewModel(
        factory = CustomerProfileViewModel.factory(
            repository = app.customerRepository,
            checkRepository = app.checkRepository,
            legacyDebtRepository = app.legacyDebtRepository,
            customerId = customerId
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val customer = uiState.customer

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پروفایل مشتری") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.legacyDebtCreate(customerId)) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Filled.History, contentDescription = "ثبت بدهی قبلی")
                }
                FloatingActionButton(onClick = {
                    navController.navigate(Routes.paymentCreate(customerId))
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "ثبت پرداختی")
                }
            }
        }
    ) { innerPadding ->
        if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "خطا در بارگذاری پروفایل",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = uiState.error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (customer == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "مشتری یافت نشد",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---- اطلاعات مشتری ----
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = customer.phone ?: "بدون تلفن",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                navController.navigate(Routes.customerEdit(customer.id))
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
                            }
                        }
                    }
                }

                // ---- بدهی / بستانکاری ----
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = when {
                                uiState.balance > 0 -> MaterialTheme.colorScheme.errorContainer
                                uiState.balance < 0 -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when {
                                    uiState.balance > 0 -> "بدهی"
                                    uiState.balance < 0 -> "بستانکار"
                                    else -> "تسویه"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = PriceFormatter.format(kotlin.math.abs(uiState.balance)) + " تومان",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ---- سوابق خرید (خلاصه) ----
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryBox(
                            modifier = Modifier.weight(1f),
                            label = "تعداد فاکتور",
                            value = PriceFormatter.format(uiState.invoices.size)
                        )
                        SummaryBox(
                            modifier = Modifier.weight(1f),
                            label = "جمع خرید",
                            value = PriceFormatter.format(uiState.totalPurchases)
                        )
                    }
                }

                // ---- بدهی‌های قبلی (سیستم مالی پیشین) ----
                if (uiState.legacyDebts.isNotEmpty()) {
                    item {
                        Text(
                            text = "بدهی‌های قبلی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(uiState.legacyDebts, key = { "legacyDebt_${it.id}" }) { debt ->
                        LegacyDebtRow(debt)
                    }
                }

                // ---- فاکتورها ----
                item {
                    Text(
                        text = "فاکتورها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (uiState.invoices.isEmpty()) {
                    item {
                        Text(
                            text = "فاکتوری ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.invoices, key = { "invoice_${it.id}" }) { invoice ->
                        InvoiceRow(invoice)
                    }
                }

                // ---- پرداخت‌ها (نقد و چک) ----
                item {
                    Text(
                        text = "پرداخت‌ها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (uiState.payments.isEmpty()) {
                    item {
                        Text(
                            text = "هنوز پرداختی ثبت نشده است — با دکمه «پرداختی» ثبت کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.payments, key = { "payment_${it.id}" }) { payment ->
                        PaymentRow(payment)
                    }
                }

                // ---- چک‌های دریافتی ----
                item {
                    Text(
                        text = "چک‌های دریافتی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (uiState.checks.isEmpty()) {
                    item {
                        Text(
                            text = "چکی از این مشتری دریافت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.checks, key = { "check_${it.id}" }) { check ->
                        CustomerCheckRow(check)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegacyDebtRow(debt: ir.furnitureshop.app.data.entity.LegacyDebt) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = debt.description ?: "بدهی قبلی (سیستم قبل)",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = JalaliDate.formatDate(debt.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = PriceFormatter.format(debt.amount) + " تومان",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryBox(modifier: Modifier, label: String, value: String) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaymentRow(payment: Payment) {
    val method = PaymentMethod.fromStored(payment.method)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (method == PaymentMethod.CHECK)
                    Icons.Filled.DateRange
                else
                    Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method?.label ?: payment.method,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = JalaliDate.formatDate(payment.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = PriceFormatter.format(payment.amount) + " تومان",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CustomerCheckRow(check: Check) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "چک ${check.checkNumber}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    CopyButton(
                        text = check.checkNumber,
                        contentDescription = "کپی شماره چک"
                    )
                }
                Text(
                    text = "سررسید: " + (check.dueDate?.let {
                        JalaliDate.formatDate(it)
                    } ?: "—"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PriceFormatter.format(check.amount) + " تومان",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CheckStatus.fromStored(check.status)?.label ?: check.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (check.status == CheckStatus.TRANSFERRED.stored)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InvoiceRow(invoice: Invoice) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${invoice.id}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(56.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = JalaliDate.formatDate(invoice.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = PriceFormatter.format(invoice.totalAmount) + " تومان",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
