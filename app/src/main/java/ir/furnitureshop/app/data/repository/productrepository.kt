package ir.furnitureshop.app.data.repository

import ir.furnitureshop.app.data.dao.ProductDao
import ir.furnitureshop.app.data.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    fun observeProducts(query: String = ""): Flow<List<Product>> =
        if (query.isBlank()) productDao.observeAll()
        else productDao.observeSearch(query.trim())

    suspend fun getById(id: Long): Product? = productDao.getById(id)

    suspend fun insert(product: Product): Long = productDao.insert(product)

    suspend fun update(product: Product) = productDao.update(product)

    suspend fun delete(product: Product) = productDao.delete(product)
}
