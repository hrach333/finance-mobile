package com.hrach.financeapp.mvp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class MvpTab(val title: String) {
    Overview("Главная"),
    Transactions("Операции"),
    Accounts("Счета"),
    Insights("Аналитика")
}

private data class MvpTransaction(
    val title: String,
    val category: String,
    val amount: String,
    val isIncome: Boolean
)

private val sampleTransactions = listOf(
    MvpTransaction("Зарплата", "Доход", "+150 000 ₽", true),
    MvpTransaction("Продукты", "Семья", "-8 420 ₽", false),
    MvpTransaction("Такси", "Транспорт", "-1 150 ₽", false),
    MvpTransaction("Подработка", "Доход", "+18 000 ₽", true)
)

@Composable
fun App() {
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF1565C0),
            secondary = Color(0xFF00897B),
            surface = Color(0xFFFAFAFA),
            background = Color(0xFFF4F7F6)
        )
    ) {
        var selectedTab by remember { mutableStateOf(MvpTab.Overview) }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            ResponsiveShell(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            ) {
                when (selectedTab) {
                    MvpTab.Overview -> OverviewScreen()
                    MvpTab.Transactions -> TransactionsScreen()
                    MvpTab.Accounts -> AccountsScreen()
                    MvpTab.Insights -> InsightsScreen()
                }
            }
        }
    }
}

@Composable
private fun ResponsiveShell(
    selectedTab: MvpTab,
    onTabSelected: (MvpTab) -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 720.dp

        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(168.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MvpTab.entries.forEach { tab ->
                        RailItem(tab, selectedTab == tab) { onTabSelected(tab) }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp)
                ) {
                    content()
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    BottomNavigation(backgroundColor = Color.White) {
                        MvpTab.entries.forEach { tab ->
                            BottomNavigationItem(
                                selected = selectedTab == tab,
                                onClick = { onTabSelected(tab) },
                                icon = { TabGlyph(tab) },
                                label = { Text(tab.title) }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(18.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun RailItem(tab: MvpTab, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) Color(0xFFE3F2FD) else Color.Transparent
    val textColor = if (selected) Color(0xFF1565C0) else Color(0xFF455A64)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabGlyph(tab)
        Spacer(modifier = Modifier.width(10.dp))
        Text(tab.title, color = textColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun TabGlyph(tab: MvpTab) {
    val label = when (tab) {
        MvpTab.Overview -> "Г"
        MvpTab.Transactions -> "О"
        MvpTab.Accounts -> "С"
        MvpTab.Insights -> "А"
    }

    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OverviewScreen() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SmartBudget", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
                Text("MVP-каркас для Android, iPhone и desktop", color = Color(0xFF5F6F6B))
            }
        }

        item {
            BalanceCard()
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Доход", "168 000 ₽", Color(0xFF00897B), Modifier.weight(1f))
                MetricCard("Расход", "42 850 ₽", Color(0xFFC62828), Modifier.weight(1f))
            }
        }

        item {
            Text("Последние операции", style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
        }

        items(sampleTransactions.take(3)) { transaction ->
            TransactionRow(transaction)
        }
    }
}

@Composable
private fun TransactionsScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Операции", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sampleTransactions) { transaction ->
                TransactionRow(transaction)
            }
        }
    }
}

@Composable
private fun AccountsScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Счета", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
        MetricCard("Наличные", "18 300 ₽", Color(0xFF546E7A), Modifier.fillMaxWidth())
        MetricCard("Основная карта", "93 520 ₽", Color(0xFF1565C0), Modifier.fillMaxWidth())
        MetricCard("Накопления", "265 000 ₽", Color(0xFF6A4C93), Modifier.fillMaxWidth())
    }
}

@Composable
private fun InsightsScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Аналитика", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
        InfoCard("Баланс месяца положительный: доходы выше расходов на 125 150 ₽.")
        InfoCard("Самая крупная категория расходов сейчас: семья и продукты.")
        InfoCard("Следующий шаг MVP: подключить общий API-клиент и реальные DTO из Android-приложения.")
    }
}

@Composable
private fun BalanceCard() {
    Card(
        backgroundColor = Color(0xFF102A43),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Общий баланс", color = Color(0xFFE3F2FD))
            Text("376 820 ₽", color = Color.White, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
            Text("4 счета, 2 участника группы", color = Color(0xFFB3C7D6))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, backgroundColor = Color.White) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Color(0xFF607D78))
            Text(value, color = accent, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TransactionRow(transaction: MvpTransaction) {
    Card(backgroundColor = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(transaction.title, fontWeight = FontWeight.SemiBold)
                Text(transaction.category, color = Color(0xFF607D78), style = MaterialTheme.typography.body2)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                transaction.amount,
                color = if (transaction.isIncome) Color(0xFF00897B) else Color(0xFFC62828),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(backgroundColor = Color.White) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF263238)
        )
    }
}
