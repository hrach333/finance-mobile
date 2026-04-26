package com.hrach.financeapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_transactions")
data class LocalTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val groupId: Int,
    val accountId: Int,
    val createdBy: Int?,
    val type: String,
    val amount: Double,
    val currency: String,
    val categoryId: Int?,
    val transactionDate: String,
    val comment: String?
)
