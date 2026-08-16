package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Payment
import ir.furnitureshop.app.data.entity.PaymentMethod
import ir.furnitureshop.app.data.repository.CheckRepository
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.data.repository.PaymentRepository
import ir.furnitureshop.app.util.DigitNormalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentCreateUiState(
    val customerName: String? = null,
    val amountText: String = "",
    val method: PaymentMethod = PaymentMethod.CASH,
    // فیلدهای چک (فقط برای روش چک)
    val checkNumber: String = "",
    val bank: String = "",
    val dueDate: Long? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

/**
 * ثبت پرداخت از پروفایل مشتری.
 *
 * - نقد: فقط Payment ثبت می‌شود
 * - چک: Payment + Check به‌صورت ATOMIC ثبت می‌شوند (چک به مشتری متصل است،
 *   holder = در دست فروشگاه). شماره چک یکتا (Duplicate ممنوع).
 */
class PaymentCreateViewModel(
    private val paymentRepository: PaymentRepository,
    private val checkRepository: CheckRepository,
    customerRepository: CustomerRepository,
    private val customerId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentCreateUiState())
    val uiState: StateFlow<PaymentCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val customer = customerRepository.getById(customerId)
                _uiState.update { it.copy(customerName = customer?.name) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطا در بارگذاری مشتری") }
            }
        }
    }

    fun onAmountChange(raw: String) {
        _uiState.update { it.copy(amountText = DigitNormalizer.digitsOnly(raw), error = null) }
    }

    fun onMethodChange(method: PaymentMethod) {
        _uiState.update { it.copy(method = method, error = null) }
    }

    fun onCheckNumberChange(value: String) {
        _uiState.update { it.copy(checkNumber = value, error = null) }
    }

    fun onBankChange(value: String) {
        _uiState.update { it.copy(bank = value) }
    }

    fun onDueDateChange(date: Long?) {
        _uiState.update { it.copy(dueDate = date) }
    }

    fun save() {
        if (_uiState.value.isSaved) return
        val state = _uiState.value
        val amount = state.amountText.toLongOrNull() ?: 0L
        if (amount <= 0) {
            _uiState.update { it.copy(error = "مبلغ معتبری وارد کنید") }
            return
        }

        if (state.method == PaymentMethod.CHECK) {
            val checkNumber = state.checkNumber.trim()
            if (checkNumber.isEmpty()) {
                _uiState.update { it.copy(error = "شماره چک را وارد کنید") }
                return
            }
            viewModelScope.launch {
                try {
                    if (checkRepository.isDuplicateNumber(checkNumber)) {
                        _uiState.update { it.copy(error = "این شماره چک قبلاً ثبت شده است (Duplicate ممنوع)") }
                        return@launch
                    }
                    // ATOMIC: Payment + Check با هم ثبت می‌شوند
                    checkRepository.receiveFromCustomer(
                        customerId = customerId,
                        amount = amount,
                        checkNumber = checkNumber,
                        bank = state.bank,
                        dueDate = state.dueDate
                    )
                    _uiState.update { it.copy(isSaved = true, error = null) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message ?: "خطا در ثبت پرداخت") }
                }
            }
        } else {
            viewModelScope.launch {
                try {
                    paymentRepository.insert(
                        Payment(
                            customerId = customerId,
                            amount = amount,
                            date = System.currentTimeMillis(),
                            method = PaymentMethod.CASH.stored
                        )
                    )
                    _uiState.update { it.copy(isSaved = true, error = null) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message ?: "خطا در ثبت پرداخت") }
                }
            }
        }
    }

    companion object {
        fun factory(
            paymentRepository: PaymentRepository,
            checkRepository: CheckRepository,
            customerRepository: CustomerRepository,
            customerId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PaymentCreateViewModel(
                    paymentRepository = paymentRepository,
                    checkRepository = checkRepository,
                    customerRepository = customerRepository,
                    customerId = customerId
                )
            }
        }
    }
}
