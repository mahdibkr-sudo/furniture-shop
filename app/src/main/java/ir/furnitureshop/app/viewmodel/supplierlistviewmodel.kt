package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Supplier
import ir.furnitureshop.app.data.repository.SupplierRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SupplierListUiState(
    val loadError: String? = null,
    val query: String = "",
    val suppliers: List<Supplier> = emptyList(),
    val supplierToDelete: Supplier? = null,
    val deleteError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SupplierListViewModel(
    private val repository: SupplierRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _supplierToDelete = MutableStateFlow<Supplier?>(null)
    private val _deleteError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SupplierListUiState> = combine(
        _query,
        _query.flatMapLatest { repository.observeSuppliers(it) },
        _supplierToDelete,
        _deleteError
    ) { query, suppliers, toDelete, error ->
        SupplierListUiState(
            query = query,
            suppliers = suppliers,
            supplierToDelete = toDelete,
            deleteError = error
        )
    }.catch { e -> emit(SupplierListUiState(loadError = e.message ?: "خطا در بارگذاری تأمین‌کنندگان")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SupplierListUiState())

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onDeleteClick(supplier: Supplier) {
        _deleteError.value = null
        _supplierToDelete.value = supplier
    }

    fun onDeleteConfirmed() {
        val supplier = _supplierToDelete.value ?: return
        _supplierToDelete.value = null
        viewModelScope.launch {
            val deleted = repository.deleteIfNoPurchases(supplier)
            if (!deleted) {
                _deleteError.value = "این تأمین‌کننده سابقه خرید دارد و قابل حذف نیست"
            }
        }
    }

    fun onDeleteDismissed() {
        _supplierToDelete.value = null
    }

    fun onDeleteErrorShown() {
        _deleteError.value = null
    }

    companion object {
        fun factory(repository: SupplierRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { SupplierListViewModel(repository) }
        }
    }
}
