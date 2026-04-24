package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.financeapp.data.model.FinanceOverview

@Composable
fun HomeOverviewScreen(
    overview: FinanceOverview,
    onLogout: (() -> Unit)?,
    onOpenMembers: () -> Unit = {},
    onOpenCategories: () -> Unit = {}
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            HomeHeader(
                title = "Главная",
                subtitle = "Пользователь: ${overview.userEmail}",
                onLogout = onLogout
            )
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
            TransactionOverviewCard(transaction)
        }

        item {
            ActionCard(
                title = "Участники группы",
                glyph = "У",
                tint = Color(0xFF4C5E8B),
                boxColor = Color(0xFFE5ECFB),
                onClick = onOpenMembers
            )
        }

        item {
            ActionCard(
                title = "Категории",
                glyph = "К",
                tint = Color(0xFF5E4B8B),
                boxColor = Color(0xFFF1E7FB),
                onClick = onOpenCategories
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeHeader(title: String, subtitle: String, onLogout: (() -> Unit)?) {
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
            onClick = { onLogout?.invoke() },
            enabled = onLogout != null,
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
private fun ActionCard(title: String, glyph: String, tint: Color, boxColor: Color, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 6.dp,
        modifier = Modifier.fillMaxWidth().height(92.dp).clickable(onClick = onClick)
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
