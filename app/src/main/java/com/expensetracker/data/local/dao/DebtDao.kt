package com.expensetracker.data.local.dao

import androidx.room.*
import com.expensetracker.data.model.DebtEntity
import com.expensetracker.data.model.DebtType
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE type = :type ORDER BY isSettled ASC, createdAt DESC")
    fun getDebtsByType(type: DebtType): Flow<List<DebtEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM debts WHERE type = :type AND isSettled = 0")
    fun getTotalUnsettledAmountByType(type: DebtType): Flow<Double>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Query("UPDATE debts SET isSettled = :isSettled WHERE id = :id")
    fun setSettledStatus(id: Long, isSettled: Boolean)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    fun deleteDebtById(id: Long)
}
