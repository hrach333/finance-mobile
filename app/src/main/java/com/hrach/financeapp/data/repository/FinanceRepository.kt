package com.hrach.financeapp.data.repository

import android.util.Log
import com.google.gson.Gson
import com.hrach.financeapp.data.api.FinanceApi
import com.hrach.financeapp.data.db.dao.LocalFinanceDao
import com.hrach.financeapp.data.db.entity.LocalAccountEntity
import com.hrach.financeapp.data.db.entity.LocalCategoryEntity
import com.hrach.financeapp.data.db.entity.LocalGroupEntity
import com.hrach.financeapp.data.db.entity.LocalTransactionEntity
import com.hrach.financeapp.data.db.entity.PendingOperationEntity
import com.hrach.financeapp.data.dto.AddGroupMemberRequest
import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateGroupRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.ForgotPasswordRequest
import com.hrach.financeapp.data.dto.ForgotPasswordResponse
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.GroupMemberDto
import com.hrach.financeapp.data.dto.LoginRequest
import com.hrach.financeapp.data.dto.MessageResponse
import com.hrach.financeapp.data.dto.RegisterRequest
import com.hrach.financeapp.data.dto.ResetPasswordRequest
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateCategoryRequest
import com.hrach.financeapp.data.dto.UpdateGroupRequest
import com.hrach.financeapp.data.dto.UpdateGroupMemberRoleRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.dto.YandexMobileLoginRequest
import com.hrach.financeapp.data.network.NetworkMonitor
import com.hrach.financeapp.data.offline.OfflineManager
import java.io.IOException

class FinanceRepository(
    private val api: FinanceApi,
    private val offlineManager: OfflineManager? = null,
    private val networkMonitor: NetworkMonitor? = null,
    private val gson: Gson = Gson(),
    private val localFinanceDao: LocalFinanceDao? = null
) {
    private var localMode: Boolean = false

    fun setLocalMode(enabled: Boolean) {
        localMode = enabled
    }

    fun isLocalMode(): Boolean = localMode

    private fun localDao(): LocalFinanceDao =
        localFinanceDao ?: error("Локальная база данных недоступна")

    private suspend fun ensureLocalSeeded() {
        val dao = localDao()
        if (dao.groupCount() > 0) return

        dao.upsertGroup(LocalGroupEntity(id = OFFLINE_GROUP_ID, name = "Мой бюджет", baseCurrency = "RUB"))
        dao.upsertCategories(
            listOf(
                LocalCategoryEntity(1, OFFLINE_GROUP_ID, "EXPENSE", "Продукты", "food", true),
                LocalCategoryEntity(2, OFFLINE_GROUP_ID, "EXPENSE", "Транспорт", "transport", true),
                LocalCategoryEntity(3, OFFLINE_GROUP_ID, "EXPENSE", "Дом", "home", true),
                LocalCategoryEntity(4, OFFLINE_GROUP_ID, "EXPENSE", "Здоровье", "health", true),
                LocalCategoryEntity(5, OFFLINE_GROUP_ID, "EXPENSE", "Развлечения", "fun", true),
                LocalCategoryEntity(6, OFFLINE_GROUP_ID, "INCOME", "Зарплата", "money", true),
                LocalCategoryEntity(7, OFFLINE_GROUP_ID, "INCOME", "Подработка", "work", true),
                LocalCategoryEntity(8, OFFLINE_GROUP_ID, "INCOME", "Подарки", "gift", true)
            )
        )
        dao.insertAccount(
            LocalAccountEntity(
                groupId = OFFLINE_GROUP_ID,
                userId = null,
                name = "Основной счет",
                type = "cash",
                currency = "RUB",
                initialBalance = 0.0,
                currentBalance = 0.0,
                shared = true,
                isActive = true
            )
        )
    }

    private fun LocalGroupEntity.toDto() = GroupDto(id = id, name = name, baseCurrency = baseCurrency)
    private fun LocalAccountEntity.toDto() = AccountDto(id, groupId, userId, name, type, currency, initialBalance, currentBalance, shared, isActive)
    private fun LocalCategoryEntity.toDto() = CategoryDto(id, groupId, type, name, iconKey, isSystem)
    private fun LocalTransactionEntity.toDto() = TransactionDto(id, groupId, accountId, createdBy, type, amount, currency, categoryId, transactionDate, comment)

    private fun balanceDelta(type: String, amount: Double): Double =
        if (type.uppercase() == "INCOME") amount else -amount

    private suspend fun applyTransactionToAccount(accountId: Int, type: String, amount: Double) {
        val dao = localDao()
        val account = dao.getAccount(accountId) ?: return
        dao.updateAccount(account.copy(currentBalance = account.currentBalance + balanceDelta(type, amount)))
    }

    private suspend fun revertTransactionFromAccount(transaction: LocalTransactionEntity) {
        val dao = localDao()
        val account = dao.getAccount(transaction.accountId) ?: return
        dao.updateAccount(account.copy(currentBalance = account.currentBalance - balanceDelta(transaction.type, transaction.amount)))
    }

    private fun shouldSaveOffline(e: Throwable): Boolean {
        if (offlineManager == null) return false
        return networkMonitor?.isConnected() == false || e is IOException
    }

    private suspend fun saveOffline(
        operationType: String,
        jsonData: String,
        groupId: String? = null,
        remoteId: String? = null
    ) {
        offlineManager?.savePendingOperation(
            operationType = operationType,
            jsonData = jsonData,
            groupId = groupId,
            remoteId = remoteId
        )
    }
    suspend fun register(name: String, email: String, password: String) =
        api.register(RegisterRequest(name = name, email = email, password = password, passwordConfirmation = password))

    suspend fun login(email: String, password: String) = api.login(LoginRequest(email, password))

    suspend fun loginWithYandex(oauthToken: String) = api.loginWithYandex(YandexMobileLoginRequest(oauthToken = oauthToken))

    suspend fun forgotPassword(email: String): ForgotPasswordResponse =
        api.forgotPassword(ForgotPasswordRequest(email))

    suspend fun resetPassword(email: String, code: String, password: String): MessageResponse =
        api.resetPassword(ResetPasswordRequest(email = email, code = code, password = password, passwordConfirmation = password))

    suspend fun me() = api.me()
    suspend fun logout() {
        if (localMode) return
        api.logout()
    }

    suspend fun getGroups(): List<GroupDto> {
        if (localMode) {
            ensureLocalSeeded()
            return localDao().getGroups().map { it.toDto() }
        }
        return api.getGroups().data
    }

    suspend fun createGroup(name: String, baseCurrency: String): GroupDto {
        if (localMode) {
            ensureLocalSeeded()
            throw IllegalStateException("В офлайн режиме доступна одна группа: Мой бюджет")
        }
        return api.createGroup(CreateGroupRequest(name, baseCurrency))
    }

    suspend fun updateGroup(groupId: Int, name: String, baseCurrency: String) {
        if (localMode) {
            throw IllegalStateException("В офлайн режиме группа закреплена как Мой бюджет")
        }
        api.updateGroup(groupId, UpdateGroupRequest(name, baseCurrency))
    }

    suspend fun getGroupMembers(groupId: Int): List<GroupMemberDto> {
        if (localMode) return emptyList()
        return api.getGroupMembers(groupId).data
    }

    suspend fun addGroupMember(groupId: Int, email: String, role: String) {
        if (localMode) throw IllegalStateException(OFFLINE_MEMBERS_MESSAGE)
        api.addGroupMember(groupId, AddGroupMemberRequest(email, role))
    }

    suspend fun updateGroupMember(groupId: Int, memberId: Int, role: String) {
        if (localMode) throw IllegalStateException(OFFLINE_MEMBERS_MESSAGE)
        api.updateGroupMember(groupId, memberId, UpdateGroupMemberRoleRequest(role))
    }

    suspend fun deleteGroupMember(groupId: Int, memberId: Int) {
        if (localMode) throw IllegalStateException(OFFLINE_MEMBERS_MESSAGE)
        api.deleteGroupMember(groupId, memberId)
    }

    suspend fun getAccounts(groupId: Int): List<AccountDto> {
        if (localMode) {
            ensureLocalSeeded()
            return localDao().getAccounts(OFFLINE_GROUP_ID).map { it.toDto() }
        }
        return api.getAccounts(groupId).data
    }
    
    suspend fun createAccount(request: CreateAccountRequest) {
        if (localMode) {
            ensureLocalSeeded()
            localDao().insertAccount(
                LocalAccountEntity(
                    groupId = OFFLINE_GROUP_ID,
                    userId = null,
                    name = request.name,
                    type = request.type,
                    currency = request.currency,
                    initialBalance = request.initialBalance,
                    currentBalance = request.initialBalance,
                    shared = request.shared,
                    isActive = true
                )
            )
            return
        }

        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "Нет интернета. Счет сохранен в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_CREATE_ACCOUNT,
                    jsonData = gson.toJson(request),
                    groupId = request.groupId.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        try {
            api.createAccount(request)
        } catch (e: Exception) {
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сеть разорвалась. Счет сохранен в офлайн режиме")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_CREATE_ACCOUNT,
                    jsonData = gson.toJson(request),
                    groupId = request.groupId.toString()
                )
                return
            }
            throw e
        }
    }
    
    suspend fun updateAccount(id: Int, request: UpdateAccountRequest) {
        if (localMode) {
            val dao = localDao()
            val existing = dao.getAccount(id) ?: return
            dao.updateAccount(
                existing.copy(
                    name = request.name,
                    type = request.type,
                    currency = request.currency,
                    initialBalance = request.initialBalance,
                    currentBalance = request.currentBalance,
                    shared = request.shared,
                    isActive = request.isActive
                )
            )
            return
        }

        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "Нет интернета. Обновление счета сохранено в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_UPDATE_ACCOUNT,
                    jsonData = gson.toJson(request),
                    remoteId = id.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        try {
            api.updateAccount(id, request)
        } catch (e: Exception) {
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сеть разорвалась. Обновление счета сохранено в офлайн режиме")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_UPDATE_ACCOUNT,
                    jsonData = gson.toJson(request),
                    remoteId = id.toString()
                )
                return
            }
            throw e
        }
    }
    
    suspend fun deleteAccount(id: Int) {
        if (localMode) {
            localDao().deleteAccount(id)
            return
        }

        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "Нет интернета. Удаление счета сохранено в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_DELETE_ACCOUNT,
                    jsonData = "{}",
                    remoteId = id.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        try {
            api.deleteAccount(id)
        } catch (e: Exception) {
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сеть разорвалась. Удаление счета сохранено в офлайн режиме")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_DELETE_ACCOUNT,
                    jsonData = "{}",
                    remoteId = id.toString()
                )
                return
            }
            throw e
        }
    }
    suspend fun getCategories(groupId: Int): List<CategoryDto> {
        if (localMode) {
            ensureLocalSeeded()
            return localDao().getCategories(OFFLINE_GROUP_ID).map { it.toDto() }
        }
        return api.getCategories(groupId).data
    }
    
    suspend fun createCategory(request: CreateCategoryRequest) {
        if (localMode) {
            ensureLocalSeeded()
            val dao = localDao()
            dao.insertCategory(
                LocalCategoryEntity(
                    id = dao.nextCategoryId(),
                    groupId = OFFLINE_GROUP_ID,
                    type = request.type,
                    name = request.name,
                    iconKey = request.iconKey,
                    isSystem = false
                )
            )
            return
        }

        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "Нет интернета. Категория сохранена в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_CREATE_CATEGORY,
                    jsonData = gson.toJson(request),
                    groupId = request.groupId.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        try {
            api.createCategory(request)
        } catch (e: Exception) {
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сеть разорвалась. Категория сохранена в офлайн режиме")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_CREATE_CATEGORY,
                    jsonData = gson.toJson(request),
                    groupId = request.groupId.toString()
                )
                return
            }
            throw e
        }
    }
    
    suspend fun updateCategory(id: Int, name: String, type: String, iconKey: String? = null) {
        if (localMode) {
            val dao = localDao()
            val category = dao.getCategory(id) ?: return
            if (category.isSystem) {
                throw IllegalStateException("Готовые категории нельзя редактировать. Создай свою категорию рядом.")
            }
            dao.updateCategory(category.copy(name = name, type = type, iconKey = iconKey))
            return
        }

        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "Нет интернета. Обновление категории сохранено в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_UPDATE_CATEGORY,
                    jsonData = gson.toJson(UpdateCategoryRequest(name = name, type = type, iconKey = iconKey)),
                    remoteId = id.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        try {
            api.updateCategory(id, UpdateCategoryRequest(name = name, type = type, iconKey = iconKey))
        } catch (e: Exception) {
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сеть разорвалась. Обновление категории сохранено в офлайн режиме")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_UPDATE_CATEGORY,
                    jsonData = gson.toJson(UpdateCategoryRequest(name = name, type = type, iconKey = iconKey)),
                    remoteId = id.toString()
                )
                return
            }
            throw e
        }
    }
    
    suspend fun deleteCategory(id: Int) {
        if (localMode) {
            val dao = localDao()
            val category = dao.getCategory(id) ?: return
            if (category.isSystem) {
                throw IllegalStateException("Готовые категории нельзя удалить, чтобы операции всегда было куда отнести.")
            }
            dao.deleteCustomCategory(id)
            return
        }

        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "Нет интернета. Удаление категории сохранено в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_DELETE_CATEGORY,
                    jsonData = "{}",
                    remoteId = id.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        try {
            api.deleteCategory(id)
        } catch (e: Exception) {
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сеть разорвалась. Удаление категории сохранено в офлайн режиме")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_DELETE_CATEGORY,
                    jsonData = "{}",
                    remoteId = id.toString()
                )
                return
            }
            throw e
        }
    }
    suspend fun getTransactions(groupId: Int): List<TransactionDto> {
        if (localMode) {
            ensureLocalSeeded()
            return localDao().getTransactions(OFFLINE_GROUP_ID).map { it.toDto() }
        }
        return api.getTransactions(groupId).data
    }
    
    suspend fun getSummary(groupId: Int, startDate: String, endDate: String): SummaryDto {
        if (localMode) {
            ensureLocalSeeded()
            val transactions = localDao().getTransactions(OFFLINE_GROUP_ID)
                .filter { it.transactionDate.substringBefore("T") in startDate..endDate }
            val income = transactions
                .filter { it.type.uppercase() == "INCOME" }
                .sumOf { it.amount }
            val expense = transactions
                .filter { it.type.uppercase() == "EXPENSE" }
                .sumOf { it.amount }
            val balance = localDao().getAccounts(OFFLINE_GROUP_ID).sumOf { it.currentBalance }
            return SummaryDto(income = income, expense = expense, balance = balance)
        }
        return api.getSummary(groupId, startDate, endDate)
    }
    
    suspend fun createTransaction(request: CreateTransactionRequest) {
        if (localMode) {
            ensureLocalSeeded()
            val transaction = LocalTransactionEntity(
                groupId = OFFLINE_GROUP_ID,
                accountId = request.accountId,
                createdBy = null,
                type = request.type,
                amount = request.amount,
                currency = request.currency,
                categoryId = request.categoryId,
                transactionDate = request.transactionDate,
                comment = request.comment
            )
            localDao().insertTransaction(transaction)
            applyTransactionToAccount(request.accountId, request.type, request.amount)
            return
        }

        val isConnected = networkMonitor?.isConnected() == true
        Log.d("FinanceRepository", "createTransaction - Интернет: $isConnected, OfflineManager: ${offlineManager != null}")
        
        // Сначала проверяем интернет ДО отправки запроса
        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "❌ Нет интернета. Сохранена транзакция в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_CREATE_TRANSACTION,
                    jsonData = gson.toJson(request),
                    groupId = request.groupId.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        // Есть интернет - отправляем
        try {
            Log.d("FinanceRepository", "✅ Есть интернет. Отправляем транзакцию на сервер")
            api.createTransaction(request)
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Ошибка при отправке: ${e.message}")
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сохранена транзакция в офлайн режиме после ошибки сети")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_CREATE_TRANSACTION,
                    jsonData = gson.toJson(request),
                    groupId = request.groupId.toString()
                )
                return
            }
            throw e
        }
    }
    
    suspend fun updateTransaction(id: Int, request: UpdateTransactionRequest) {
        if (localMode) {
            val dao = localDao()
            val existing = dao.getTransaction(id) ?: return
            revertTransactionFromAccount(existing)
            dao.updateTransaction(
                existing.copy(
                    accountId = request.accountId,
                    type = request.type,
                    amount = request.amount,
                    currency = request.currency,
                    categoryId = request.categoryId,
                    transactionDate = request.transactionDate,
                    comment = request.comment
                )
            )
            applyTransactionToAccount(request.accountId, request.type, request.amount)
            return
        }

        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "Нет интернета. Обновление транзакции сохранено в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_UPDATE_TRANSACTION,
                    jsonData = gson.toJson(request),
                    remoteId = id.toString(),
                    groupId = request.groupId.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        try {
            api.updateTransaction(id, request)
        } catch (e: Exception) {
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сеть разорвалась. Обновление транзакции сохранено в офлайн режиме")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_UPDATE_TRANSACTION,
                    jsonData = gson.toJson(request),
                    remoteId = id.toString(),
                    groupId = request.groupId.toString()
                )
                return
            }
            throw e
        }
    }
    
    suspend fun deleteTransaction(id: Int) {
        if (localMode) {
            val dao = localDao()
            val existing = dao.getTransaction(id) ?: return
            revertTransactionFromAccount(existing)
            dao.deleteTransaction(id)
            return
        }

        if (networkMonitor?.isConnected() == false) {
            if (offlineManager != null) {
                Log.d("FinanceRepository", "Нет интернета. Удаление транзакции сохранено в офлайн режиме")
                offlineManager.savePendingOperation(
                    operationType = PendingOperationEntity.TYPE_DELETE_TRANSACTION,
                    jsonData = "{}",
                    remoteId = id.toString()
                )
                return
            } else {
                throw Exception("Нет интернета и OfflineManager не инициализирован")
            }
        }
        
        try {
            api.deleteTransaction(id)
        } catch (e: Exception) {
            if (shouldSaveOffline(e)) {
                Log.d("FinanceRepository", "Сеть разорвалась. Удаление транзакции сохранено в офлайн режиме")
                saveOffline(
                    operationType = PendingOperationEntity.TYPE_DELETE_TRANSACTION,
                    jsonData = "{}",
                    remoteId = id.toString()
                )
                return
            }
            throw e
        }
    }

    companion object {
        const val OFFLINE_GROUP_ID = 1
        const val OFFLINE_MEMBERS_MESSAGE =
            "Совместный бюджет, участники и защита от потери данных доступны после регистрации."
    }
}
