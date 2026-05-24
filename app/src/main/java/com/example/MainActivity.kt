package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ExpenseRepository
import com.example.data.GeminiRepository
import com.example.ui.SpendWiseApp
import com.example.ui.SpendWiseViewModel
import com.example.ui.SpendWiseViewModelFactory
import com.example.ui.theme.SpendWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize the Room database singleton offline architecture
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ExpenseRepository(database.expenseDao)
        val geminiRepository = GeminiRepository()

        // 2. Load the Viewmodel via custom Factory injecting repositories
        val factory = SpendWiseViewModelFactory(application, repository, geminiRepository)
        val viewModel = ViewModelProvider(this, factory)[SpendWiseViewModel::class.java]

        // 3. Render Content inside Custom Dynamic Themes
        setContent {
            val activeThemeMode by viewModel.themeMode.collectAsState()

            SpendWiseTheme(themeMode = activeThemeMode) {
                SpendWiseApp(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
