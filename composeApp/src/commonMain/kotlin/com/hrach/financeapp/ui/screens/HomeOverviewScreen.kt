package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.GroupOverview
import org.jetbrains.compose.resources.painterResource
import smartbudget.composeapp.generated.resources.Res
import smartbudget.composeapp.generated.resources.bg_balance_card
import smartbudget.composeapp.generated.resources.bg_expense_card
import smartbudget.composeapp.generated.resources.bg_income_card
import smartbudget.composeapp.generated.resources.img_wallet2

@Composable
fun HomeOverviewScreen(
    overview: FinanceOverview,
    onLogout: (() -> Unit)?,
    onOpenMembers: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
    onSelectGroup: (GroupOverview) -> Unit = {},
    onOpenGroups: () -> Unit = {}
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
            GroupSelectorPreview(
                overview = overview,
                onSelectGroup = onSelectGroup,
                onOpenSettings = onOpenGroups
            )
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
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.72f), contentColor = AppPurple),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            FinanceIcon(FinanceIcon.Logout, contentDescription = "Выйти")
        }
    }
}

@Composable
private fun OfflineStatusCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppGreen))
            Column {
                Text("Онлайн", color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                Text("Синхронизация и офлайн-очередь будут подключены позже", color = Color(0xFF6B6579), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun GroupSelectorPreview(
    overview: FinanceOverview,
    onSelectGroup: (GroupOverview) -> Unit,
    onOpenSettings: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val activeGroup = overview.groups.firstOrNull { it.id == overview.activeGroupId }
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Группа", color = Color(0xFF6B6579), fontSize = 12.sp)
                Box {
                    Button(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppLilac, contentColor = AppPurple),
                        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
                    ) {
                        Text(activeGroup?.name ?: overview.activeGroupName)
                        FinanceIcon(FinanceIcon.ChevronDown, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        overview.groups.forEach { group ->
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    if (group.id != overview.activeGroupId) {
                                        onSelectGroup(group)
                                    }
                                }
                            ) {
                                Text(
                                    text = if (group.id == overview.activeGroupId) "${group.name} • активна" else group.name,
                                    color = if (group.id == overview.activeGroupId) Color(0xFF16A34A) else Color(0xFF2F2B3A)
                                )
                            }
                        }
                    }
                }
            }
            RoundIconButton(
                icon = FinanceIcon.Settings,
                contentDescription = "Настроить группы",
                onClick = onOpenSettings,
                background = AppPurple,
                contentColor = Color.White
            )
        }
    }
}

@Composable
private fun SummaryHeroCard(overview: FinanceOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(202.dp),
            radius = 28.dp,
            elevation = 12.dp,
            backgroundColor = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(Res.drawable.bg_balance_card),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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
                        .padding(top = 16.dp, end = 18.dp)
                        .size(112.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White.copy(alpha = 0.26f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.img_wallet2),
                        contentDescription = null,
                        modifier = Modifier.size(86.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.TopStart).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Общий баланс", color = Color.White.copy(alpha = 0.84f), style = MaterialTheme.typography.subtitle1)
                    Text(overview.summary.balanceLabel, color = Color.White, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
                    Text(overview.summary.subtitle, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.body2)
                }
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
    val signColor = if (isIncome) Color(0xFF16A34A) else Color(0xFFE85B6A)

    GlassCard(
        modifier = modifier
            .height(150.dp),
        radius = 28.dp,
        elevation = 8.dp,
        backgroundColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(if (isIncome) Res.drawable.bg_income_card else Res.drawable.bg_expense_card),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-6).dp)
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
            Column(modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 10.dp, bottom = 16.dp)) {
                Text(title, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(if (isIncome) "+" else "−", color = Color.White, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                    Text(value.replace("+", "").replace("-", "").replace("−", ""), color = Color.White, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                }
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
    GlassCard(
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
                FinanceIcon(
                    icon = if (title.contains("Участ")) FinanceIcon.Group else FinanceIcon.Tag,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(title, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2B3A))
        }
    }
}
