package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class VoiceParseResult(
    val amount: Double,
    val category: String,
    val note: String
)

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    /**
     * Checks if a valid non-placeholder API key is available.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("API_KEY")
    }

    /**
     * Generates intelligent, personalized financial recommendations based on expense data.
     */
    suspend fun generateFinancialInsights(expenses: List<Expense>, monthlyBudget: Double, savingsGoal: Double): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            // Return high quality offline fallback insights
            return@withContext getOfflineMockInsights(expenses, monthlyBudget, savingsGoal)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"

        val totalSpending = expenses.sumOf { it.amount }
        val categoryTotals = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
        val categoryBreakdownString = categoryTotals.entries.joinToString { "${it.key}: ${it.value}" }

        val systemPrompt = "You are SpendWise AI, an expert elite personal wealth manager and financial analyst. Give rich, analytical, modern fintech summaries."
        val prompt = """
            Analyze my current financial transactions for this month and give me highly personalized, actionable savings suggestions, trend analysis, unusual spending alerts, and recommendations. Provide exactly 4 concise points. Keep them direct, punchy, and premium.
            
            My Financial Profile:
            - Monthly Budget Limit: $monthlyBudget
            - Monthly Savings Goal: $savingsGoal
            - Total Spending This Month: $totalSpending
            - Spending breakdown per category: $categoryBreakdownString
            - Transactions Count: ${expenses.size}
            
            Give your output in a clean bulleted format with high visual appeal.
        """.trimIndent()

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val rootJson = JSONObject(responseBody)
                val textResponse = rootJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                return@withContext textResponse
            } else {
                Log.e("GeminiRepository", "API Error: ${response.code} ${response.message}")
                return@withContext getOfflineMockInsights(expenses, monthlyBudget, savingsGoal) + "\n\n⚠️ (Note: API request failed with error code ${response.code}; showing local offline insights)"
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error calling Gemini API", e)
            return@withContext getOfflineMockInsights(expenses, monthlyBudget, savingsGoal) + "\n\n⚠️ (Note: Could not connect to Gemini API; showing local offline insights)"
        }
    }

    /**
     * Parses a spoken phrase (like "Spent 500 rupees on coffee of Starbucks") into a structured expense JSON.
     */
    suspend fun parseVoiceInput(text: String): VoiceParseResult? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext parseVoiceInputOffline(text)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"

        val prompt = """
            Parse the following spoken financial transaction text: "$text"
            
            Extract the transaction amount, classify it into exactly one of the supported categories: Food, Travel, Shopping, Bills, Entertainment, Health, Education, Others. Also extract a brief note.
            
            Return output strictly matching this JSON schema:
            {
               "amount": double,
               "category": "Food" | "Travel" | "Shopping" | "Bills" | "Entertainment" | "Health" | "Education" | "Others",
               "note": "string"
            }
            Do not enclose in markdown codeblocks. Return raw JSON.
        """.trimIndent()

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val rootJson = JSONObject(responseBody)
                val rawText = rootJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val parsed = JSONObject(cleanJson)
                return@withContext VoiceParseResult(
                    amount = parsed.optDouble("amount", 0.0),
                    category = parsed.optString("category", "Others"),
                    note = parsed.optString("note", "Voice Expense")
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error parsing voice text via Gemini", e)
        }
        return@withContext parseVoiceInputOffline(text)
    }

    private fun parseVoiceInputOffline(text: String): VoiceParseResult {
        // Regex rules to intelligently parse numbers and categories offline
        val lower = text.lowercase()
        var extractedAmount = 0.0

        val numberRegex = "\\b\\d+(\\.\\d+)?\\b".toRegex()
        val match = numberRegex.find(lower)
        if (match != null) {
            extractedAmount = match.value.toDoubleOrNull() ?: 0.0
        }

        var category = "Others"
        when {
            lower.contains("eat") || lower.contains("food") || lower.contains("restaurant") || lower.contains("grocer") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("coffee") || lower.contains("starbucks") -> category = "Food"
            lower.contains("uber") || lower.contains("cab") || lower.contains("bus") || lower.contains("train") || lower.contains("flight") || lower.contains("fuel") || lower.contains("travel") || lower.contains("petrol") -> category = "Travel"
            lower.contains("shirt") || lower.contains("amazon") || lower.contains("clothes") || lower.contains("mall") || lower.contains("shopping") || lower.contains("buy") -> category = "Shopping"
            lower.contains("rent") || lower.contains("electricity") || lower.contains("water") || lower.contains("wifi") || lower.contains("mobile") || lower.contains("bills") || lower.contains("bill") -> category = "Bills"
            lower.contains("movie") || lower.contains("netflix") || lower.contains("concert") || lower.contains("show") || lower.contains("game") || lower.contains("entertainment") -> category = "Entertainment"
            lower.contains("doctor") || lower.contains("medicine") || lower.contains("hospital") || lower.contains("gym") || lower.contains("health") || lower.contains("pharmacy") -> category = "Health"
            lower.contains("book") || lower.contains("course") || lower.contains("school") || lower.contains("college") || lower.contains("tutor") || lower.contains("education") -> category = "Education"
        }

        val note = text.replace("(?i)\\bspent\\b".toRegex(), "")
            .replace("(?i)\\bspent\\b".toRegex(), "")
            .trim()
            .takeIf { it.isNotEmpty() } ?: "Voice Input"

        return VoiceParseResult(extractedAmount, category, note)
    }

    private fun getOfflineMockInsights(expenses: List<Expense>, monthlyBudget: Double, savingsGoal: Double): String {
        val totalSpending = expenses.sumOf { it.amount }
        val percent = if (monthlyBudget > 0) (totalSpending / monthlyBudget) * 100 else 0.0

        val categoryBreakdown = expenses.groupBy { it.category }
        val highestCategory = categoryBreakdown.entries.maxByOrNull { entry -> entry.value.sumOf { it.amount } }?.key ?: "N/A"

        return """
            📊 **SpendWise Financial Insights (Offline Engine)**
            
            • **Budget Progress**: You have utilized **${String.format("%.1f", percent)}%** of your monthly limit. ${if (percent > 90) "🚨 You are extremely close to overspending! Consider freezing non-essential subscriptions." else if (percent > 70) "⚠️ You are approaching your budget limits. Slow down shopping." else "✅ You are pacing extremely well for this month's goals."}
            
            • **Highest Spending focus**: Your peak expense driver this cycle is matches **$highestCategory**. Decreasing secondary habits in this channel can boost your savings rate immediately by **15%**.
            
            • **Streaks and Discipline**: You have logged expenses continuously! This is laying a powerful foundation for automated wealth accumulation metrics.
            
            • **Fintech Action Goal**: Transfer ₹3,000 to your savings pool immediately to hit your active target of **₹$savingsGoal** this month.
        """.trimIndent()
    }
}
