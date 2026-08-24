package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.dao.CheckWithCustomer
import ir.furnitureshop.app.data.entity.CheckFlow
import ir.furnitureshop.app.data.entity.Supplier
import ir.furnitureshop.app.data.repository.CheckRepository
import ir.furnitureshop.app.data.repository.SupplierRepository
import ir.furnitureshop.app.util.YearFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class CheckListUiState(
    val loadError: String? = null,
    val checks: List<CheckWithCustomer> = emptyList(),
    val suppliers: List<Supplier> = emptyList()
) {
    /** نام صاحب فعلی برای نمایش (با نگاشت id تأمین‌کننده → نام) */
    fun holderLabel(holder: String): String {
        val supplierId = CheckFlow.supplierIdOf(holder)
        val name = supplierId?.let { id -> suppliers.find { it.id == id }?.name }
        return CheckFlow.holderLabel(holder, name)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CheckListViewModel(
    checkRepository: CheckRepository,
    supplierRepository: SupplierRepository
) : ViewModel() {

    val uiState: StateFlow<CheckListUiState> =
        YearFilter.selectedYear.flatMapLatest { year ->
            combine(
                checkRepository.observeAllWithCustomer(),
                supplierRepository.observeSuppliers()
            ) { checks, suppliers ->
                CheckListUiState(
                    checks = checks.filter { YearFilter.isInYear(it.check.receiveDate, year) },
                    suppliers = suppliers
                )
            }
        }.catch { e -> emit(CheckListUiState(loadError = e.message ?: "خطا در بارگذاری چک‌ها")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckListUiState())

    companion object {
        fun factory(
            checkRepository: CheckRepository,
            supplierRepository: SupplierRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { CheckListViewModel(checkRepository, supplierRepository) }
        }
    }
}
