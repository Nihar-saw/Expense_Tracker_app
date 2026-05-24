package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.ui.theme.*

@Composable
fun PremiumDonutChart(
    categorySums: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val total = categorySums.values.sum().toFloat()
    if (total <= 0f) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No spending data available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    // Categories sorted for consistent arc placements
    val items = categorySums.entries.toList().sortedByDescending { it.value }
    val colors = listOf(
        BrandBlue, BrandEmerald, BrandAmber,
        Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFFF43F5E), Color(0xFF6B7280)
    )

    val animateSweep = remember { Animatable(0f) }
    LaunchedEffect(categorySums) {
        animateSweep.animateTo(1f, animationSpec = tween(1200))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                items.forEachIndexed { index, entry ->
                    val sweepAngle = ((entry.value / total) * 360f).toFloat() * animateSweep.value
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(10f, 10f),
                        size = Size(size.width - 20f, size.height - 20f),
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }

            // Central Summary Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
                Text(
                    text = String.format("%.0f", total),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Legends
        Column(
            modifier = Modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.take(4).forEachIndexed { index, entry ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colors[index % colors.size], CircleShape)
                    )
                    Text(
                        text = "${entry.key} (${String.format("%.0f", (entry.value / total) * 100)}%)",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumLineTrendGraph(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data points logic found for line trends",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    // Group spending by index (e.g. 7 entries for week, or past 7 days)
    // For representation, map previous 7 days
    val currentMillis = System.currentTimeMillis()
    val periodMillis = 7 * 24 * 60 * 60 * 1000L
    val recentExpenses = expenses.filter { it.date > currentMillis - periodMillis }

    val dailySpending = mutableMapOf<Int, Double>()
    for (i in 0..6) {
        dailySpending[i] = 0.0
    }

    recentExpenses.forEach { exp ->
        val daysAgo = ((currentMillis - exp.date) / (24 * 60 * 60 * 1000L)).toInt().coerceIn(0, 6)
        val index = 6 - daysAgo
        dailySpending[index] = (dailySpending[index] ?: 0.0) + exp.amount
    }

    val dataPoints = dailySpending.values.toList().map { it.toFloat() }
    val maxVal = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(100f)

    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(expenses) {
        animateProgress.animateTo(1f, animationSpec = tween(1500))
    }

    val gridLineColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = BrandBlue
    val secondaryColor = BrandEmerald

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Past 7 Days Spend Trend",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val spacing = width / 6

            // Draw grid guides
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height - (height / gridLines) * i
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw points path
            val path = Path()
            val fillPath = Path()

            val points = dataPoints.mapIndexed { index, value ->
                val x = index * spacing
                val y = height - (value / maxVal) * height * animateProgress.value
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, height)
                fillPath.lineTo(points.first().x, points.first().y)

                for (i in 1 until points.size) {
                    // Cubic Bezier styling curves
                    val prev = points[i - 1]
                    val curr = points[i]
                    val controlX1 = (prev.x + curr.x) / 2f
                    path.cubicTo(
                        controlX1, prev.y,
                        controlX1, curr.y,
                        curr.x, curr.y
                    )
                    fillPath.cubicTo(
                        controlX1, prev.y,
                        controlX1, curr.y,
                        curr.x, curr.y
                    )
                }

                fillPath.lineTo(points.last().x, height)
                fillPath.close()

                // Draw filled glow gradient under line
                val gradient = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
                drawPath(fillPath, brush = gradient)

                // Draw the connecting glow curve
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw specific node indicator dots
                points.forEachIndexed { _, offset ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = secondaryColor,
                        radius = 2.dp.toPx(),
                        center = offset
                    )
                }
            }
        }

        // Labels at x coordinates representing days
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val daysLabel = listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Yesterday", "Today")
            daysLabel.forEach { day ->
                Text(
                    text = day,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}
