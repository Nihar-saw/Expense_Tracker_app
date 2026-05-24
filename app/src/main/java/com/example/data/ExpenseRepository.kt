package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class SyncStatus {
    IDLE, SYNCING, SYNCED, ERROR
}

class ExpenseRepository(private val dao: ExpenseDao) {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    // Simulated cloud data store for demonstrating Firebase Firestore Sync
    private val cloudFirestoreSimulatedStore = mutableListOf<Expense>()

    val userFlow: Flow<User?> = dao.getUserFlow()

    fun getExpensesFlow(userId: String): Flow<List<Expense>> = dao.getExpensesByUserIdFlow(userId)

    fun getCategoryBudgetsFlow(userId: String): Flow<List<CategoryBudget>> = dao.getCategoryBudgetsFlow(userId)

    suspend fun getActiveUser(): User? {
        val user = dao.getUserSync()
        if (user == null) {
            // Seed a default mock active user if database is completely empty
            // to show an immediate premium experience.
            val defaultUser = User(
                id = "uid_demo_user",
                name = "Aria Richards",
                email = "aria.richards@spendwise.com",
                monthlyBudget = 45000.0,
                savingsGoal = 15000.0,
                currency = "INR",
                themeMode = "DARK",
                healthScore = 88,
                streakDays = 5
            )
            dao.insertUser(defaultUser)
            return defaultUser
        }
        return user
    }

    suspend fun updateCurrentUser(user: User) {
        dao.insertUser(user)
        triggerCloudSync()
    }

    suspend fun insertExpense(expense: Expense) {
        dao.insertExpense(expense)
        triggerCloudSync()
    }

    suspend fun updateExpense(expense: Expense) {
        dao.insertExpense(expense) // Room Insert REPLACE acts as update
        triggerCloudSync()
    }

    suspend fun deleteExpenseById(id: String) {
        dao.deleteExpenseById(id)
        triggerCloudSync()
    }

    suspend fun insertCategoryBudget(budget: CategoryBudget) {
        dao.insertCategoryBudget(budget)
        triggerCloudSync()
    }

    suspend fun deleteCategoryBudget(id: String) {
        dao.deleteCategoryBudgetById(id)
        triggerCloudSync()
    }

    // High fidelity offline-first background synchronization loop simulating Firebase Firestore Sync
    private fun triggerCloudSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _syncStatus.value = SyncStatus.SYNCING
                delay(1200) // Simulate network roundtrip latency with Firebase Firestore

                // Fetch local records
                val localUser = dao.getUserSync()
                val currentLocalExpenses = dao.getAllExpensesFlow().firstOrNull() ?: emptyList()

                // Synchronize local with simulated cloud Storage
                cloudFirestoreSimulatedStore.clear()
                cloudFirestoreSimulatedStore.addAll(currentLocalExpenses)

                // Optional calculation of updated health index based on savings & budget
                if (localUser != null) {
                    val totalMonthSpending = currentLocalExpenses.filter {
                        // Current month simple timestamp filter approximate
                        it.date > System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
                    }.sumOf { it.amount }

                    val percentSpendingOfBudget = if (localUser.monthlyBudget > 0) {
                        (totalMonthSpending / localUser.monthlyBudget) * 100
                    } else 0.0

                    // Math for health index
                    val health = when {
                        percentSpendingOfBudget > 100 -> 35 // Overspent
                        percentSpendingOfBudget > 90 -> 55 // Alert
                        percentSpendingOfBudget > 75 -> 78 // Warning
                        percentSpendingOfBudget > 50 -> 88 // Steady
                        else -> 94 // Elegant
                    }
                    if (health != localUser.healthScore) {
                        dao.insertUser(localUser.copy(healthScore = health))
                    }
                }

                _syncStatus.value = SyncStatus.SYNCED
                delay(1500)
                _syncStatus.value = SyncStatus.IDLE
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR
            }
        }
    }
}
