package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.dao.InvoiceWithCustomer
import ir.furnitureshop.app.data.repository.InvoiceRepository
import ir.furnitureshop.app.util.YearFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class InvoiceListUiState(
    val loadError: String? = null,
    val invoices: List<InvoiceWithCustomer> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class InvoiceListViewModel(
    private val repository: InvoiceRepository
) : ViewModel() {

    val uiState: StateFlow<InvoiceListUiState> =
        YearFilter.selectedYear.flatMapLatest { year ->
            repository.observeAllWithCustomer()
                .map { invoices ->
                    InvoiceListUiState(invoices = invoices.filter { YearFilter.isInYear(it.invoice.date, year) })
                }
        }
            .catch { e -> emit(InvoiceListUiState(loadError = e.message ?: "خطا در بارگذاری فاکتورها")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceListUiState())

    companion object {
        fun factory(repository: InvoiceRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { InvoiceListViewModel(repository) }
        }
    }
}
