package com.hrach.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(private val repository: FinanceRepository) : ViewModel() {
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

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val groupsData = repository.getGroups()
                _groups.value = groupsData
                val firstGroupId = groupsData.firstOrNull()?.id
                if (_selectedGroupId.value == null && firstGroupId != null) {
                    _selectedGroupId.value = firstGroupId
                    loadGroupData(firstGroupId)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки групп"
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectGroup(groupId: Int) {
        _selectedGroupId.value = groupId
        loadGroupData(groupId)
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
                _error.value = e.message ?: "Ошибка загрузки данных"
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
                        createdBy = 1,
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
                _error.value = e.message ?: "Ошибка добавления операции"
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
                        createdBy = item.createdBy,
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
                _error.value = e.message ?: "Ошибка обновления операции"
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
                _error.value = e.message ?: "Ошибка удаления операции"
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
                repository.createAccount(CreateAccountRequest(groupId, 1, name, type, "RUB", initialBalance, shared))
                loadGroupData(groupId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка создания счета"
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
                        userId = account.userId,
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
                _error.value = e.message ?: "Ошибка изменения счета"
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
                _error.value = e.message ?: "Ошибка удаления счета"
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
                _error.value = e.message ?: "Ошибка создания категории"
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
                _error.value = e.message ?: "Ошибка обновления категории"
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
                _error.value = e.message ?: "Ошибка удаления категории"
            } finally {
                _loading.value = false
            }
        }
    }
}
