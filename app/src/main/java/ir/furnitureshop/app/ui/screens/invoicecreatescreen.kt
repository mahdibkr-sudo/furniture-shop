package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.navigation.Routes
import ir.furnitureshop.app.util.InvoiceCartItem
import ir.furnitureshop.app.util.PriceFormatter
import ir.furnitureshop.app.viewmodel.InvoiceCreateViewModel

/**
 * صدور فاکتور:
 * انتخاب مشتری ← افزودن محصولات (قیمت لحظه‌ای) ← تعداد/قیمت ← تخفیف ← پارچه ← صدور.
 * Payment در این صفحه ثبت نمی‌شود.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreateScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: InvoiceCreateViewModel = viewModel(
        factory = InvoiceCreateViewModel.factory(
            invoiceRepository = app.invoiceRepository,
            productRepository = app.productRepository,
            customerRepository = app.customerRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedInvoiceId) {
        uiState.savedInvoiceId?.let { id ->
            navController.navigate(Routes.invoiceDetail(id)) {
                popUpTo(Routes.INVOICES) { inclusive = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("صدور فاکتور") },
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
            // ---- مشتری ----
            Text("مشتری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (uiState.customers.isEmpty()) {
                Text(
                    text = "هنوز مشتری‌ای ثبت نشده است. ابتدا از بخش «مشتریان» مشتری اضافه کنید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                var customerExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedCustomer?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("انتخاب مشتری") },
                        placeholder = { Text("انتخاب کنید…") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = customerExpanded,
                        onDismissRequest = { customerExpanded = false }
                    ) {
                        uiState.customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text(customer.name) },
                                onClick = {
                                    viewModel.onCustomerSelected(customer.id)
                                    customerExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- افزودن محصول ----
            Text("محصولات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (uiState.products.isEmpty()) {
                Text(
                    text = "هنوز محصولی ثبت نشده است. ابتدا از بخش «محصولات» محصول اضافه کنید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                var productExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("افزودن محصول") },
                        placeholder = { Text("انتخاب محصول…") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        uiState.products.forEach { product ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${product.name} — ${PriceFormatter.format(product.basePrice)} تومان"
                                    )
                                },
                                onClick = {
                                    viewModel.onProductSelected(product)
                                    productExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- اقلام فاکتور ----
            if (uiState.items.isEmpty()) {
                Text(
                    text = "هیچ محصولی به فاکتور اضافه نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.items.forEach { item ->
                    InvoiceItemRow(item = item, viewModel = viewModel)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ---- تخفیف ----
            Text("تخفیف", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.discountText,
                onValueChange = viewModel::onDiscountChange,
                label = { Text("تخفیف (تومان)") },
                placeholder = { Text("۰") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // ---- پارچه ----
            Text("پارچه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onFabricModeChange(true) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = uiState.fabricCustomerProvided,
                    onClick = { viewModel.onFabricModeChange(true) }
                )
                Spacer(Modifier.width(8.dp))
                Text("پارچه توسط مشتری تأمین شده (مبلغ اضافه نمی‌شود)")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onFabricModeChange(false) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !uiState.fabricCustomerProvided,
                    onClick = { viewModel.onFabricModeChange(false) }
                )
                Spacer(Modifier.width(8.dp))
                Text("پارچه توسط فروشگاه تهیه می‌شود (به فاکتور اضافه می‌شود)")
            }
            OutlinedTextField(
                value = uiState.fabricNameText,
                onValueChange = viewModel::onFabricNameChange,
                label = { Text("نام پارچه") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                singleLine = true
            )
            if (!uiState.fabricCustomerProvided) {
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.fabricMetersText,
                        onValueChange = viewModel::onFabricMetersChange,
                        label = { Text("متراژ پارچه (متر)") },
                        placeholder = { Text("۳٫۵") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = uiState.fabricUnitPriceText,
                        onValueChange = viewModel::onFabricUnitPriceChange,
                        label = { Text("قیمت هر متر (تومان)") },
                        placeholder = { Text("۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }
                if (uiState.fabricAmount > 0) {
                    Text(
                        text = "مبلغ پارچه: ${PriceFormatter.format(uiState.fabricAmount)} تومان",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ---- جمع کل ----
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TotalRow("جمع اقلام", PriceFormatter.format(uiState.subtotal))
                    if (uiState.discount > 0) {
                        TotalRow("تخفیف", "− " + PriceFormatter.format(uiState.discount))
                    }
                    if (uiState.fabricAmount > 0) {
                        TotalRow("پارچه", PriceFormatter.format(uiState.fabricAmount))
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("جمع کل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = PriceFormatter.format(uiState.total) + " تومان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::save,
                enabled = uiState.savedInvoiceId == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("صدور فاکتور", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InvoiceItemRow(
    item: InvoiceCartItem,
    viewModel: InvoiceCreateViewModel
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.onRemoveItem(item) }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "حذف از فاکتور",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = if (item.unitPrice > 0) item.unitPrice.toString() else "",
                    onValueChange = { viewModel.onUnitPriceChange(item, it) },
                    label = { Text("قیمت واحد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = { viewModel.onQuantityChange(item, -1) }) {
                    Text("−", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = PriceFormatter.format(item.quantity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.onQuantityChange(item, +1) }) {
                    Icon(Icons.Filled.Add, contentDescription = "افزایش تعداد")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = PriceFormatter.format(item.unitPrice * item.quantity),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
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
