package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Check
import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.Supplier
import ir.furnitureshop.app.data.repository.CheckRepository
import ir.furnitureshop.app.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckTransferUiState(
    val loadError: String? = null,
    val check: Check? = null,
    val availableChecks: List<Check> = emptyList(),
    val selectedCheckId: Long? = null,
    val suppliers: List<Supplier> = emptyList(),
    val selectedSupplierId: Long? = null,
    val error: String? = null,
    val isTransferred: Boolean = false
) {
    val selectedCheck: Check?
        get() = if (check != null) check
        else availableChecks.find { it.id == selectedCheckId }

    val selectedSupplier: Supplier?
        get() = suppliers.find { it.id == selectedSupplierId }
}

/**
 * انتقال چک به تأمین‌کننده:
 * - از پروفایل تأمین‌کننده: supplierId ثابت است، چک از بین چک‌های در دست انتخاب می‌شود
 * - از جزئیات چک: checkId ثابت است، تأمین‌کننده انتخاب می‌شود
 * انتقال + Payment مربوطه ATOMIC هستند (همان چک منتقل می‌شود، جدید ساخته نمی‌شود).
 */
class CheckTransferViewModel(
    private val checkRepository: CheckRepository,
    supplierRepository: SupplierRepository,
    checkId: Long,
    private val fixedSupplierId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckTransferUiState())
    val uiState: StateFlow<CheckTransferUiState> = combine(
        _uiState,
        supplierRepository.observeSuppliers()
    ) { state, suppliers -> state.copy(suppliers = suppliers) }
        .catch { e -> emit(CheckTransferUiState(loadError = e.message ?: "خطا در بارگذاری انتقال چک")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckTransferUiState())

    init {
        viewModelScope.launch {
            try {
                if (checkId > 0) {
                    val check = checkRepository.getById(checkId)
                    _uiState.update { it.copy(check = check, selectedCheckId = check?.id) }
                } else {
                    checkRepository.observeInHand().collect { inHand ->
                        _uiState.update {
                            it.copy(
                                availableChecks = inHand,
                                selectedCheckId = if (it.selectedCheckId in inHand.map { c -> c.id })
                                    it.selectedCheckId else inHand.firstOrNull()?.id
                            )
                        }
                    }
                }
                if (fixedSupplierId != null) {
                    _uiState.update { it.copy(selectedSupplierId = fixedSupplierId) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در بارگذاری انتقال چک") }
            }
        }
    }

    fun onCheckSelected(checkId: Long) {
        _uiState.update { it.copy(selectedCheckId = checkId, error = null) }
    }

    fun onSupplierSelected(supplierId: Long) {
        _uiState.update { it.copy(selectedSupplierId = supplierId, error = null) }
    }

    fun transfer() {
        if (_uiState.value.isTransferred) return
        val state = _uiState.value
        val check = state.selectedCheck
        val supplierId = state.selectedSupplierId
        if (check == null || !CheckFlow.canTransfer(check)) {
            _uiState.update { it.copy(error = "چک قابل انتقالی انتخاب نشده است") }
            return
        }
        if (supplierId == null) {
            _uiState.update { it.copy(error = "تأمین‌کننده را انتخاب کنید") }
            return
        }
        viewModelScope.launch {
            try {
                checkRepository.transferToSupplier(check, supplierId)
                _uiState.update { it.copy(isTransferred = true, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "انتقال ناموفق بود: ${e.message}") }
            }
        }
    }

    companion object {
        fun factory(
            checkRepository: CheckRepository,
            supplierRepository: SupplierRepository,
            checkId: Long,
            supplierId: Long?
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CheckTransferViewModel(
                    checkRepository = checkRepository,
                    supplierRepository = supplierRepository,
                    checkId = checkId,
                    fixedSupplierId = supplierId
                )
            }
        }
    }
}
