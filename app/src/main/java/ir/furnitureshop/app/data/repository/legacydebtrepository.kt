package ir.furnitureshop.app.data.repository

import ir.furnitureshop.app.data.dao.LegacyDebtDao
import ir.furnitureshop.app.data.entity.LegacyDebt
import kotlinx.coroutines.flow.Flow

class LegacyDebtRepository(private val dao: LegacyDebtDao) {

    fun observeByCustomer(customerId: Long): Flow<List<LegacyDebt>> =
        dao.observeByCustomer(customerId)

    fun observeAll(): Flow<List<LegacyDebt>> =
        dao.observeAll()

    suspend fun insert(debt: LegacyDebt): Long =
        dao.insert(debt)
}
