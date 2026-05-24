package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.*

@Composable
fun SpendWiseApp(
    viewModel: SpendWiseViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    val navigationBarVisible = currentScreen !in listOf("splash", "onboarding", "login")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D10))
            .drawBehind {
                // Top-Left ambient blue glow consistent with tailwind w-[60%] blur-[100px] bg-[#3D5AFE]/20
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x333D5AFE), Color.Transparent),
                        radius = size.width * 0.9f
                    ),
                    center = Offset(-size.width * 0.05f, -size.height * 0.05f)
                )
                // Mid-Right ambient green glow consistent with tailwind bg-[#00E676]/10 blur-[100px]
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1800E676), Color.Transparent),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 1.05f, size.height * 0.35f)
                )
            }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Ensure background glows show through the transparent container
            bottomBar = {
                if (navigationBarVisible) {
                    NavigationBar(
                        containerColor = Color(0xCC161920), // Frosted dark theme tab bar (80% opacity navigation container)
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == "dashboard",
                            onClick = { viewModel.navigateTo("dashboard") },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Overview", fontWeight = FontWeight.SemiBold) }
                        )

                        NavigationBarItem(
                            selected = currentScreen == "history",
                            onClick = { viewModel.navigateTo("history") },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Ledgers") },
                            label = { Text("Ledgers", fontWeight = FontWeight.SemiBold) }
                        )

                        NavigationBarItem(
                            selected = currentScreen == "analytics",
                            onClick = { viewModel.navigateTo("analytics") },
                            icon = { Icon(Icons.Default.PieChart, contentDescription = "Charts") },
                            label = { Text("Analytics", fontWeight = FontWeight.SemiBold) }
                        )

                        NavigationBarItem(
                            selected = currentScreen == "budget",
                            onClick = { viewModel.navigateTo("budget") },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Budgets") },
                            label = { Text("Budgets", fontWeight = FontWeight.SemiBold) }
                        )

                        NavigationBarItem(
                            selected = currentScreen == "insights",
                            onClick = { viewModel.navigateTo("insights") },
                            icon = { Icon(Icons.Default.Psychology, contentDescription = "Insights") },
                            label = { Text("AI Insights", fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (navigationBarVisible && currentScreen != "add_expense") {
                    FloatingActionButton(
                        onClick = {
                            // Clear optional editing target before launching add screen
                            viewModel.startEditingExpense(
                                com.example.data.Expense(
                                    id = "", userId = "", amount = 0.0, category = "Food", note = "", date = 0L, paymentMethod = ""
                                )
                            )
                            viewModel.navigateTo("add_expense") 
                        },
                        containerColor = BrandEmerald,
                        contentColor = Color(0xFF0B0D10)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Log Expense", modifier = Modifier.size(28.dp))
                    }
                }
            }
        ) { innerPadding ->
            // Animated transition overlay container for premium slide shifts
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "splash" -> SplashScreen(viewModel = viewModel)
                    "onboarding" -> OnboardingScreen(viewModel = viewModel)
                    "login" -> LoginRegisterScreen(viewModel = viewModel)
                    "dashboard" -> DashboardScreen(viewModel = viewModel)
                    "add_expense" -> AddExpenseScreen(viewModel = viewModel)
                    "history" -> ExpenseHistoryScreen(viewModel = viewModel)
                    "analytics" -> AnalyticsScreen(viewModel = viewModel)
                    "budget" -> BudgetScreen(viewModel = viewModel)
                    "insights" -> AiInsightsScreen(viewModel = viewModel)
                    "scanner" -> ReceiptScannerScreen(viewModel = viewModel)
                    "voice" -> VoiceInputScreen(viewModel = viewModel)
                    "profile" -> ProfileSettingsScreen(viewModel = viewModel)
                    else -> DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}

