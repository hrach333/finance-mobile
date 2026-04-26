package com.hrach.financeapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_categories")
data class LocalCategoryEntity(
    @PrimaryKey
    val id: Int,
    val groupId: Int,
    val type: String,
    val name: String,
    val iconKey: String?,
    val isSystem: Boolean
)
