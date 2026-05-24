package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpendWiseViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReceiptScannerScreen(
    viewModel: SpendWiseViewModel,
    modifier: Modifier = Modifier
) {
    var isScanning by remember { mutableStateOf(false) }
    var scannedResultText by remember { mutableStateOf<String?>(null) }
    var parsedAmount by remember { mutableDoubleStateOf(0.0) }
    var parsedCategory by remember { mutableStateOf("Food") }
    var parsedNote by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Mock receipts that are extremely high-fidelity
    val mockReceipts = listOf(
        Triple("Starbucks Coffee Bill - ₹420.00", 420.00, "Food"),
        Triple("Uber Trip Ticket - ₹750.00", 750.00, "Travel"),
        Triple("Flipkart Shopping Invoice - ₹2,400.00", 2400.00, "Shopping"),
        Triple("Tata Power Electricity - ₹4,800.00", 4800.00, "Bills")
    )

    // Scanning laser bar vertical translation animation
    val infiniteTransition = rememberInfiniteTransition()
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = "OCR Receipt Scanner",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Scanner viewfinder box showing laser scrolling
            item {
                Text(
                    text = "Receipt Camera Viewfinder",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            // Video / scanning frame laser simulation grid line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .offset(y = laserOffset.dp)
                                    .background(NeonGreen)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = BrandEmerald)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Analyzing receipt characters via ML Kit OCR...",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Choose a bill receipt card below to scan",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Seed Choice Receipts Header
            item {
                Text(
                    text = "Available Receipts to Demo",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Display interactive list of receipts
            items(mockReceipts) { receipt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isScanning) {
                            coroutineScope.launch {
                                isScanning = true
                                delay(2200) // Latency animation simulation
                                isScanning = false
                                scannedResultText = "Successfully extracted text!"
                                parsedAmount = receipt.second
                                parsedCategory = receipt.third
                                parsedNote = receipt.first.substringBefore(" -")
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = BrandBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = receipt.first,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Scan", tint = BrandEmerald)
                    }
                }
            }

            // Recognition Result details and Action button
            scannedResultText?.let {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandEmerald.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Extracted Expense Proposal",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                            Text(
                                text = "• Description: $parsedNote",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "• Extracted Cost: ₹$parsedAmount",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Auto Category: $parsedCategory",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    viewModel.addExpense(
                                        amount = parsedAmount,
                                        category = parsedCategory,
                                        note = parsedNote,
                                        date = System.currentTimeMillis(),
                                        paymentMethod = "UPI"
                                    )
                                    scannedResultText = null
                                    viewModel.navigateTo("dashboard")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Add Extracted Expense", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceInputScreen(
    viewModel: SpendWiseViewModel,
    modifier: Modifier = Modifier
) {
    var voiceTextPhrase by remember { mutableStateOf("") }
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    var extractionProgressText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Rotating pulse animation for speaking waveform helper
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = "Voice Expense Input",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Audio Pulsing Microphone Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isAiLoading) {
                            CircularProgressIndicator(color = BrandAmber)
                        } else {
                            // Pulsing decorative waveform ring
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .rotate(pulseScale * 20f)
                                    .border(
                                        width = 3.dp,
                                        color = BrandAmber.copy(alpha = (1.5f - pulseScale).coerceIn(0f, 1f)),
                                        shape = CircleShape
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(BrandAmber)
                                    .clickable {
                                        // Auto fill demo phrase inside voice fields
                                        voiceTextPhrase = "Spent 650 rupees on delicious lunch with friends"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }

            // Text input phrase for typing/demonstrating spoken lines
            item {
                OutlinedTextField(
                    value = voiceTextPhrase,
                    onValueChange = { voiceTextPhrase = it },
                    label = { Text("What did you spend today?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("e.g. 'Spent 1500 on Shopping fuel petrol'", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandAmber,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {
                        if (voiceTextPhrase.isNotEmpty()) {
                            viewModel.processVoiceMessage(voiceTextPhrase) { success, msg ->
                                extractionProgressText = msg
                                if (success) {
                                    voiceTextPhrase = ""
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAmber),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Parse and Logging Entry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Feedback status card
            if (extractionProgressText.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = BrandAmber)
                            Text(
                                text = extractionProgressText,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSettingsScreen(
    viewModel: SpendWiseViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = "Profile & Settings",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // User Info details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(BrandBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(40.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user?.name ?: "Aria Richards",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = user?.email ?: "aria.richards@spendwise.com",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Currency selection item
            item {
                Text(
                    text = "Currency Configuration",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("INR", "USD", "EUR", "GBP").forEach { curr ->
                        val isSelected = currency == curr
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updateCurrency(curr) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandEmerald else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = curr,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Theme selections item
            item {
                Text(
                    text = "Application Theme Preferences",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DARK", "LIGHT", "SYSTEM").forEach { mode ->
                        val isSelected = themeMode == mode
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updateThemeMode(mode) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // CSV Exporter buttons
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Download & Exports",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Extract and backup all transactional records to sharing-ready CSV files locally.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        Button(
                            onClick = {
                                if (expenses.isEmpty()) {
                                    Toast.makeText(context, "No expense items found to export!", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Generate sharing ready CSV string
                                    val csvContent = StringBuilder("ID,Amount,Category,note,Date,PaymentMethod\n")
                                    expenses.forEach {
                                        csvContent.append("${it.id},${it.amount},${it.category},${it.note},${it.date},${it.paymentMethod}\n")
                                    }
                                    Toast.makeText(context, "CSV exported: ${expenses.size} entries backed up successfully!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Export to CSV file", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Log Out Button
            item {
                Button(
                    onClick = { viewModel.logoutUser() },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("De-authorize & Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
