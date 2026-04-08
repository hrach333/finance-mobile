package com.hrach.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.ApiErrorResponse
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.GroupMemberDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate

class HomeViewModel(private val repository: FinanceRepository) : ViewModel() {
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

    private var currentUserId: Int? = null

    fun onAuthenticated(userId: Int) {
        currentUserId = userId
        loadGroups()
    }

    fun onLoggedOut() {
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
                _error.value = parseException(e)
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

    fun accountName(accountId: Int): String? = _accounts.value.firstOrNull { it.id == accountId }?.name
    fun categoryName(categoryId: Int?): String? = _categories.value.firstOrNull { it.id == categoryId }?.name

    fun createTransaction(type: String, amount: Double, accountId: Int, categoryId: Int, comment: String) {
        val groupId = _selectedGroupId.value ?: return
        val account = _accounts.value.firstOrNull { it.id == accountId } ?: return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.createTransaction(
                    CreateTransactionRequest(
                        groupId = groupId,
                        accountId = accountId,
                        createdBy = currentUserId,
                        type = type,
                        amount = amount,
                        currency = account.currency,
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
                        createdBy = item.createdBy ?: currentUserId,
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
                repository.createAccount(CreateAccountRequest(groupId, currentUserId, name, type, "RUB", initialBalance, shared))
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
                repository.createCategory(CreateCategoryRequest(groupId, type, name, iconKey))
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

    private fun parseException(e: Exception): String {
        if (e is HttpException) {
            val body = e.response()?.errorBody()?.string()
            val parsed = runCatching { gson.fromJson(body, ApiErrorResponse::class.java) }.getOrNull()
            if (!parsed?.errors.isNullOrEmpty()) {
                return parsed?.errors?.values?.flatten()?.joinToString("\n") ?: "Ошибка запроса"
            }
            return when (e.code()) {
                401 -> {
                    _sessionExpired.value = true
                    "Сессия истекла. Войди снова"
                }
                403 -> "Нет доступа к группе"
                422 -> parsed?.message ?: "Ошибка валидации"
                else -> parsed?.message ?: "Ошибка запроса: ${e.code()}"
            }
        }
        return e.message ?: "Неизвестная ошибка"
    }
}
