package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SpendWiseViewModel(
    application: Application,
    private val repository: ExpenseRepository,
    private val geminiRepository: GeminiRepository
) : AndroidViewModel(application) {

    // Theme Mode
    private val _themeMode = MutableStateFlow("DARK") // DARK is fintech premium default
    val themeMode: StateFlow<String> = _themeMode

    // Currency Selection
    private val _currency = MutableStateFlow("INR") // INR, USD, EUR, GBP
    val currency: StateFlow<String> = _currency

    // Navigation and Session
    private val _currentScreen = MutableStateFlow("splash")
    val currentScreen: StateFlow<String> = _currentScreen

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // Expense & Budget states
    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpenses: StateFlow<List<Expense>> = _allExpenses

    private val _categoryBudgets = MutableStateFlow<List<CategoryBudget>>(emptyList())
    val categoryBudgets: StateFlow<List<CategoryBudget>> = _categoryBudgets

    val syncStatus: StateFlow<SyncStatus> = repository.syncStatus

    // AI insights states
    private val _aiInsights = MutableStateFlow<String>("")
    val aiInsights: StateFlow<String> = _aiInsights

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    // Edit target
    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense: StateFlow<Expense?> = _editingExpense

    // Local lists for quick stats
    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            val user = repository.getActiveUser()
            _currentUser.value = user
            if (user != null) {
                _currency.value = user.currency
                _themeMode.value = user.themeMode
                // If user id is standard registered
                if (user.id != "uid_demo_user_unregistered_yet") {
                    _isUserLoggedIn.value = true
                }
                loadUserData(user.id)
            }
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            // Collect expenses
            repository.getExpensesFlow(userId).collect { list ->
                _allExpenses.value = list
            }
        }
        viewModelScope.launch {
            // Collect budgets
            repository.getCategoryBudgetsFlow(userId).collect { list ->
                _categoryBudgets.value = list
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // Authentication Actions
    fun loginUser(email: String, name: String) {
        viewModelScope.launch {
            val secureId = "user_" + UUID.randomUUID().toString().take(6)
            val loggedInUser = User(
                id = secureId,
                name = name.ifEmpty { "Aria Richards" },
                email = email.ifEmpty { "aria@spendwise.com" },
                monthlyBudget = 50000.0,
                savingsGoal = 10000.0,
                currency = _currency.value,
                themeMode = _themeMode.value,
                healthScore = 90,
                streakDays = 1
            )
            repository.updateCurrentUser(loggedInUser)
            _currentUser.value = loggedInUser
            _isUserLoggedIn.value = true
            loadUserData(secureId)
            _currentScreen.value = "dashboard"
        }
    }

    fun registerUser(name: String, email: String) {
        loginUser(email, name)
    }

    fun logoutUser() {
        viewModelScope.launch {
            _isUserLoggedIn.value = false
            _currentUser.value = null
            _allExpenses.value = emptyList()
            _categoryBudgets.value = emptyList()
            _currentScreen.value = "login"
        }
    }

    // Manage settings
    fun updateCurrency(newCurrency: String) {
        _currency.value = newCurrency
        viewModelScope.launch {
            _currentUser.value?.let {
                val updated = it.copy(currency = newCurrency)
                repository.updateCurrentUser(updated)
                _currentUser.value = updated
            }
        }
    }

    fun updateThemeMode(mode: String) {
        _themeMode.value = mode
        viewModelScope.launch {
            _currentUser.value?.let {
                val updated = it.copy(themeMode = mode)
                repository.updateCurrentUser(updated)
                _currentUser.value = updated
            }
        }
    }

    fun updateBudgets(monthlyLimit: Double, savingsGoal: Double) {
        viewModelScope.launch {
            _currentUser.value?.let {
                val updated = it.copy(monthlyBudget = monthlyLimit, savingsGoal = savingsGoal)
                repository.updateCurrentUser(updated)
                _currentUser.value = updated
            }
        }
    }

    // Expense Actions
    fun addExpense(amount: Double, category: String, note: String, date: Long, paymentMethod: String, receiptImage: String? = null) {
        val userId = _currentUser.value?.id ?: "uid_demo_user"
        val expense = Expense(
            id = UUID.randomUUID().toString(),
            userId = userId,
            amount = amount,
            category = category,
            note = note,
            date = date,
            paymentMethod = paymentMethod,
            receiptImage = receiptImage
        )
        viewModelScope.launch {
            repository.insertExpense(expense)
            // Auto increment usage streak days casually
            _currentUser.value?.let { user ->
                repository.updateCurrentUser(user.copy(streakDays = user.streakDays + 1))
            }
        }
    }

    fun startEditingExpense(expense: Expense) {
        _editingExpense.value = expense
        navigateTo("add_expense")
    }

    fun updateExpense(id: String, amount: Double, category: String, note: String, date: Long, paymentMethod: String, receiptImage: String?) {
        val userId = _currentUser.value?.id ?: "uid_demo_user"
        val expense = Expense(
            id = id,
            userId = userId,
            amount = amount,
            category = category,
            note = note,
            date = date,
            paymentMethod = paymentMethod,
            receiptImage = receiptImage
        )
        viewModelScope.launch {
            repository.updateExpense(expense)
            _editingExpense.value = null
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    // Category budgets
    fun setCategoryBudget(category: String, limit: Double) {
        val userId = _currentUser.value?.id ?: "uid_demo_user"
        val budget = CategoryBudget(
            id = "${userId}_${category}",
            userId = userId,
            category = category,
            limitAmount = limit
        )
        viewModelScope.launch {
            repository.insertCategoryBudget(budget)
        }
    }

    fun removeCategoryBudget(id: String) {
        viewModelScope.launch {
            repository.deleteCategoryBudget(id)
        }
    }

    // AI Analysis calls
    fun getFinancialInsights() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val user = _currentUser.value
            val budget = user?.monthlyBudget ?: 50000.0
            val savings = user?.savingsGoal ?: 10000.0
            val insightsText = geminiRepository.generateFinancialInsights(_allExpenses.value, budget, savings)
            _aiInsights.value = insightsText
            _isAiLoading.value = false
        }
    }

    // Extract Voice Input via Gemini
    fun processVoiceMessage(speechText: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val parsedResult = geminiRepository.parseVoiceInput(speechText)
            _isAiLoading.value = false
            if (parsedResult != null && parsedResult.amount > 0) {
                addExpense(
                    amount = parsedResult.amount,
                    category = parsedResult.category,
                    note = parsedResult.note,
                    date = System.currentTimeMillis(),
                    paymentMethod = "Cash"
                )
                onComplete(true, "Add complete: ₹${parsedResult.amount} spent on ${parsedResult.note} in ${parsedResult.category}")
            } else {
                onComplete(false, "Could not understand structural figures. Please specify amount and item (e.g. 'Spent 600 rupees on Netflix subscription')")
            }
        }
    }
}

// Custom Factory for instantiation in Compose
class SpendWiseViewModelFactory(
    private val application: Application,
    private val repository: ExpenseRepository,
    private val geminiRepository: GeminiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpendWiseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpendWiseViewModel(application, repository, geminiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
