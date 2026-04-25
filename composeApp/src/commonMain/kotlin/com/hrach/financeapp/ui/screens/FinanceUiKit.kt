package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val AppInk = Color(0xFF23212B)
val AppMuted = Color(0xFF6B6579)
val AppPurple = Color(0xFF5E4B8B)
val AppBlue = Color(0xFF4C5E8B)
val AppGreen = Color(0xFF16A34A)
val AppRed = Color(0xFFE85B6A)
val AppCard = Color(0xFFF9F6FC)
val AppLilac = Color(0xFFF1E7FB)

val AppBackgroundGradient = Brush.verticalGradient(
    listOf(Color(0xFFCCCFDF), Color(0xFFEFD6EF), Color(0xFFABA7CE))
)

enum class FinanceIcon {
    Home,
    List,
    Wallet,
    Tag,
    Group,
    Pie,
    Edit,
    Delete,
    Settings,
    Plus,
    Logout,
    Calendar,
    Filter,
    ChevronDown,
    Back,
    Check
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    radius: Dp = 24.dp,
    elevation: Dp = 8.dp,
    backgroundColor: Color = AppCard.copy(alpha = 0.94f),
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(radius),
        backgroundColor = backgroundColor,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.86f)),
        elevation = elevation,
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun RoundIconButton(
    icon: FinanceIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    background: Color = AppLilac,
    contentColor: Color = AppPurple,
    enabled: Boolean = true
) {
    val actualAlpha = if (enabled) 1f else ContentAlpha.disabled
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background.copy(alpha = background.alpha * actualAlpha))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor.copy(alpha = actualAlpha),
            LocalContentAlpha provides actualAlpha
        ) {
            FinanceIcon(icon = icon, contentDescription = contentDescription)
        }
    }
}

@Composable
fun FinanceIcon(
    icon: FinanceIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
) {
    Icon(
        imageVector = icon.imageVector(),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(22.dp)
    )
}

private fun FinanceIcon.imageVector(): ImageVector = when (this) {
    FinanceIcon.Home -> Icons.Filled.Home
    FinanceIcon.List -> Icons.AutoMirrored.Filled.List
    FinanceIcon.Wallet -> Icons.Filled.AccountBalanceWallet
    FinanceIcon.Tag -> Icons.Filled.LocalOffer
    FinanceIcon.Group -> Icons.Filled.Group
    FinanceIcon.Pie -> Icons.Filled.PieChart
    FinanceIcon.Edit -> Icons.Filled.Edit
    FinanceIcon.Delete -> Icons.Filled.Delete
    FinanceIcon.Settings -> Icons.Filled.Settings
    FinanceIcon.Plus -> Icons.Filled.Add
    FinanceIcon.Logout -> Icons.AutoMirrored.Filled.Logout
    FinanceIcon.Calendar -> Icons.Filled.DateRange
    FinanceIcon.Filter -> Icons.Filled.Tune
    FinanceIcon.ChevronDown -> Icons.Filled.ExpandMore
    FinanceIcon.Back -> Icons.AutoMirrored.Filled.ArrowBack
    FinanceIcon.Check -> Icons.Filled.Check
}
