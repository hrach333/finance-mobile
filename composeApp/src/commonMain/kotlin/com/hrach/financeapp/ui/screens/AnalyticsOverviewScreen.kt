package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hrach.financeapp.data.currency.CurrencyCatalog
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.TransactionKind
import org.jetbrains.compose.resources.painterResource
import smartbudget.composeapp.generated.resources.Res
import smartbudget.composeapp.generated.resources.bg_expense_card
import smartbudget.composeapp.generated.resources.bg_income_card
import kotlin.math.abs
import kotlin.math.roundToLong

private val ChartColors = listOf(
    Color(0xFF5E4B8B),
    Color(0xFFE85B6A),
    Color(0xFF0EA5E9),
    Color(0xFF16A34A),
    Color(0xFFF97316),
    Color(0xFF14B8A6),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6)
)

private data class ExpenseSlice(
    val name: String,
    val amount: Double,
    val color: Color
)

@Composable
fun AnalyticsOverviewScreen(
    overview: FinanceOverview,
    aiHelpEnabled: Boolean = false,
    showAiAdvice: Boolean = false,
    aiAdvice: String? = null,
    aiError: String? = null,
    aiLoading: Boolean = false,
    onRequestAiAdvice: () -> Unit = {},
    onRefreshAiAdvice: () -> Unit = {},
    onDismissAiAdvice: () -> Unit = {}
) {
    val currency = overview.activeCurrency()
    val expenseSlices = remember(overview.transactions, overview.categories) {
        val categoryNames = overview.categories.associate { it.id to it.name }
        overview.transactions
            .filter { it.kind == TransactionKind.Expense }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .filterValues { it > 0.0 }
            .entries
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                ExpenseSlice(
                    name = entry.key?.let { categoryNames[it] } ?: "Без категории",
                    amount = entry.value,
                    color = ChartColors[index % ChartColors.size]
                )
            }
    }
    val totalExpense = expenseSlices.sumOf { it.amount }
    val totalIncome = overview.transactions
        .filter { it.kind == TransactionKind.Income }
        .sumOf { it.amount }

    if (showAiAdvice) {
        FinanceAiAdviceDialog(
            advice = aiAdvice,
            error = aiError,
            isLoading = aiLoading,
            onRefresh = onRefreshAiAdvice,
            onDismiss = onDismissAiAdvice
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader(title = "Аналитика", subtitle = "Структура доходов и расходов")
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMetricCard(
                    title = "Доход",
                    value = totalIncome.moneyLabel(currency, withSign = true),
                    accent = Color(0xFF16A34A),
                    isIncome = true,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCard(
                    title = "Расход",
                    value = totalExpense.moneyLabel(currency, withSign = true, negative = true),
                    accent = Color(0xFFE85B6A),
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            ExpenseDonutCard(
                slices = expenseSlices,
                totalExpense = totalExpense,
                currency = currency,
                aiHelpEnabled = aiHelpEnabled,
                aiLoading = aiLoading,
                hasAiAdvice = !aiAdvice.isNullOrBlank(),
                onRequestAiAdvice = onRequestAiAdvice
            )
        }
        item {
            Text(
                text = "Топ категорий расхода",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF23212B)
            )
        }
        if (expenseSlices.isEmpty()) {
            item {
                EmptyAnalyticsCard()
            }
        } else {
            items(expenseSlices) { slice ->
                CategoryLegendItem(slice = slice, totalExpense = totalExpense, currency = currency)
            }
        }
        if (overview.insights.isNotEmpty()) {
            item {
                Text(
                    text = "Наблюдения",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF23212B)
                )
            }
            items(overview.insights) { insight ->
                InsightCard(text = insight)
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    accent: Color,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = modifier.height(132.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(if (isIncome) Res.drawable.bg_income_card else Res.drawable.bg_expense_card),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.align(Alignment.CenterStart).padding(16.dp)) {
                Text(title, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = value,
                    color = accent,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExpenseDonutCard(
    slices: List<ExpenseSlice>,
    totalExpense: Double,
    currency: String,
    aiHelpEnabled: Boolean,
    aiLoading: Boolean,
    hasAiAdvice: Boolean,
    onRequestAiAdvice: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Структура расходов",
                    color = Color(0xFF23212B),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (aiHelpEnabled) {
                    Button(
                        onClick = onRequestAiAdvice,
                        enabled = !aiLoading
                    ) {
                        Text(
                            when {
                                aiLoading -> "ИИ..."
                                hasAiAdvice -> "Прочесть анализ ИИ"
                                else -> "ИИ помощник"
                            }
                        )
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                if (totalExpense <= 0.0) {
                    Text(
                        text = "Нет данных о расходах за текущий период",
                        color = Color(0xFF6B6579),
                        textAlign = TextAlign.Center
                    )
                } else {
                    DonutChart(slices = slices, totalExpense = totalExpense)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Расходы", color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                        Text(
                            text = totalExpense.moneyLabel(currency),
                            color = Color(0xFF5E4B8B),
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceAiAdviceDialog(
    advice: String?,
    error: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color(0xFFFFFBFF),
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 760.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "Финансовый ИИ помощник",
                        color = Color(0xFF23212B),
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isLoading) "Готовлю анализ" else "Сохраненный анализ можно открыть повторно",
                        color = Color(0xFF6B6579),
                        style = MaterialTheme.typography.body2
                    )
                }

                Divider(color = Color(0xFFE8E1F0))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    when {
                        isLoading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text(
                                    text = "Анализирую доходы, расходы и категории...",
                                    color = Color(0xFF2F2B3A)
                                )
                            }
                        }
                        error != null -> Text(
                            text = error,
                            color = Color(0xFFE85B6A),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                        advice != null -> FinanceAiMarkdownText(
                            text = advice,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                        else -> Text("Ответ пока пустой.", color = Color(0xFF2F2B3A))
                    }
                }

                Divider(color = Color(0xFFE8E1F0))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isLoading) {
                        TextButton(onClick = onRefresh) {
                            Text("Пересчитать")
                        }
                        TextButton(onClick = onDismiss) {
                            Text("Закрыть")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceAiMarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { rawLine ->
                val headingLevel = rawLine.takeWhile { it == '#' }.length
                val line = rawLine
                    .removePrefix("#".repeat(headingLevel))
                    .trim()
                    .removeMarkdownMarkers()
                when {
                    headingLevel > 0 -> Text(
                        text = line,
                        color = Color(0xFF2F2B3A),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (headingLevel == 1) 20.sp else 18.sp
                    )
                    rawLine.startsWith("- ") || rawLine.startsWith("* ") -> Text(
                        text = "• ${rawLine.drop(2).trim().removeMarkdownMarkers()}",
                        color = Color(0xFF2F2B3A),
                        style = MaterialTheme.typography.body1
                    )
                    rawLine.matches(Regex("""\d+\.\s+.*""")) -> Text(
                        text = rawLine.removeMarkdownMarkers(),
                        color = Color(0xFF2F2B3A),
                        style = MaterialTheme.typography.body1
                    )
                    else -> Text(
                        text = line,
                        color = Color(0xFF2F2B3A),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
    }
}

private fun String.removeMarkdownMarkers(): String {
    return replace(Regex("""\*\*(.*?)\*\*"""), "$1")
        .replace(Regex("""__(.*?)__"""), "$1")
        .replace(Regex("""`([^`]*)`"""), "$1")
        .replace(Regex("""^\s*>+\s*"""), "")
        .trim()
}

@Composable
private fun DonutChart(slices: List<ExpenseSlice>, totalExpense: Double) {
    Canvas(modifier = Modifier.size(260.dp)) {
        val diameter = size.minDimension
        val ringWidth = diameter * 0.14f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        drawCircle(
            color = Color(0xFFE8E1F0),
            radius = diameter / 2f - ringWidth / 2f,
            center = center,
            style = Stroke(width = ringWidth)
        )

        var startAngle = -90f
        slices.forEach { slice ->
            val sweepAngle = (slice.amount / totalExpense * 360f).toFloat()
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(slice.color, slice.color.copy(alpha = 0.76f)),
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

        drawCircle(color = Color(0xFFF9F6FC), radius = diameter * 0.31f, center = center)
        drawCircle(
            color = Color.White.copy(alpha = 0.82f),
            radius = diameter * 0.33f,
            center = center,
            style = Stroke(width = ringWidth * 0.18f)
        )
    }
}

@Composable
private fun CategoryLegendItem(slice: ExpenseSlice, totalExpense: Double, currency: String) {
    val percent = if (totalExpense > 0.0) slice.amount / totalExpense * 100.0 else 0.0
    Card(
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(14.dp).background(slice.color, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(slice.name, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                    Text("${percent.roundPercent()}%", color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                }
            }
            Text(slice.amount.moneyLabel(currency), color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyAnalyticsCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Расходов пока нет, график появится после первых операций.",
            modifier = Modifier.padding(18.dp),
            color = Color(0xFF6B6579)
        )
    }
}

@Composable
private fun InsightCard(text: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text, modifier = Modifier.padding(18.dp), color = Color(0xFF2F2B3A))
    }
}

private fun FinanceOverview.activeCurrency(): String {
    return groups.firstOrNull { it.id == activeGroupId }?.baseCurrency ?: CurrencyCatalog.DEFAULT_CODE
}

private fun Double.moneyLabel(
    currency: String,
    withSign: Boolean = false,
    negative: Boolean = false
): String {
    val sign = when {
        !withSign -> ""
        negative -> "-"
        this > 0.0 -> "+"
        else -> ""
    }
    val rounded = abs(this).roundToLong().toString()
    val grouped = rounded.reversed().chunked(3).joinToString(" ").reversed()
    return "$sign$grouped ${CurrencyCatalog.symbolFor(currency)}"
}

private fun Double.roundPercent(): String {
    val rounded = (this * 10).roundToLong() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
