package com.expensetracker.data.local.dao

import androidx.room.*
import com.expensetracker.data.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL AND (monthYear = :monthYear OR monthYear = 'DEFAULT') LIMIT 1")
    fun getOverallBudget(monthYear: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND (monthYear = :monthYear OR monthYear = 'DEFAULT') LIMIT 1")
    fun getBudgetForCategory(categoryId: Long, monthYear: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    fun deleteBudgetById(id: Long)
}
