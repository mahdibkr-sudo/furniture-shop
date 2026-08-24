package ir.furnitureshop.app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.entity.SupplierPayment
import ir.furnitureshop.app.navigation.Routes
import ir.furnitureshop.app.ui.components.rememberReceiptImage
import ir.furnitureshop.app.util.JalaliDate
import ir.furnitureshop.app.util.PriceFormatter
import ir.furnitureshop.app.viewmodel.SupplierProfileViewModel

/**
 * پروفایل تأمین‌کننده: اطلاعات، سوابق مالی، خریدها (جنسی)، پرداخت‌ها و تراز.
 * چک‌های پرداختی در Task بعدی متصل می‌شوند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierProfileScreen(navController: NavHostController, supplierId: Long) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: SupplierProfileViewModel = viewModel(
        factory = SupplierProfileViewModel.factory(
            repository = app.supplierRepository,
            checkRepository = app.checkRepository,
            supplierId = supplierId
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val supplier = uiState.supplier
    var selectedReceiptPath by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پروفایل تأمین‌کننده") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (supplier == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "تأمین‌کننده یافت نشد",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---- اطلاعات ----
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Face,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = supplier.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = supplier.phone ?: "بدون تلفن",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                navController.navigate(Routes.supplierEdit(supplier.id))
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
                            }
                        }
                    }
                }

                // ---- تراز ----
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
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
                                    uiState.balance > 0 -> "بدهی ما به تأمین‌کننده"
                                    uiState.balance < 0 -> "بستانکاری ما"
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

                // ---- عملیات: خرید و پرداخت ----
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate(Routes.supplierPurchaseCreate(supplierId)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("ثبت خرید")
                            }
                            OutlinedButton(
                                onClick = { navController.navigate(Routes.supplierPaymentCreate(supplierId)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("ثبت پرداختی (نقد / چک)")
                            }
                            OutlinedButton(
                                onClick = { navController.navigate(Routes.checkTransfer(supplierId = supplierId)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.DateRange, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("پرداخت با چک موجود")
                            }
                        }
                    }
                }

                // ---- مبلغ خرید جنسی ----
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "مبلغ خرید جنسی",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = PriceFormatter.format(uiState.purchasesGoods) + " تومان",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${PriceFormatter.format(uiState.purchases.size)} خرید · جمع پرداخت‌ها: ${PriceFormatter.format(uiState.totalPaid)} تومان",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // ---- خریدها ----
                item {
                    Text(
                        text = "خریدها (جنسی)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (uiState.purchases.isEmpty()) {
                    item {
                        Text(
                            text = "خریدی ثبت نشده است — با دکمه «ثبت خرید» مبلغ کالای گرفته‌شده را ثبت کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.purchases, key = { "purchase_${it.id}" }) { purchase ->
                        PurchaseRow(purchase)
                    }
                }

                // ---- پرداخت‌ها ----
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
                        SupplierPaymentRow(payment, onReceiptClick = { selectedReceiptPath = it })
                    }
                }

                // ---- چک‌های پرداختی (منتقل‌شده به این تأمین‌کننده) ----
                item {
                    Text(
                        text = "چک‌های پرداختی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (uiState.checks.isEmpty()) {
                    item {
                        Text(
                            text = "چکی به این تأمین‌کننده منتقل نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.checks, key = { "check_${it.id}" }) { check ->
                        SupplierCheckRow(check)
                    }
                }
            }
        }
    }

    // ---- نمایش تمام صفحه رسید ----
    if (selectedReceiptPath != null) {
        Dialog(onDismissRequest = { selectedReceiptPath = null }) {
            Box(modifier = Modifier.fillMaxSize()) {
                val bitmap = rememberReceiptImage(selectedReceiptPath)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "رسید",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "تصویر یافت نشد",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                IconButton(
                    onClick = { selectedReceiptPath = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بستن",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SupplierCheckRow(check: Check) {
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
                Text(
                    text = "چک ${check.checkNumber}",
                    style = MaterialTheme.typography.titleSmall
                )
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
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun PurchaseRow(purchase: Purchase) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = purchase.note ?: "خرید جنسی",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = JalaliDate.formatDate(purchase.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = PriceFormatter.format(purchase.totalAmount) + " تومان",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SupplierPaymentRow(
    payment: SupplierPayment,
    onReceiptClick: (String) -> Unit
) {
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
            if (method == PaymentMethod.CASH && payment.receiptImagePath != null) {
                ReceiptThumbnail(
                    path = payment.receiptImagePath,
                    modifier = Modifier.size(40.dp),
                    onClick = { onReceiptClick(payment.receiptImagePath!!) }
                )
            }
        }
    }
}

@Composable
private fun ReceiptThumbnail(
    path: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bitmap = rememberReceiptImage(path)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { bitmap?.let { onClick() } },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "رسید",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = "رسید یافت نشد",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
