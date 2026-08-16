package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerEditUiState(
    val customerId: Long = 0,
    val name: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val saveError: String? = null,
    val notFound: Boolean = false,
    val isSaved: Boolean = false
)

class CustomerEditViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerEditUiState())
    val uiState: StateFlow<CustomerEditUiState> = _uiState.asStateFlow()

    fun loadCustomer(customerId: Long) {
        if (customerId <= 0) return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val customer = repository.getById(customerId)
                if (customer == null) {
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            customerId = customer.id,
                            name = customer.name,
                            phone = customer.phone.orEmpty(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, saveError = e.message ?: "خطا در بارگذاری مشتری") }
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
            _uiState.update { it.copy(nameError = "نام مشتری را وارد کنید") }
            return
        }
        val phone = state.phone.trim().ifEmpty { null }

        viewModelScope.launch {
            try {
                val customer = Customer(
                    id = state.customerId,
                    name = name,
                    phone = phone
                )
                if (state.customerId > 0) {
                    repository.update(customer)
                } else {
                    repository.insert(customer)
                }
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message ?: "خطا در ذخیره مشتری") }
            }
        }
    }

    companion object {
        fun factory(repository: CustomerRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CustomerEditViewModel(repository) }
        }
    }
}
