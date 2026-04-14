package com.hrach.financeapp.data.repository

import android.util.Log
import com.google.gson.Gson
import com.hrach.financeapp.data.api.FinanceApi
import com.hrach.financeapp.data.db.entity.PendingOperationEntity
import com.hrach.financeapp.data.dto.AddGroupMemberRequest
import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateGroupRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.ForgotPasswordRequest
import com.hrach.financeapp.data.dto.ForgotPasswordResponse
import com.hrach.financeapp.data.dto.LoginRequest
import com.hrach.financeapp.data.dto.MessageResponse
import com.hrach.financeapp.data.dto.RegisterRequest
import com.hrach.financeapp.data.dto.ResetPasswordRequest
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateCategoryRequest
import com.hrach.financeapp.data.dto.UpdateGroupRequest
import com.hrach.financeapp.data.dto.UpdateGroupMemberRoleRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.network.NetworkMonitor
import com.hrach.financeapp.data.offline.OfflineManager
import java.io.IOException

class FinanceRepository(
    private val api: FinanceApi,
    private val offlineManager: OfflineManager? = null,
    private val networkMonitor: NetworkMonitor? = null,
    private val gson: Gson = Gson()
) {

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

    suspend fun forgotPassword(email: String): ForgotPasswordResponse =
        api.forgotPassword(ForgotPasswordRequest(email))

    suspend fun resetPassword(email: String, code: String, password: String): MessageResponse =
        api.resetPassword(ResetPasswordRequest(email = email, code = code, password = password, passwordConfirmation = password))

    suspend fun me() = api.me()
    suspend fun logout() = api.logout()

    suspend fun getGroups() = api.getGroups().data
    suspend fun createGroup(name: String, baseCurrency: String) = api.createGroup(CreateGroupRequest(name, baseCurrency))
    suspend fun updateGroup(groupId: Int, name: String, baseCurrency: String) = api.updateGroup(groupId, UpdateGroupRequest(name, baseCurrency))
    suspend fun getGroupMembers(groupId: Int) = api.getGroupMembers(groupId).data
    suspend fun addGroupMember(groupId: Int, email: String, role: String) = api.addGroupMember(groupId, AddGroupMemberRequest(email, role))
    suspend fun updateGroupMember(groupId: Int, memberId: Int, role: String) = api.updateGroupMember(groupId, memberId, UpdateGroupMemberRoleRequest(role))
    suspend fun deleteGroupMember(groupId: Int, memberId: Int) = api.deleteGroupMember(groupId, memberId)

    suspend fun getAccounts(groupId: Int) = api.getAccounts(groupId).data
    
    suspend fun createAccount(request: CreateAccountRequest) {
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
    suspend fun getCategories(groupId: Int) = api.getCategories(groupId).data
    
    suspend fun createCategory(request: CreateCategoryRequest) {
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
    suspend fun getTransactions(groupId: Int) = api.getTransactions(groupId).data
    
    suspend fun getSummary(groupId: Int, startDate: String, endDate: String) = api.getSummary(groupId, startDate, endDate)
    
    suspend fun createTransaction(request: CreateTransactionRequest) {
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
}
