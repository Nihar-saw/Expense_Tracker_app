package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val monthlyBudget: Double = 50000.0,
    val savingsGoal: Double = 10000.0,
    val currency: String = "INR", // DEFAULT CURRENCY (e.g., INR, USD, EUR, GBP)
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val healthScore: Int = 85,
    val streakDays: Int = 1
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long, // Epoch millis
    val paymentMethod: String, // "Cash", "Card", "UPI", "Net Banking"
    val receiptImage: String? = null // local file path or uri, or text summary
)

@Entity(tableName = "category_budgets")
data class CategoryBudget(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String,
    val limitAmount: Double
)
