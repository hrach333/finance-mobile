package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.OverviewColorToken

@Composable
fun AccountsOverviewScreen(overview: FinanceOverview) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader(title = "Счета", subtitle = "Баланс по семейной группе")
        }
        items(overview.accounts) { account ->
            AccountOverviewCard(account = account)
        }
    }
}

@Composable
private fun AccountOverviewCard(account: AccountOverview) {
    Card(
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(account.title, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                Text(account.balanceLabel, color = account.colorToken.toScreenColor(), fontWeight = FontWeight.Bold)
            }
            Divider(color = Color.White.copy(alpha = 0.7f))
            Text(account.subtitle, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
        }
    }
}

private fun OverviewColorToken.toScreenColor(): Color {
    return when (this) {
        OverviewColorToken.Income -> Color(0xFF16A34A)
        OverviewColorToken.Expense -> Color(0xFFE85B6A)
        OverviewColorToken.Primary -> Color(0xFF5E4B8B)
        OverviewColorToken.Secondary -> Color(0xFF4C5E8B)
        OverviewColorToken.Muted -> Color(0xFF6B6579)
    }
}
