package com.hrach.financeapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_accounts")
data class LocalAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val groupId: Int,
    val userId: Int?,
    val name: String,
    val type: String,
    val currency: String,
    val initialBalance: Double,
    val currentBalance: Double,
    val shared: Boolean,
    val isActive: Boolean
)
