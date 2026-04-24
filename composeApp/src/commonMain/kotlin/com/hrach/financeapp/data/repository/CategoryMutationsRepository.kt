package com.hrach.financeapp.data.repository

interface CategoryMutationsRepository {
    suspend fun createCategory(
        groupId: Int,
        name: String,
        type: String,
        iconKey: String?
    )

    suspend fun updateCategory(
        categoryId: Int,
        name: String,
        type: String,
        iconKey: String?
    )

    suspend fun deleteCategory(categoryId: Int)
}
