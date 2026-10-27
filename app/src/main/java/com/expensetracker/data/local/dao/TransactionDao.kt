package com.expensetracker.data.local.dao

import androidx.room.*
import com.expensetracker.data.model.TransactionEntity
import com.expensetracker.data.model.TransactionType
import com.expensetracker.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY timestamp DESC")
    fun getTransactionsBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionWithDetails?

    @Transaction
    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR targetAccountId = :accountId ORDER BY timestamp DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionWithDetails>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = :type AND timestamp BETWEEN :startTimestamp AND :endTimestamp")
    fun getTotalAmountByType(type: TransactionType, startTimestamp: Long, endTimestamp: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    fun deleteAllTransactions()
}
