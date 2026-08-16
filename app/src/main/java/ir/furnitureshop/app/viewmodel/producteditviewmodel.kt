package ir.furnitureshop.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Product
import ir.furnitureshop.app.data.local.ProductImageStore
import ir.furnitureshop.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductEditUiState(
    val productId: Long = 0,
    val name: String = "",
    val imagePath: String? = null,
    val basePriceText: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val priceError: String? = null,
    val saveError: String? = null,
    val notFound: Boolean = false,
    val isSaved: Boolean = false
)

class ProductEditViewModel(
    private val repository: ProductRepository,
    private val imageStore: ProductImageStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    private var originalImagePath: String? = null

    fun loadProduct(productId: Long) {
        if (productId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val product = repository.getById(productId)
            if (product == null) {
                _uiState.update { it.copy(isLoading = false, notFound = true) }
            } else {
                originalImagePath = product.image
                _uiState.update {
                    it.copy(
                        productId = product.id,
                        name = product.name,
                        imagePath = product.image,
                        basePriceText = if (product.basePrice > 0) product.basePrice.toString() else "",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onBasePriceChange(raw: String) {
        // تبدیل ارقام فارسی/عربی به انگلیسی و نگهداشتن فقط رقم
        val text = raw.map { c ->
            when (c) {
                in '۰'..'۹' -> '0' + (c - '۰')
                in '٠'..'٩' -> '0' + (c - '٠')
                else -> c
            }
        }.joinToString("")
        if (text.isEmpty() || text.all { it in '0'..'9' }) {
            _uiState.update { it.copy(basePriceText = text, priceError = null) }
        }
    }

    fun onImagePicked(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val savedPath = imageStore.saveImage(uri)
            if (savedPath != null) {
                _uiState.update { it.copy(imagePath = savedPath) }
            }
        }
    }

    fun save() {
        if (_uiState.value.isSaved) return
        val state = _uiState.value
        val name = state.name.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(nameError = "نام محصول را وارد کنید") }
            return
        }
        // اگر عدد واردشده قابل تبدیل نباشد (مثلاً بیش از حد بزرگ)، به‌جای ذخیره ۰ خطا نشان بده
        val price = if (state.basePriceText.isBlank()) 0L else state.basePriceText.toLongOrNull()
        if (price == null) {
            _uiState.update { it.copy(priceError = "مبلغ واردشده معتبر نیست") }
            return
        }

        viewModelScope.launch {
            try {
                val product = Product(
                    id = state.productId,
                    name = name,
                    image = state.imagePath,
                    basePrice = price
                )
                if (state.productId > 0) {
                    repository.update(product)
                    // اگر تصویر عوض شده، تصویر قبلی را پاک کن
                    if (originalImagePath != null && originalImagePath != state.imagePath) {
                        imageStore.deleteImage(originalImagePath)
                    }
                } else {
                    repository.insert(product)
                }
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                // به‌جای کرش، خطا نمایش داده می‌شود
                _uiState.update {
                    it.copy(saveError = e.message ?: "خطا در ذخیره محصول")
                }
            }
        }
    }

    companion object {
        fun factory(
            repository: ProductRepository,
            imageStore: ProductImageStore
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProductEditViewModel(repository, imageStore) }
        }
    }
}
