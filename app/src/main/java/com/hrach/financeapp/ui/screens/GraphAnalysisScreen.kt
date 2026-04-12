package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrach.financeapp.R
import com.hrach.financeapp.viewmodel.HomeViewModel
import kotlin.math.min

private val PieColors = listOf(
    Color(0xFF5B21B6),
    Color(0xFFDB2777),
    Color(0xFF0EA5E9),
    Color(0xFF16A34A),
    Color(0xFFF97316),
    Color(0xFF14B8A6),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6)
)

private data class CategorySlice(
    val name: String,
    val amount: Double,
    val color: Color
)

@Composable
fun GraphAnalysisScreen(viewModel: HomeViewModel, paddingValues: PaddingValues) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    val totalIncome = summary?.income ?: 0.0
    val totalExpense = summary?.expense ?: 0.0

    val categorySlices = remember(transactions, categories) {
        val expenseTransactions = transactions.filter { it.type.uppercase() == "EXPENSE" }
        val totalsByCategory = expenseTransactions.groupBy { it.categoryId }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }

        totalsByCategory.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                val categoryName = entry.key?.let { id ->
                    categories.firstOrNull { category -> category.id == id }?.name
                } ?: "Без категории"
                CategorySlice(
                    name = categoryName ?: "Без категории",
                    amount = entry.value,
                    color = PieColors[index % PieColors.size]
                )
            }
    }

    val totalExpenseForChart = categorySlices.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Графический анализ",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

            item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMetricCard(
                    label = "Доход",
                    value = totalIncome,
                    isIncome = true,
                    backgroundRes = R.drawable.bg_income_card,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                )
                SummaryMetricCard(
                    label = "Расход",
                    value = totalExpense,
                    isIncome = false,
                    backgroundRes = R.drawable.bg_expense_card,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                )
            }
        }

        item {
            Text(
                text = "Структура расходов по категориям",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            if (totalExpenseForChart <= 0.0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет данных о расходах за текущий период",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val canvasBackgroundColor = MaterialTheme.colorScheme.background
                    val canvasStrokeColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .shadow(18.dp, RoundedCornerShape(24.dp), clip = false)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bg_analysis_card),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.04f))
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val chartSize = 280.dp
                            Canvas(modifier = Modifier.size(chartSize)) {
                                val diameter = min(size.width, size.height)
                                val ringWidth = diameter * 0.14f
                                val innerRadius = (diameter - ringWidth * 2f) / 2f
                                val topLeft = Offset(
                                    (size.width - diameter) / 2f,
                                    (size.height - diameter) / 2f
                                )
                                val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                                // Base donut shadow and subtle background
                                drawCircle(
                                    color = canvasBackgroundColor,
                                    radius = diameter / 2f,
                                    center = center
                                )
                                drawCircle(
                                    color = canvasStrokeColor.copy(alpha = 0.12f),
                                    radius = diameter / 2f - ringWidth * 0.25f,
                                    center = center,
                                    style = Stroke(width = ringWidth * 0.55f)
                                )

                                var startAngle = -90f
                                categorySlices.forEach { slice ->
                                    val sweepAngle = (slice.amount / totalExpenseForChart * 360f).toFloat()
                                    drawArc(
                                        brush = Brush.linearGradient(
                                            colors = listOf(slice.color, slice.color.copy(alpha = 0.8f)),
                                            start = Offset(center.x, topLeft.y),
                                            end = Offset(center.x, topLeft.y + diameter)
                                        ),
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = ringWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweepAngle
                                }

                                drawCircle(
                                    color = canvasBackgroundColor,
                                    radius = innerRadius,
                                    center = center
                                )
                                drawCircle(
                                    color = canvasStrokeColor.copy(alpha = 0.08f),
                                    radius = innerRadius + ringWidth * 0.12f,
                                    center = center,
                                    style = Stroke(width = ringWidth * 0.16f)
                                )

                                // Top highlight for 3D effect
                                drawArc(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.22f),
                                            Color.Transparent
                                        ),
                                        start = Offset(topLeft.x, topLeft.y),
                                        end = Offset(topLeft.x + diameter, topLeft.y + diameter * 0.3f)
                                    ),
                                    startAngle = -120f,
                                    sweepAngle = 120f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = ringWidth * 0.8f, cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Расходы",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${totalExpense.toInt()} ₽",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Топ категорий расхода",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (categorySlices.isEmpty()) {
            item {
                Text(
                    text = "Нет расходов для группировки по категориям.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(categorySlices) { slice ->
                CategoryLegendItem(slice = slice, totalExpense = totalExpenseForChart)
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    value: Double,
    isIncome: Boolean,
    @DrawableRes backgroundRes: Int? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (backgroundRes != null) {
                Image(
                    painter = painterResource(id = backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.16f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = 360f
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isIncome) {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE7F9EC), Color(0xFFF5FFF8))
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFDE8ED), Color(0xFFFFF4F6))
                                )
                            },
                            shape = RoundedCornerShape(24.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.86f)
                )
                Text(
                    text = "${if (isIncome) "+" else "−"}${value.toInt()} ₽",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isIncome) Color(0xFF15803D) else Color(0xFFB91C1C)
                )
            }
        }
    }
}

@Composable
private fun CategoryLegendItem(slice: CategorySlice, totalExpense: Double) {
    val percent = if (totalExpense > 0.0) slice.amount / totalExpense * 100 else 0.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color = slice.color, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = slice.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${percent.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "${slice.amount.toInt()} ₽",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
