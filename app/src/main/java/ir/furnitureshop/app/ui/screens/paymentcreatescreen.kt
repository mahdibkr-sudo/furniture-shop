package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.ui.components.JalaliDatePickerDialog
import ir.furnitureshop.app.util.JalaliDate
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.viewmodel.PaymentCreateViewModel

/**
 * ثبت پرداختی از پروفایل مشتری: مبلغ + روش دریافت (نقد / چک).
 *
 * - نقد: فقط Payment ثبت می‌شود
 * - چک: Payment + Check به‌صورت ATOMIC ثبت می‌شوند (شماره چک یکتا، Duplicate ممنوع)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCreateScreen(navController: NavHostController, customerId: Long) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: PaymentCreateViewModel = viewModel(
        factory = PaymentCreateViewModel.factory(
            paymentRepository = app.paymentRepository,
            checkRepository = app.checkRepository,
            customerRepository = app.customerRepository,
            customerId = customerId
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navController.popBackStack()
    }

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت پرداختی") },
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
            uiState.customerName?.let {
                Text(
                    text = "مشتری: $it",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
            }

            // ---- مبلغ ----
            OutlinedTextField(
                value = uiState.amountText,
                onValueChange = viewModel::onAmountChange,
                label = { Text("مبلغ (تومان)") },
                placeholder = { Text("۰") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // ---- روش دریافت ----
            Text("روش دریافت", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            PaymentMethod.entries.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onMethodChange(method) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.method == method,
                        onClick = { viewModel.onMethodChange(method) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(method.label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // ---- فیلدهای چک ----
            if (uiState.method == PaymentMethod.CHECK) {
                Spacer(Modifier.height(8.dp))
                Text("اطلاعات چک", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.checkNumber,
                    onValueChange = viewModel::onCheckNumberChange,
                    label = { Text("شماره چک *") },
                    placeholder = { Text("مثلاً ۱۲۳۴۵۶") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.bank,
                    onValueChange = viewModel::onBankChange,
                    label = { Text("بانک (اختیاری)") },
                    placeholder = { Text("مثلاً: ملت") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.dueDate != null)
                            "سررسید: " + JalaliDate.formatDate(uiState.dueDate!!)
                        else "انتخاب سررسید (اختیاری)"
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "با ثبت، Payment و Check با هم ذخیره می‌شوند. شماره چک نباید تکراری باشد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                enabled = !uiState.isSaved,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("ثبت پرداختی", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialDateMillis = uiState.dueDate,
            onConfirm = { millis ->
                viewModel.onDueDateChange(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
