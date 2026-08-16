package ir.furnitureshop.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ir.furnitureshop.app.FurnitureApplication
import ir.furnitureshop.app.data.dao.CheckWithCustomer
import ir.furnitureshop.app.data.entity.CheckStatus
import ir.furnitureshop.app.navigation.Routes
import ir.furnitureshop.app.util.JalaliDate
import ir.furnitureshop.app.util.PriceFormatter
import ir.furnitureshop.app.viewmodel.CheckListViewModel

/**
 * لیست چک‌ها: شماره، مبلغ، بانک، تاریخ دریافت، سررسید، دریافت از، صاحب فعلی، وضعیت.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecksScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FurnitureApplication
    val viewModel: CheckListViewModel = viewModel(
        factory = CheckListViewModel.factory(app.checkRepository, app.supplierRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("چک‌ها") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.checks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "هنوز چکی ثبت نشده است",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "چک از بخش «ثبت پرداختی» با روش چک دریافت می‌شود.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.checks, key = { it.check.id }) { item ->
                    CheckCard(item, uiState.holderLabel(item.check.currentHolder)) {
                        navController.navigate(Routes.checkDetail(item.check.id))
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckCard(item: CheckWithCustomer, holderLabel: String, onClick: () -> Unit) {
    val check = item.check
    val status = CheckStatus.fromStored(check.status)
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "چک ${check.checkNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = status?.label ?: check.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (status == CheckStatus.TRANSFERRED)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = PriceFormatter.format(check.amount) + " تومان",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = buildString {
                    append("بانک: ").append(check.bank ?: "—")
                    append("  |  سررسید: ").append(
                        check.dueDate?.let {
                            JalaliDate.formatDate(it)
                        } ?: "—"
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = buildString {
                    append("دریافت از: ").append(item.customerName ?: "—")
                    append("  |  صاحب فعلی: ").append(holderLabel)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
