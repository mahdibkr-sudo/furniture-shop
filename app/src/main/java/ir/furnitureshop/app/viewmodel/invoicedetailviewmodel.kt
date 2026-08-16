package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvoiceDetailUiState(
    val loadError: String? = null,
    val invoice: Invoice? = null,
    val customer: Customer? = null,
    val items: List<InvoiceItem> = emptyList(),
    val loaded: Boolean = false
) {
    val customerName: String? get() = customer?.name
    val customerPhone: String? get() = customer?.phone
}

/**
 * نمایش فاکتور (فقط خواندنی) — قیمت‌ها از snapshot خود فاکتور می‌آیند.
 */
class InvoiceDetailViewModel(
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository,
    invoiceId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val invoice = invoiceRepository.getById(invoiceId)
                if (invoice == null) {
                    _uiState.update { it.copy(loaded = true) }
                    return@launch
                }
                val customer = customerRepository.getById(invoice.customerId)
                val items = invoiceRepository.getItems(invoiceId)
                _uiState.update {
                    it.copy(
                        invoice = invoice,
                        customer = customer,
                        items = items,
                        loaded = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loaded = true, loadError = e.message ?: "خطا در بارگذاری فاکتور") }
            }
        }
    }

    companion object {
        fun factory(
            invoiceRepository: InvoiceRepository,
            customerRepository: CustomerRepository,
            invoiceId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                InvoiceDetailViewModel(
                    invoiceRepository = invoiceRepository,
                    customerRepository = customerRepository,
                    invoiceId = invoiceId
                )
            }
        }
    }
}
