package com.hrach.financeapp.data.offline

import kotlinx.serialization.Serializable

@Serializable
data class OfflineFinanceDatabase(
    val groups: List<OfflineGroupEntity> = listOf(OfflineGroupEntity()),
    val accounts: List<OfflineAccountEntity> = listOf(OfflineAccountEntity()),
    val categories: List<OfflineCategoryEntity> = defaultOfflineCategories,
    val transactions: List<OfflineTransactionEntity> = emptyList(),
    val nextAccountId: Int = 2,
    val nextCategoryId: Int = 1000,
    val nextTransactionId: Int = 1
)

@Serializable
data class OfflineGroupEntity(
    val id: Int = 1,
    val name: String = "Мой бюджет",
    val baseCurrency: String = "RUB"
)

@Serializable
data class OfflineAccountEntity(
    val id: Int = 1,
    val groupId: Int = 1,
    val userId: Int? = null,
    val name: String = "Основной счет",
    val type: String = "CASH",
    val currency: String = "RUB",
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val shared: Boolean = true,
    val isActive: Boolean = true
)

@Serializable
data class OfflineCategoryEntity(
    val id: Int,
    val groupId: Int = 1,
    val type: String,
    val name: String,
    val iconKey: String? = null,
    val isSystem: Boolean = true
)

@Serializable
data class OfflineTransactionEntity(
    val id: Int,
    val groupId: Int = 1,
    val accountId: Int,
    val createdBy: Int? = null,
    val type: String,
    val amount: Double,
    val currency: String,
    val categoryId: Int?,
    val transactionDate: String,
    val comment: String? = null
)

val defaultOfflineCategories = listOf(
    OfflineCategoryEntity(1, type = "EXPENSE", name = "Продукты", iconKey = "food"),
    OfflineCategoryEntity(2, type = "EXPENSE", name = "Транспорт", iconKey = "transport"),
    OfflineCategoryEntity(3, type = "EXPENSE", name = "Дом", iconKey = "home"),
    OfflineCategoryEntity(4, type = "EXPENSE", name = "Здоровье", iconKey = "health"),
    OfflineCategoryEntity(5, type = "EXPENSE", name = "Развлечения", iconKey = "fun"),
    OfflineCategoryEntity(6, type = "INCOME", name = "Зарплата", iconKey = "money"),
    OfflineCategoryEntity(7, type = "INCOME", name = "Подработка", iconKey = "work"),
    OfflineCategoryEntity(8, type = "INCOME", name = "Подарки", iconKey = "gift")
)
