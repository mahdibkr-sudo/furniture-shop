package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Customer
import ir.furnitureshop.app.data.entity.Invoice
import ir.furnitureshop.app.data.entity.InvoiceItem
import ir.furnitureshop.app.data.entity.Product
import ir.furnitureshop.app.data.repository.CustomerRepository
import ir.furnitureshop.app.data.repository.InvoiceRepository
import ir.furnitureshop.app.data.repository.ProductRepository
import ir.furnitureshop.app.util.DigitNormalizer
import ir.furnitureshop.app.util.InvoiceCalculator
import ir.furnitureshop.app.util.InvoiceCartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvoiceCreateUiState(
    val loadError: String? = null,
    val customers: List<Customer> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedCustomerId: Long? = null,
    val items: List<InvoiceCartItem> = emptyList(),
    val discountText: String = "",
    val fabricCustomerProvided: Boolean = false,
    val fabricMetersText: String = "",
    val fabricUnitPriceText: String = "",
    val fabricNameText: String = "",
    val error: String? = null,
    val savedInvoiceId: Long? = null
) {
    val selectedCustomer: Customer?
        get() = customers.find { it.id == selectedCustomerId }

    val subtotal: Long
        get() = InvoiceCalculator.subtotal(items)

    val discount: Long
        get() = discountText.toLongOrNull() ?: 0L

    /** متراژ پارچه (مثلاً ۳/۵ متر). */
    val fabricMeters: Double
        get() = fabricMetersText.toDoubleOrNull() ?: 0.0

    /** قیمت هر متر پارچه. */
    val fabricUnitPrice: Long
        get() = fabricUnitPriceText.toLongOrNull() ?: 0L

    /** مبلغ کل پارچه = متراژ × قیمت واحد؛ فقط وقتی لحاظ می‌شود که مشتری خودش پارچه نیاورده باشد */
    val fabricAmount: Long
        get() = if (fabricCustomerProvided) 0L else ((fabricMeters * fabricUnitPrice).toLong())

    val total: Long
        get() = InvoiceCalculator.total(
            subtotal = subtotal,
            discount = discount,
            fabricCustomerProvided = fabricCustomerProvided,
            fabricAmount = fabricAmount
        )
}

/**
 * صدور فاکتور — فقط صدور؛ Payment در این تسک ثبت نمی‌شود.
 */
class InvoiceCreateViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _form = MutableStateFlow(InvoiceCreateUiState())

    val uiState: StateFlow<InvoiceCreateUiState> =
        combine(_customers, _products, _form) { customers, products, form ->
            form.copy(customers = customers, products = products)
        }.catch { e -> emit(InvoiceCreateUiState(loadError = e.message ?: "خطا در بارگذاری فرم فاکتور")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceCreateUiState())

    init {
        viewModelScope.launch {
            try {
                customerRepository.observeCustomers().collect { _customers.value = it }
            } catch (e: Exception) {
                _form.update { it.copy(loadError = e.message ?: "خطا در بارگذاری مشتریان") }
            }
        }
        viewModelScope.launch {
            try {
                productRepository.observeProducts().collect { _products.value = it }
            } catch (e: Exception) {
                _form.update { it.copy(loadError = e.message ?: "خطا در بارگذاری محصولات") }
            }
        }
    }

    fun onCustomerSelected(customerId: Long) {
        _form.update { it.copy(selectedCustomerId = customerId, error = null) }
    }

    /** افزودن محصول با قیمت پایه لحظه‌ای (قیمت تاریخی) */
    fun onProductSelected(product: Product) {
        _form.update { state ->
            if (state.items.any { it.productId == product.id }) {
                state.copy(error = "«${product.name}» قبلاً به فاکتور اضافه شده است")
            } else {
                state.copy(
                    items = state.items + InvoiceCalculator.cartItemFrom(product),
                    error = null
                )
            }
        }
    }

    fun onQuantityChange(item: InvoiceCartItem, delta: Int) {
        _form.update { state ->
            state.copy(
                items = state.items.map {
                    if (it.productId == item.productId) {
                        it.copy(quantity = (it.quantity + delta).coerceAtLeast(1))
                    } else it
                }
            )
        }
    }

    fun onUnitPriceChange(item: InvoiceCartItem, raw: String) {
        val text = DigitNormalizer.digitsOnly(raw)
        _form.update { state ->
            state.copy(
                items = state.items.map {
                    if (it.productId == item.productId) {
                        it.copy(unitPrice = text.toLongOrNull() ?: 0L)
                    } else it
                }
            )
        }
    }

    fun onRemoveItem(item: InvoiceCartItem) {
        _form.update { state ->
            state.copy(
                items = state.items.filterNot { it.productId == item.productId },
                error = null
            )
        }
    }

    fun onDiscountChange(raw: String) {
        _form.update { it.copy(discountText = DigitNormalizer.digitsOnly(raw), error = null) }
    }

    fun onFabricModeChange(customerProvided: Boolean) {
        _form.update { it.copy(fabricCustomerProvided = customerProvided) }
    }

    /** متراژ پارچه — اعشاری (ارقام و نقطه/کاما). */
    fun onFabricMetersChange(raw: String) {
        val normalized = raw.replace('٫', '.').replace(',', '.')
            .filter { it.isDigit() || it == '.' }
        _form.update { it.copy(fabricMetersText = normalized, error = null) }
    }

    /** قیمت هر متر پارچه. */
    fun onFabricUnitPriceChange(raw: String) {
        _form.update { it.copy(fabricUnitPriceText = DigitNormalizer.digitsOnly(raw), error = null) }
    }

    /** نام پارچه — اختیاری؛ در هر دو حالت تهیه توسط مشتری یا فروشگاه ثبت می‌شود. */
    fun onFabricNameChange(raw: String) {
        _form.update { it.copy(fabricNameText = raw, error = null) }
    }

    fun save() {
        if (_form.value.savedInvoiceId != null) return
        val state = _form.value
        if (state.selectedCustomerId == null) {
            _form.update { it.copy(error = "ابتدا مشتری را انتخاب کنید") }
            return
        }
        if (state.items.isEmpty()) {
            _form.update { it.copy(error = "حداقل یک محصول به فاکتور اضافه کنید") }
            return
        }
        if (!state.fabricCustomerProvided) {
            if (state.fabricMeters <= 0.0 || state.fabricUnitPrice <= 0) {
                _form.update { it.copy(error = "متراژ و قیمت واحد پارچه را وارد کنید") }
                return
            }
        }

        viewModelScope.launch {
            try {
                val invoice = Invoice(
                    customerId = state.selectedCustomerId!!,
                    invoiceNumber = invoiceRepository.nextInvoiceNumber(),
                    date = System.currentTimeMillis(),
                    discount = state.discount,
                    fabricCustomerProvided = state.fabricCustomerProvided,
                    fabricMeters = state.fabricMeters,
                    fabricUnitPrice = state.fabricUnitPrice,
                    fabricAmount = state.fabricAmount,
                    fabricName = state.fabricNameText.ifBlank { null },
                    totalAmount = state.total
                )
                val items = state.items.map {
                    InvoiceItem(
                        invoiceId = 0,
                        productId = it.productId,
                        productName = it.productName,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice
                    )
                }
                val id = invoiceRepository.createInvoice(invoice, items)
                _form.update { it.copy(savedInvoiceId = id, error = null) }
            } catch (e: Exception) {
                _form.update { it.copy(error = e.message ?: "خطا در ثبت فاکتور") }
            }
        }
    }

    companion object {
        fun factory(
            invoiceRepository: InvoiceRepository,
            productRepository: ProductRepository,
            customerRepository: CustomerRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                InvoiceCreateViewModel(
                    invoiceRepository = invoiceRepository,
                    productRepository = productRepository,
                    customerRepository = customerRepository
                )
            }
        }
    }
}
