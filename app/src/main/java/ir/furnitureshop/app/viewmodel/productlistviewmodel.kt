package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ir.furnitureshop.app.data.entity.Product
import ir.furnitureshop.app.data.local.ProductImageStore
import ir.furnitureshop.app.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductListUiState(
    val query: String = "",
    val products: List<Product> = emptyList(),
    val productToDelete: Product? = null,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModel(
    private val repository: ProductRepository,
    private val imageStore: ProductImageStore
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _productToDelete = MutableStateFlow<Product?>(null)
    private val _deleteError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProductListUiState> = combine(
        _query,
        _query.flatMapLatest { repository.observeProducts(it) },
        _productToDelete,
        _deleteError
    ) { query, products, toDelete, error ->
        ProductListUiState(
            query = query,
            products = products,
            productToDelete = toDelete,
            error = error
        )
    }
        .catch { e ->
            emit(ProductListUiState(error = e.message ?: "خطا در بارگذاری محصولات"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductListUiState())

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onDeleteClick(product: Product) {
        _productToDelete.value = product
    }

    fun onDeleteConfirmed() {
        val product = _productToDelete.value ?: return
        _productToDelete.value = null
        viewModelScope.launch {
            try {
                imageStore.deleteImage(product.image)
                repository.delete(product)
            } catch (e: Exception) {
                _deleteError.value = e.message ?: "خطا در حذف محصول"
            }
        }
    }

    fun onDeleteDismissed() {
        _productToDelete.value = null
    }

    companion object {
        fun factory(
            repository: ProductRepository,
            imageStore: ProductImageStore
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProductListViewModel(repository, imageStore) }
        }
    }
}
