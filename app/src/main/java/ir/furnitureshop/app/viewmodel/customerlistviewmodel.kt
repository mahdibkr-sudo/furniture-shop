package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.repository.CustomerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerListUiState(
    val query: String = "",
    val customers: List<Customer> = emptyList(),
    val customerToDelete: Customer? = null,
    val deleteError: String? = null,
    val loadError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerListViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _customerToDelete = MutableStateFlow<Customer?>(null)
    private val _deleteError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CustomerListUiState> = combine(
        _query,
        _query.flatMapLatest { repository.observeCustomers(it) },
        _customerToDelete,
        _deleteError
    ) { query, customers, toDelete, error ->
        CustomerListUiState(
            query = query,
            customers = customers,
            customerToDelete = toDelete,
            deleteError = error
        )
    }
        .catch { e ->
            emit(CustomerListUiState(loadError = e.message ?: "خطا در بارگذاری مشتریان"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomerListUiState())

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onDeleteClick(customer: Customer) {
        _deleteError.value = null
        _customerToDelete.value = customer
    }

    fun onDeleteConfirmed() {
        val customer = _customerToDelete.value ?: return
        _customerToDelete.value = null
        viewModelScope.launch {
            try {
                val deleted = repository.deleteIfNoInvoices(customer)
                if (!deleted) {
                    _deleteError.value = "این مشتری سابقه مالی (فاکتور، پرداخت یا بدهی قبلی) دارد و قابل حذف نیست"
                }
            } catch (e: Exception) {
                // حذف مشتریِ دارای پرداخت/چک می‌تواند constraint بدهد — به‌جای کرش، پیام
                _deleteError.value = "این مشتری سابقه مالی دارد و قابل حذف نیست"
            }
        }
    }

    fun onDeleteDismissed() {
        _customerToDelete.value = null
    }

    fun onDeleteErrorShown() {
        _deleteError.value = null
    }

    companion object {
        fun factory(repository: CustomerRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CustomerListViewModel(repository) }
        }
    }
}
