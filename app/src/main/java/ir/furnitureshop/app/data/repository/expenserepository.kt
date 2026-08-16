package ir.furnitureshop.app.data.repository

import ir.furnitureshop.app.data.dao.ExpenseDao
import ir.furnitureshop.app.data.entity.Expense
import kotlinx.coroutines.flow.Flow

/**
 * مخزن Expense — مستقل از Supplier و Check؛ فقط CRUD.
 */
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun observeAll(): Flow<List<Expense>> = expenseDao.observeAll()

    fun observeById(id: Long): Flow<Expense?> = expenseDao.observeById(id)

    suspend fun getById(id: Long): Expense? = expenseDao.getById(id)

    suspend fun insert(expense: Expense): Long = expenseDao.insert(expense)

    suspend fun update(expense: Expense) = expenseDao.update(expense)

    suspend fun delete(expense: Expense) = expenseDao.delete(expense)
}
