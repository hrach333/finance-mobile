package com.hrach.financeapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_groups")
data class LocalGroupEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val baseCurrency: String
)
