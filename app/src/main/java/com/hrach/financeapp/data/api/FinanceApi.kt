package com.hrach.financeapp.data.api

import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.ApiListResponse
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateCategoryRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FinanceApi {
    @GET("groups")
    suspend fun getGroups(): ApiListResponse<GroupDto>

    @GET("accounts")
    suspend fun getAccounts(@Query("groupId") groupId: Int): ApiListResponse<AccountDto>

    @POST("accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): AccountDto

    @PUT("accounts/{id}")
    suspend fun updateAccount(@Path("id") id: Int, @Body request: UpdateAccountRequest): AccountDto

    @DELETE("accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: Int): Response<Unit>

    @GET("categories")
    suspend fun getCategories(@Query("groupId") groupId: Int): ApiListResponse<CategoryDto>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): CategoryDto

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body request: UpdateCategoryRequest): CategoryDto

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

    @GET("transactions")
    suspend fun getTransactions(@Query("groupId") groupId: Int): ApiListResponse<TransactionDto>

    @GET("analytics/summary")
    suspend fun getSummary(
        @Query("groupId") groupId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): SummaryDto

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): TransactionDto

    @PUT("transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: Int, @Body request: UpdateTransactionRequest): TransactionDto

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Unit>
}
