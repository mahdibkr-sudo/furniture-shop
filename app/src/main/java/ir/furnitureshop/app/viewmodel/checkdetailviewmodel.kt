package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Supplier
import ir.furnitureshop.app.data.repository.CheckRepository
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckDetailUiState(
    val loadError: String? = null,
    val check: Check? = null,
    val receivedFromName: String? = null,
    val currentHolderName: String? = null,
    val loaded: Boolean = false
)

/**
 * جزئیات چک + امکان انتقال (فقط وقتی در دست فروشگاه باشد).
 */
class CheckDetailViewModel(
    checkRepository: CheckRepository,
    customerRepository: CustomerRepository,
    supplierRepository: SupplierRepository,
    private val checkId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckDetailUiState())
    val uiState: StateFlow<CheckDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val check = checkRepository.getById(checkId)
                if (check == null) {
                    _uiState.update { it.copy(loaded = true) }
                    return@launch
                }
                val customer = check.receivedFromCustomerId?.let { customerRepository.getById(it) }
                val holderSupplierId = CheckFlow.supplierIdOf(check.currentHolder)
                val supplier = holderSupplierId?.let { supplierRepository.getById(it) }
                _uiState.update {
                    it.copy(
                        check = check,
                        receivedFromName = customer?.name,
                        currentHolderName = supplier?.name,
                        loaded = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loaded = true, loadError = e.message ?: "خطا در بارگذاری چک") }
            }
        }
    }

    companion object {
        fun factory(
            checkRepository: CheckRepository,
            customerRepository: CustomerRepository,
            supplierRepository: SupplierRepository,
            checkId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CheckDetailViewModel(
                    checkRepository = checkRepository,
                    customerRepository = customerRepository,
                    supplierRepository = supplierRepository,
                    checkId = checkId
                )
            }
        }
    }
}
