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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.ui.components.YearSwitcher
import ir.furnitureshop.app.util.FinancialOverview
import ir.furnitureshop.app.util.PriceFormatter
import ir.furnitureshop.app.viewmodel.FinancialOverviewViewModel

/**
 * «حساب مالی» — وضعیت مالی کلی کسب‌وکار از داده‌های موجود (بدون سیستم حسابداری جدید).
 *
 * - تراز مشتریان و تأمین‌کنندگان با همان منطق قبلی (BalanceCalculator / SupplierBalanceCalculator)
 * - چک به تنهایی مبلغ نمی‌سازد؛ Payment روی Balance اثر دارد
 * - Expense مستقل از Supplier است
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialOverviewScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: FinancialOverviewViewModel = viewModel(
        factory = FinancialOverviewViewModel.factory(
            invoiceRepository = app.invoiceRepository,
            paymentRepository = app.paymentRepository,
            supplierRepository = app.supplierRepository,
            expenseRepository = app.expenseRepository,
            legacyDebtRepository = app.legacyDebtRepository
        )
    )
    val overview by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("حساب مالی") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    // آمار این صفحه فقط مربوط به سال انتخابی است
                    YearSwitcher()
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
            // ---- نتیجه کلی ----
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نتیجه (فروش − خرید جنسی − هزینه‌ها)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = PriceFormatter.format(overview.netResult) + " تومان",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            overview.netResult > 0 -> MaterialTheme.colorScheme.primary
                            overview.netResult < 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- ترازها ----
            SectionTitle("ترازها")
            OverviewRow(
                icon = { Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.tertiary) },
                label = "تراز مشتریان (بدهی آن‌ها به ما)",
                value = overview.customersBalance,
                valueColor = if (overview.customersBalance > 0)
                    MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurface
            )
            OverviewRow(
                icon = { Icon(Icons.Filled.Face, null, tint = MaterialTheme.colorScheme.error) },
                label = "تراز تأمین‌کنندگان (بدهی ما به آن‌ها)",
                value = overview.suppliersBalance,
                valueColor = if (overview.suppliersBalance > 0)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            // ---- درآمد ----
            SectionTitle("درآمد")
            OverviewRow(
                icon = { Icon(Icons.Filled.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary) },
                label = "فروش (مجموع فاکتورها)",
                value = overview.totalSales
            )
            OverviewRow(
                icon = { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                label = "دریافتی از مشتریان (نقد و چک)",
                value = overview.totalReceived
            )

            Spacer(Modifier.height(16.dp))

            // ---- هزینه‌ها ----
            SectionTitle("هزینه‌ها")
            OverviewRow(
                icon = { Icon(Icons.Filled.ShoppingCart, null, tint = MaterialTheme.colorScheme.error) },
                label = "خرید جنسی از تأمین‌کنندگان",
                value = overview.purchasesGoods
            )
            OverviewRow(
                icon = { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.error) },
                label = "پرداختی به تأمین‌کنندگان (نقد و چک)",
                value = overview.totalPaidToSuppliers
            )
            OverviewRow(
                icon = { Icon(Icons.Filled.DateRange, null, tint = MaterialTheme.colorScheme.error) },
                label = "هزینه‌های جانبی",
                value = overview.totalExpenses
            )

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "• تراز مشتریان = مجموع فاکتورها − مجموع پرداخت‌ها (نقد و چک).\n" +
                            "• تراز تأمین‌کنندگان = خرید جنسی − پرداخت‌ها (نقد و چک).\n" +
                            "• چک به تنهایی مبلغ مالی جدید ایجاد نمی‌کند؛ Payment است که روی Balance اثر دارد.\n" +
                            "• هزینه‌های جانبی مستقل از تأمین‌کننده‌ها هستند و تراز آن‌ها را تغییر نمی‌دهند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun OverviewRow(
    icon: @Composable () -> Unit,
    label: String,
    value: Long,
    valueColor: Color? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = PriceFormatter.format(value) + " تومان",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}
