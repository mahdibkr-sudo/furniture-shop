package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Purchase
import ir.furnitureshop.app.data.repository.SupplierRepository
import ir.furnitureshop.app.util.DigitNormalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupplierPurchaseCreateUiState(
    val supplierName: String? = null,
    val amountText: String = "",
    val note: String = "",
    val error: String? = null,
    val isSaved: Boolean = false
)

/**
 * ثبت خرید جنسی از تأمین‌کننده:
 * مبلغ کالا/مواد/خدمتی که از او گرفته‌ایم — در تراز مالی او لحاظ می‌شود.
 */
class SupplierPurchaseCreateViewModel(
    private val repository: SupplierRepository,
    private val supplierId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierPurchaseCreateUiState())
    val uiState: StateFlow<SupplierPurchaseCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val supplier = repository.getById(supplierId)
                _uiState.update { it.copy(supplierName = supplier?.name) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در بارگذاری تأمین‌کننده") }
            }
        }
    }

    fun onAmountChange(raw: String) {
        _uiState.update { it.copy(amountText = DigitNormalizer.digitsOnly(raw), error = null) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun save() {
        if (_uiState.value.isSaved) return
        val state = _uiState.value
        val amount = state.amountText.toLongOrNull() ?: 0L
        if (amount <= 0) {
            _uiState.update { it.copy(error = "مبلغ معتبری وارد کنید") }
            return
        }

        viewModelScope.launch {
            try {
                repository.insertPurchase(
                    Purchase(
                        supplierId = supplierId,
                        date = System.currentTimeMillis(),
                        totalAmount = amount,
                        note = state.note.trim().ifEmpty { null }
                    )
                )
                _uiState.update { it.copy(isSaved = true, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در ثبت خرید") }
            }
        }
    }

    companion object {
        fun factory(
            repository: SupplierRepository,
            supplierId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SupplierPurchaseCreateViewModel(repository, supplierId)
            }
        }
    }
}
