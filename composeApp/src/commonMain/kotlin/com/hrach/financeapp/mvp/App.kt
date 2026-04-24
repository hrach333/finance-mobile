package com.hrach.financeapp.mvp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.OverviewColorToken
import com.hrach.financeapp.data.model.TransactionKind
import com.hrach.financeapp.data.model.TransactionOverview
import com.hrach.financeapp.data.repository.DemoFinanceOverviewRepository
import com.hrach.financeapp.data.repository.FinanceOverviewRepository

private enum class MvpTab(val title: String, val glyph: String) {
    Home("Главная", "Г"),
    Transactions("Операции", "О"),
    Accounts("Счета", "С"),
    Analytics("Аналитика", "А")
}

private val backgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFCCCFDF), Color(0xFFEFD6EF), Color(0xFFABA7CE))
)


@Composable
fun App(repository: FinanceOverviewRepository = DemoFinanceOverviewRepository()) {
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF5E4B8B),
            primaryVariant = Color(0xFF4C5E8B),
            secondary = Color(0xFF16A34A),
            background = Color(0xFFEFD6EF),
            surface = Color(0xFFF9F6FC),
            error = Color(0xFFE85B6A)
        )
    ) {
        var selectedTab by remember { mutableStateOf(MvpTab.Home) }
        var overview by remember(repository) { mutableStateOf<FinanceOverview?>(null) }
        var loadingError by remember(repository) { mutableStateOf<String?>(null) }

        LaunchedEffect(repository) {
            loadingError = null
            overview = runCatching { repository.getOverview() }
                .onFailure { loadingError = it.message ?: "Не удалось загрузить данные" }
                .getOrNull()
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            ResponsiveShell(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            ) {
                val loadedOverview = overview
                if (loadedOverview == null) {
                    LoadingDashboard(error = loadingError)
                } else {
                    when (selectedTab) {
                        MvpTab.Home -> HomeDashboard(loadedOverview)
                        MvpTab.Transactions -> TransactionsDashboard(loadedOverview)
                        MvpTab.Accounts -> AccountsDashboard(loadedOverview)
                        MvpTab.Analytics -> AnalyticsDashboard(loadedOverview)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingDashboard(error: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HeaderBlock(title = "SmartBudget", subtitle = "Загрузка данных")
        Card(
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xFFF9F6FC),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = error ?: "Подключаем финансовые данные...",
                modifier = Modifier.padding(18.dp),
                color = if (error == null) Color(0xFF2F2B3A) else Color(0xFFE85B6A)
            )
        }
    }
}

@Composable
private fun ResponsiveShell(
    selectedTab: MvpTab,
    onTabSelected: (MvpTab) -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        val useRail = maxWidth >= 760.dp

        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                DesktopRail(selectedTab = selectedTab, onTabSelected = onTabSelected)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(0.72f)) {
                        content()
                    }
                }
            }
        } else {
            Scaffold(
                backgroundColor = Color.Transparent,
                bottomBar = {
                    BottomNavigation(backgroundColor = Color(0xFFF9F6FC), elevation = 10.dp) {
                        MvpTab.entries.forEach { tab ->
                            BottomNavigationItem(
                                selected = selectedTab == tab,
                                onClick = { onTabSelected(tab) },
                                icon = { NavigationGlyph(tab, selectedTab == tab) },
                                label = { Text(tab.title, fontSize = 11.sp) },
                                selectedContentColor = Color(0xFF5E4B8B),
                                unselectedContentColor = Color(0xFF6B6579)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DesktopRail(selectedTab: MvpTab, onTabSelected: (MvpTab) -> Unit) {
    Column(
        modifier = Modifier
            .width(206.dp)
            .fillMaxHeight()
            .background(Color(0xFFF9F6FC).copy(alpha = 0.78f))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "SmartBudget",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF23212B)
        )
        Text(
            text = "KMP preview",
            color = Color(0xFF6B6579),
            style = MaterialTheme.typography.body2
        )
        Spacer(modifier = Modifier.height(12.dp))
        MvpTab.entries.forEach { tab ->
            RailItem(tab = tab, selected = selectedTab == tab, onClick = { onTabSelected(tab) })
        }
    }
}

@Composable
private fun RailItem(tab: MvpTab, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) Color(0xFFF1E7FB) else Color.Transparent
    val textColor = if (selected) Color(0xFF5E4B8B) else Color(0xFF4B4760)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavigationGlyph(tab = tab, selected = selected)
        Text(tab.title, color = textColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun NavigationGlyph(tab: MvpTab, selected: Boolean) {
    val background = if (selected) Color(0xFF5E4B8B) else Color(0xFFE8E1F0)
    val content = if (selected) Color.White else Color(0xFF6B6579)

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(tab.glyph, color = content, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun HomeDashboard(overview: FinanceOverview) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            HeaderBlock(title = "Главная", subtitle = "Пользователь: ${overview.userEmail}")
        }

        item {
            OfflineStatusCard()
        }

        item {
            GroupSelectorPreview(overview.activeGroupName)
        }

        item {
            SummaryHeroCard(overview)
        }

        item {
            SectionTitle("Последние операции")
        }

        items(overview.transactions.take(4)) { transaction ->
            TransactionCard(transaction)
        }

        item {
            ActionCard(title = "Участники группы", glyph = "У", tint = Color(0xFF4C5E8B), boxColor = Color(0xFFE5ECFB))
        }

        item {
            ActionCard(title = "Категории", glyph = "К", tint = Color(0xFF5E4B8B), boxColor = Color(0xFFF1E7FB))
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TransactionsDashboard(overview: FinanceOverview) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HeaderBlock(title = "Операции", subtitle = "Демонстрационный список до подключения Laravel API")
        }
        items(overview.transactions) { transaction ->
            TransactionCard(transaction)
        }
    }
}

@Composable
private fun AccountsDashboard(overview: FinanceOverview) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HeaderBlock(title = "Счета", subtitle = "Баланс по семейной группе")
        }
        items(overview.accounts) { account ->
            AccountPreviewCard(
                title = account.title,
                balance = account.balanceLabel,
                subtitle = account.subtitle,
                tint = account.colorToken.toColor()
            )
        }
    }
}

@Composable
private fun AnalyticsDashboard(overview: FinanceOverview) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HeaderBlock(title = "Аналитика", subtitle = "Пока статическая, затем будет из API")
        }
        items(overview.insights) { insight ->
            InsightCard(insight)
        }
    }
}

@Composable
private fun HeaderBlock(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF23212B)
            )
            Text(text = subtitle, color = Color(0xFF4B4760), style = MaterialTheme.typography.body2)
        }
        Button(
            onClick = {},
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF9F6FC), contentColor = Color(0xFF5E4B8B)),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Text("Выйти")
        }
    }
}

@Composable
private fun OfflineStatusCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color(0xFFF9F6FC).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF16A34A)))
            Column {
                Text("Онлайн", color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                Text("Синхронизация и офлайн-очередь будут подключены позже", color = Color(0xFF6B6579), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun GroupSelectorPreview(groupName: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Группа", color = Color(0xFF6B6579), fontSize = 12.sp)
                Text(groupName, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {},
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF1E7FB), contentColor = Color(0xFF5E4B8B)),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text("+")
            }
        }
    }
}

@Composable
private fun SummaryHeroCard(overview: FinanceOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFEFE5FF), Color(0xFFD8ECF7), Color(0xFFF8F1F7))
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 26.dp, y = 20.dp)
                    .size(168.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.28f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 20.dp)
                    .size(88.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(alpha = 0.42f)),
                contentAlignment = Alignment.Center
            ) {
                Text("₽", color = Color(0xFF5E4B8B), fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Общий баланс", color = Color(0xFF454545).copy(alpha = 0.78f), style = MaterialTheme.typography.subtitle1)
                Text(overview.summary.balanceLabel, color = Color(0xFF454545), style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
                Text(overview.summary.subtitle, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard("Расходы", overview.summary.expenseLabel, false, Modifier.weight(1f))
            SummaryStatCard("Доходы", overview.summary.incomeLabel, true, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryStatCard(title: String, value: String, isIncome: Boolean, modifier: Modifier = Modifier) {
    val gradient = if (isIncome) {
        Brush.linearGradient(listOf(Color(0xFFF0F8E8), Color(0xFFE7F2DE), Color(0xFFF7FAF1)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFCECEF), Color(0xFFF7E1E6), Color(0xFFFBF3F5)))
    }
    val signColor = if (isIncome) Color(0xFF16A34A) else Color(0xFFE85B6A)

    Box(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(gradient)
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-6).dp)
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
        )
        Column(modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 10.dp, bottom = 16.dp)) {
            Text(title, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B4658))
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(if (isIncome) "+" else "−", color = signColor, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Text(value.replace("+", "").replace("-", "").replace("−", ""), color = Color(0xFF2F2B3A), style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, color = Color(0xFF23212B))
}

@Composable
private fun TransactionCard(transaction: TransactionOverview) {
    val tint = transaction.colorToken.toColor()

    Card(
        backgroundColor = Color(0xFFF4EDF7),
        elevation = 3.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(transaction.category.take(1), color = tint, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                Text(transaction.comment, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
            }
            Text(transaction.dateLabel, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
            Text(
                transaction.amountLabel,
                color = if (transaction.kind == TransactionKind.Income) Color(0xFF16A34A) else Color(0xFFDC2626),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActionCard(title: String, glyph: String, tint: Color, boxColor: Color) {
    Card(
        shape = RoundedCornerShape(28.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 6.dp,
        modifier = Modifier.fillMaxWidth().height(92.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(boxColor),
                contentAlignment = Alignment.Center
            ) {
                Text(glyph, color = tint, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            Text(title, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2B3A))
        }
    }
}

@Composable
private fun AccountPreviewCard(title: String, balance: String, subtitle: String, tint: Color) {
    Card(
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                Text(balance, color = tint, fontWeight = FontWeight.Bold)
            }
            Divider(color = Color.White.copy(alpha = 0.7f))
            Text(subtitle, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
        }
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

private fun OverviewColorToken.toColor(): Color {
    return when (this) {
        OverviewColorToken.Income -> Color(0xFF16A34A)
        OverviewColorToken.Expense -> Color(0xFFE85B6A)
        OverviewColorToken.Primary -> Color(0xFF5E4B8B)
        OverviewColorToken.Secondary -> Color(0xFF4C5E8B)
        OverviewColorToken.Muted -> Color(0xFF6B6579)
    }
}
