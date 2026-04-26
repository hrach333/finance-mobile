package com.hrach.financeapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hrach.financeapp.data.db.entity.LocalAccountEntity
import com.hrach.financeapp.data.db.entity.LocalCategoryEntity
import com.hrach.financeapp.data.db.entity.LocalGroupEntity
import com.hrach.financeapp.data.db.entity.LocalTransactionEntity

@Dao
interface LocalFinanceDao {
    @Query("SELECT COUNT(*) FROM local_groups")
    suspend fun groupCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroup(group: LocalGroupEntity)

    @Query("SELECT * FROM local_groups ORDER BY id")
    suspend fun getGroups(): List<LocalGroupEntity>

    @Query("SELECT * FROM local_groups WHERE id = :id LIMIT 1")
    suspend fun getGroup(id: Int): LocalGroupEntity?

    @Update
    suspend fun updateGroup(group: LocalGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<LocalCategoryEntity>)

    @Insert
    suspend fun insertCategory(category: LocalCategoryEntity)

    @Update
    suspend fun updateCategory(category: LocalCategoryEntity)

    @Query("DELETE FROM local_categories WHERE id = :id AND isSystem = 0")
    suspend fun deleteCustomCategory(id: Int)

    @Query("SELECT * FROM local_categories WHERE groupId = :groupId ORDER BY isSystem DESC, type DESC, name")
    suspend fun getCategories(groupId: Int): List<LocalCategoryEntity>

    @Query("SELECT * FROM local_categories WHERE id = :id LIMIT 1")
    suspend fun getCategory(id: Int): LocalCategoryEntity?

    @Query("SELECT COALESCE(MAX(id), 1000) + 1 FROM local_categories")
    suspend fun nextCategoryId(): Int

    @Insert
    suspend fun insertAccount(account: LocalAccountEntity): Long

    @Update
    suspend fun updateAccount(account: LocalAccountEntity)

    @Query("DELETE FROM local_accounts WHERE id = :id")
    suspend fun deleteAccount(id: Int)

    @Query("SELECT * FROM local_accounts WHERE groupId = :groupId ORDER BY isActive DESC, name")
    suspend fun getAccounts(groupId: Int): List<LocalAccountEntity>

    @Query("SELECT * FROM local_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccount(id: Int): LocalAccountEntity?

    @Insert
    suspend fun insertTransaction(transaction: LocalTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: LocalTransactionEntity)

    @Query("DELETE FROM local_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("SELECT * FROM local_transactions WHERE groupId = :groupId ORDER BY transactionDate DESC, id DESC")
    suspend fun getTransactions(groupId: Int): List<LocalTransactionEntity>

    @Query("SELECT * FROM local_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransaction(id: Int): LocalTransactionEntity?
}
