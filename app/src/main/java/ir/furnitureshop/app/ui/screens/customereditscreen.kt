package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.viewmodel.CustomerEditViewModel

/**
 * افزودن / ویرایش مشتری: نام (الزامی) و تلفن (اختیاری).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditScreen(navController: NavHostController, customerId: Long) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: CustomerEditViewModel = viewModel(
        factory = CustomerEditViewModel.factory(app.customerRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(customerId) { viewModel.loadCustomer(customerId) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (customerId > 0) "ویرایش مشتری" else "افزودن مشتری") },
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
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("نام مشتری") },
                placeholder = { Text("مثلاً: علی محمدی") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { error -> { Text(error) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("تلفن (اختیاری)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaved && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (customerId > 0) "ذخیره تغییرات" else "افزودن مشتری",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            uiState.saveError?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
