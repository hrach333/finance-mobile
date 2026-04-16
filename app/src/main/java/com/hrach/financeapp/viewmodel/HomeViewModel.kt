package com.hrach.financeapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hrach.financeapp.data.api.ApiClient
import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.AIChatRequest
import com.hrach.financeapp.data.dto.ApiErrorResponse
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.ChatMessage
import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.GroupMemberDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.network.AIPromptBuilder
import com.hrach.financeapp.data.network.NetworkMonitor
import com.hrach.financeapp.data.offline.OfflineManager
import com.hrach.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate

class HomeViewModel(
    private val repository: FinanceRepository,
    private val offlineManager: OfflineManager? = null,
    private val networkMonitor: NetworkMonitor? = null
) : ViewModel() {
    private val gson = Gson()

    private val _groups = MutableStateFlow<List<GroupDto>>(emptyList())
    val groups: StateFlow<List<GroupDto>> = _groups.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<Int?>(null)
    val selectedGroupId: StateFlow<Int?> = _selectedGroupId.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountDto>>(emptyList())
    val accounts: StateFlow<List<AccountDto>> = _accounts.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionDto>>(emptyList())
    val transactions: StateFlow<List<TransactionDto>> = _transactions.asStateFlow()

    private val _summary = MutableStateFlow<SummaryDto?>(null)
    val summary: StateFlow<SummaryDto?> = _summary.asStateFlow()

    private val _members = MutableStateFlow<List<GroupMemberDto>>(emptyList())
    val members: StateFlow<List<GroupMemberDto>> = _members.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    // AI советник
    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // Офлайн синхронизация
    val isSyncing: StateFlow<Boolean> = offlineManager?.isSyncing ?: MutableStateFlow(false).asStateFlow()
    val pendingCount: StateFlow<Int> = offlineManager?.pendingCount ?: MutableStateFlow(0).asStateFlow()
    val syncError: StateFlow<String?> = offlineManager?.syncError ?: MutableStateFlow(null).asStateFlow()
    val pendingOfflineOperations: Flow<List<com.hrach.financeapp.data.db.entity.PendingOperationEntity>> =
        offlineManager?.pendingOperations ?: emptyFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var currentUserId: Int? = null
    private var pollingJob: Job? = null

    init {
        networkMonitor?.let { monitor ->
            viewModelScope.launch {
                monitor.isOnline.collect { isOnline ->
                    _isOnline.value = isOnline
                }
            }
        }
    }

    fun onAuthenticated(userId: Int) {
        currentUserId = userId
        loadGroups()
    }

    fun onLoggedOut() {
        stopPolling()
        currentUserId = null
        _groups.value = emptyList()
        _selectedGroupId.value = null
        _accounts.value = emptyList()
        _categories.value = emptyList()
        _transactions.value = emptyList()
        _summary.value = null
        _members.value = emptyList()
        _error.value = null
        _sessionExpired.value = false
        _aiAdvice.value = null
        _aiError.value = null
    }

    fun loadGroups() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val groupsData = repository.getGroups()
                _groups.value = groupsData
                val currentSelected = _selectedGroupId.value
                val validSelected = groupsData.firstOrNull { it.id == currentSelected }?.id
                val firstGroupId = validSelected ?: groupsData.firstOrNull()?.id
                _selectedGroupId.value = firstGroupId
                if (firstGroupId != null) {
                    loadGroupData(firstGroupId)
                    loadMembers(firstGroupId)
                } else {
                    _accounts.value = emptyList()
                    _categories.value = emptyList()
                    _transactions.value = emptyList()
                    _summary.value = null
                    _members.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectGroup(groupId: Int) {
        _selectedGroupId.value = groupId
        loadGroupData(groupId)
        loadMembers(groupId)
    }

    fun createGroup(name: String, baseCurrency: String = "RUB") {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val group = repository.createGroup(name.trim(), baseCurrency)
                _selectedGroupId.value = group.id
                loadGroups()
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateGroup(name: String, baseCurrency: String = "RUB") {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.updateGroup(groupId, name.trim(), baseCurrency)
                loadGroups()
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadGroupData(groupId: Int? = _selectedGroupId.value) {
        val id = groupId ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _accounts.value = repository.getAccounts(id)
                _categories.value = repository.getCategories(id)
                _transactions.value = repository.getTransactions(id)
                val now = LocalDate.now()
                val startDate = now.withDayOfMonth(1).toString()
                val endDate = now.withDayOfMonth(now.lengthOfMonth()).toString()
                _summary.value = repository.getSummary(id, startDate, endDate)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Ошибка загрузки данных группы: ${e.message}")
                _error.value = parseException(e)
                // В офлайн режиме - не показываем ошибку, просто используем кэшированные данные
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMembers(groupId: Int? = _selectedGroupId.value) {
        val id = groupId ?: return
        viewModelScope.launch {
            try {
                _members.value = repository.getGroupMembers(id)
            } catch (e: Exception) {
                _error.value = parseException(e)
            }
        }
    }

    fun addMember(email: String, role: String = "member") {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.addGroupMember(groupId, email, role)
                loadMembers(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateMemberRole(memberId: Int, role: String) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.updateGroupMember(groupId, memberId, role)
                loadMembers(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteMember(memberId: Int) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.deleteGroupMember(groupId, memberId)
                loadMembers(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun refreshAll() {
        val groupId = _selectedGroupId.value
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val groupsData = repository.getGroups()
                _groups.value = groupsData

                val validSelected = groupsData.firstOrNull { it.id == groupId }?.id
                val activeGroupId = validSelected ?: groupsData.firstOrNull()?.id
                _selectedGroupId.value = activeGroupId

                if (activeGroupId != null) {
                    _accounts.value = repository.getAccounts(activeGroupId)
                    _categories.value = repository.getCategories(activeGroupId)
                    _transactions.value = repository.getTransactions(activeGroupId)
                    _members.value = repository.getGroupMembers(activeGroupId)

                    val now = LocalDate.now()
                    val startDate = now.withDayOfMonth(1).toString()
                    val endDate = now.withDayOfMonth(now.lengthOfMonth()).toString()
                    _summary.value = repository.getSummary(activeGroupId, startDate, endDate)
                } else {
                    _accounts.value = emptyList()
                    _categories.value = emptyList()
                    _transactions.value = emptyList()
                    _summary.value = null
                    _members.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun startPolling(intervalMs: Long = 15000L) {
        val groupId = _selectedGroupId.value ?: return
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    _accounts.value = repository.getAccounts(groupId)
                    _categories.value = repository.getCategories(groupId)
                    _transactions.value = repository.getTransactions(groupId)
                    _members.value = repository.getGroupMembers(groupId)

                    val now = LocalDate.now()
                    val startDate = now.withDayOfMonth(1).toString()
                    val endDate = now.withDayOfMonth(now.lengthOfMonth()).toString()
                    _summary.value = repository.getSummary(groupId, startDate, endDate)
                } catch (e: Exception) {
                    _error.value = parseException(e)
                }
                delay(intervalMs)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun accountName(accountId: Int): String? = _accounts.value.firstOrNull { it.id == accountId }?.name
    fun categoryName(categoryId: Int?): String? = _categories.value.firstOrNull { it.id == categoryId }?.name

    fun createTransaction(type: String, amount: Double, accountId: Int, categoryId: Int, comment: String) {
        val groupId = _selectedGroupId.value ?: return
        
        // Базовые проверки
        if (accountId <= 0) {
            _error.value = "Не выбран счёт"
            return
        }

        if ((type == "INCOME" || type == "EXPENSE") && categoryId <= 0) {
            _error.value = "Не выбрана категория"
            return
        }
        
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Пытаемся получить валюту счета, если счета загружены
                val account = _accounts.value.firstOrNull { it.id == accountId }
                val currency = account?.currency ?: "RUB"  // Если счета нет - используем RUB
                
                repository.createTransaction(
                    CreateTransactionRequest(
                        groupId = groupId,
                        accountId = accountId,
                        createdBy = null,
                        type = type,
                        amount = amount,
                        currency = currency,
                        categoryId = categoryId,
                        transactionDate = LocalDate.now().toString(),
                        comment = comment
                    )
                )
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateTransaction(item: TransactionDto, type: String, amount: Double, accountId: Int, categoryId: Int, comment: String) {
        val groupId = _selectedGroupId.value ?: return
        val account = _accounts.value.firstOrNull { it.id == accountId } ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.updateTransaction(
                    item.id,
                    UpdateTransactionRequest(
                        groupId = groupId,
                        accountId = accountId,
                        createdBy = null,
                        type = type,
                        amount = amount,
                        currency = account.currency,
                        categoryId = categoryId,
                        transactionDate = item.transactionDate,
                        comment = comment
                    )
                )
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteTransaction(id: Int) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.deleteTransaction(id)
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun createAccount(name: String, type: String, initialBalance: Double, shared: Boolean = true) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.createAccount(CreateAccountRequest(groupId, null, name.trim(), type, "RUB", initialBalance, shared))
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateAccount(account: AccountDto, name: String, type: String, initialBalance: Double, shared: Boolean = true, active: Boolean = true) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.updateAccount(
                    account.id,
                    UpdateAccountRequest(
                        groupId = groupId,
                        userId = account.userId ?: currentUserId,
                        name = name,
                        type = type,
                        currency = account.currency,
                        initialBalance = initialBalance,
                        currentBalance = initialBalance,
                        shared = shared,
                        isActive = active
                    )
                )
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteAccount(id: Int) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.deleteAccount(id)
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun createCategory(name: String, type: String, iconKey: String? = null) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.createCategory(CreateCategoryRequest(groupId, type, name.trim(), iconKey))
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateCategory(category: CategoryDto, name: String, type: String, iconKey: String? = null) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.updateCategory(category.id, name, type, iconKey)
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteCategory(id: Int) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.deleteCategory(id)
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSessionExpired() {
        _sessionExpired.value = false
    }

    /**
     * Ручная синхронизация ожидающих операций
     */
    fun manualSync() {
        viewModelScope.launch {
            offlineManager?.syncPendingOperations()
        }
    }

    /**
     * Удалить операцию из очереди
     */
    fun deleteOfflineOperation(operationId: Long) {
        viewModelScope.launch {
            offlineManager?.deleteOperation(operationId)
        }
    }

    /**
     * Повторить неудачную операцию
     */
    fun retryOfflineOperation(operationId: Long) {
        viewModelScope.launch {
            offlineManager?.retryFailedOperation(operationId)
            offlineManager?.syncPendingOperations()
        }
    }

    /**
     * Получить финансовый совет от ИИ
     */
    fun getFinanceAdvice() {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            _aiAdvice.value = null
            try {
                val prompt = AIPromptBuilder.buildFinanceAdvicePrompt(
                    transactions = _transactions.value,
                    categories = _categories.value,
                    summary = _summary.value
                )

                val request = AIChatRequest(
                    messages = listOf(
                        ChatMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.7,
                    max_tokens = 1000
                )

                val response = ApiClient.aiService.getChatCompletion(request)
                val advice = response.choices.firstOrNull()?.message?.content
                
                if (advice != null) {
                    _aiAdvice.value = advice
                } else {
                    _aiError.value = "Не удалось получить ответ от ИИ"
                }
            } catch (e: Exception) {
                Log.e("AI_ADVICE", "Ошибка при запросе к ИИ", e)
                _aiError.value = when {
                    e.message?.contains("Connection refused") == true -> 
                        "ИИ модель недоступна. Убедитесь, что LM запущен на http://127.0.0.1:1234"
                    e.message?.contains("Failed to connect") == true ->
                        "Не удалось подключиться к ИИ модели"
                    else -> "Ошибка: ${e.message ?: "Неизвестная ошибка"}"
                }
            } finally {
                _aiLoading.value = false
            }
        }
    }

    /**
     * Очистить совет от ИИ
     */
    fun clearAIAdvice() {
        _aiAdvice.value = null
        _aiError.value = null
    }

    private fun parseException(e: Exception): String {
        if (e is HttpException) {
            val body = e.response()?.errorBody()?.string()
            Log.e("HomeViewModel", "HTTP Error ${e.code()}: $body")
            
            val errorText = parseError(body)
            if (errorText.isNotBlank()) {
                Log.d("HomeViewModel", "Errors from backend:\n$errorText")
                return errorText
            }
            
            return when (e.code()) {
                401 -> {
                    _sessionExpired.value = true
                    "Сессия истекла. Войди снова"
                }
                403 -> "Нет доступа к группе"
                422 -> "Ошибка валидации: проверьте введенные данные"
                else -> "Ошибка запроса: ${e.code()}"
            }
        }
        return e.message ?: "Неизвестная ошибка"
    }
    
    private fun parseError(json: String?): String {
        return try {
            val parsed = gson.fromJson(json, ApiErrorResponse::class.java)
            Log.d("HomeViewModel", "Parsed response: errors=${parsed.errors}, message=${parsed.message}")
            
            parsed.errors
                ?.let { errors ->
                    when (errors) {
                        is Map<*, *> -> {
                            @Suppress("UNCHECKED_CAST")
                            val errorsMap = errors as? Map<String, List<String>> ?: return ""
                            errorsMap.entries
                                .map { (field, messages) ->
                                    val translatedMessages = messages
                                        .map { mapError(it) }
                                    val fieldName = when (field) {
                                        "name" -> "Имя"
                                        "email" -> "Email"
                                        "password" -> "Пароль"
                                        "password_confirmation" -> "Подтверждение пароля"
                                        else -> field.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    }
                                    "$fieldName: ${translatedMessages.joinToString(", ")}"
                                }
                                .joinToString("\n")
                        }
                        is List<*> -> {
                            errors.filterIsInstance<String>()
                                .map { mapError(it) }
                                .joinToString("\n")
                        }
                        else -> ""
                    }
                }
                ?: parsed.message
                ?: "Ошибка запроса"
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to parse error: ${e.message}, json=$json")
            "Ошибка запроса"
        }
    }
    
    private fun mapError(message: String): String {
        return when {
            message.contains("email has already been taken", ignoreCase = true) ->
                "Пользователь с таким email уже существует"

            message.contains("password must be at least", ignoreCase = true) ->
                "Пароль должен быть не менее 8 символов"

            message.contains("password confirmation does not match", ignoreCase = true) ->
                "Пароли не совпадают"

            message.contains("email must be a valid email", ignoreCase = true) ->
                "Некорректный email"
            
            message.contains("validation.unique", ignoreCase = true) ->
                "Это значение уже занято"
            
            message.contains("validation.required", ignoreCase = true) ->
                "Это поле обязательно"
            
            message.contains("validation.min.string", ignoreCase = true) ->
                "Значение слишком короткое"
            
            message.contains("validation.min.numeric", ignoreCase = true) ->
                "Значение должно быть больше"
            
            message.contains("validation.confirmed", ignoreCase = true) ->
                "Поле подтверждения не совпадает"
            
            message.contains("validation.email", ignoreCase = true) ->
                "Email должен быть корректным"

            else -> message
        }
    }
}
