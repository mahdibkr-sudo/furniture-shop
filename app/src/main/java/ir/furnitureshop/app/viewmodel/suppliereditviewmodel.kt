package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Supplier
import ir.furnitureshop.app.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupplierEditUiState(
    val supplierId: Long = 0,
    val name: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val saveError: String? = null,
    val notFound: Boolean = false,
    val isSaved: Boolean = false
)

class SupplierEditViewModel(
    private val repository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierEditUiState())
    val uiState: StateFlow<SupplierEditUiState> = _uiState.asStateFlow()

    fun loadSupplier(supplierId: Long) {
        if (supplierId <= 0) return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val supplier = repository.getById(supplierId)
                if (supplier == null) {
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            supplierId = supplier.id,
                            name = supplier.name,
                            phone = supplier.phone.orEmpty(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, saveError = e.message ?: "خطا در بارگذاری تأمین‌کننده") }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun save() {
        if (_uiState.value.isSaved) return
        val state = _uiState.value
        val name = state.name.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(nameError = "نام تأمین‌کننده را وارد کنید") }
            return
        }
        val phone = state.phone.trim().ifEmpty { null }

        viewModelScope.launch {
            try {
                val supplier = Supplier(
                    id = state.supplierId,
                    name = name,
                    phone = phone
                )
                if (state.supplierId > 0) {
                    repository.update(supplier)
                } else {
                    repository.insert(supplier)
                }
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message ?: "خطا در ذخیره تأمین‌کننده") }
            }
        }
    }

    companion object {
        fun factory(repository: SupplierRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { SupplierEditViewModel(repository) }
        }
    }
}
