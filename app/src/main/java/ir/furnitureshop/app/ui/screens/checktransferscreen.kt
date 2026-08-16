package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.util.PriceFormatter
import ir.furnitureshop.app.viewmodel.CheckTransferViewModel

/**
 * انتقال چک به تأمین‌کننده.
 *
 * دو حالت ورود:
 * - از پروفایل تأمین‌کننده: supplierId ثابت، چک از بین چک‌های در دست انتخاب می‌شود
 * - از جزئیات چک: checkId ثابت، تأمین‌کننده انتخاب می‌شود
 *
 * همان چک منتقل می‌شود (جدید ساخته نمی‌شود)؛ انتقال + Payment به‌صورت ATOMIC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckTransferScreen(
    navController: NavHostController,
    checkId: Long,
    supplierId: Long?
) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: CheckTransferViewModel = viewModel(
        factory = CheckTransferViewModel.factory(
            checkRepository = app.checkRepository,
            supplierRepository = app.supplierRepository,
            checkId = checkId,
            supplierId = supplierId
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isTransferred) {
        if (uiState.isTransferred) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("انتقال چک") },
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
            // ---- انتخاب چک (وقتی از پروفایل تأمین‌کننده آمدیم) ----
            if (uiState.check == null) {
                Text("انتخاب چک", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (uiState.availableChecks.isEmpty()) {
                    Text(
                        text = "چک قابل انتقالی در دست فروشگاه وجود ندارد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    var checkExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = checkExpanded,
                        onExpandedChange = { checkExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedCheck?.let {
                                "چک ${it.checkNumber} — ${PriceFormatter.format(it.amount)} تومان"
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("چک در دست") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = checkExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = checkExpanded,
                            onDismissRequest = { checkExpanded = false }
                        ) {
                            uiState.availableChecks.forEach { check ->
                                DropdownMenuItem(
                                    text = {
                                        Text("چک ${check.checkNumber} — ${PriceFormatter.format(check.amount)} تومان")
                                    },
                                    onClick = {
                                        viewModel.onCheckSelected(check.id)
                                        checkExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                // ---- از جزئیات چک آمدیم: نمایش چک ثابت ----
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "چک ${uiState.check!!.checkNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = PriceFormatter.format(uiState.check!!.amount) + " تومان — " +
                                (uiState.check!!.bank ?: "بدون بانک"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ---- انتخاب تأمین‌کننده ----
            Text("تأمین‌کننده مقصد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            var supplierExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = supplierExpanded,
                onExpandedChange = { supplierExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedSupplier?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("انتخاب تأمین‌کننده") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = supplierExpanded,
                    onDismissRequest = { supplierExpanded = false }
                ) {
                    uiState.suppliers.forEach { supplier ->
                        DropdownMenuItem(
                            text = { Text(supplier.name) },
                            onClick = {
                                viewModel.onSupplierSelected(supplier.id)
                                supplierExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "• همان چک منتقل می‌شود؛ چک جدید ساخته نمی‌شود.\n" +
                            "• صاحب فعلی به تأمین‌کننده تغییر می‌کند و وضعیت «انتقال‌یافته» می‌شود.\n" +
                            "• Payment تأمین‌کننده هم‌زمان و به‌صورت اتمیک ثبت می‌شود.\n" +
                            "• مبلغ این چک در تراز مشتری و تأمین‌کننده دوبار محاسبه نمی‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = viewModel::transfer,
                enabled = uiState.selectedCheck != null && uiState.selectedSupplierId != null && !uiState.isTransferred,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("انتقال چک و ثبت پرداخت", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
