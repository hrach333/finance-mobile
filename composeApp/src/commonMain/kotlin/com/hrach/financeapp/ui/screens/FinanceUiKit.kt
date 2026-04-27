package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.imageResource
import smartbudget.composeapp.generated.resources.Res
import smartbudget.composeapp.generated.resources.category_icons_sprite

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
    Check,
    Shopping,
    Transport,
    Health,
    Food,
    Sport,
    Work,
    Gift,
    Cash,
    Other
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

@Composable
fun CategorySpriteIcon(
    iconKey: String?,
    type: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    val index = iconKey.categorySpriteIndex(type).coerceIn(0, 63)
    val column = index % 8
    val row = index / 8
    val sprite = imageResource(Res.drawable.category_icons_sprite)
    val cellWidth = sprite.width / 8
    val cellHeight = sprite.height / 8
    val sourceInset = 0

    Canvas(modifier = modifier.size(size)) {
        drawImage(
            image = sprite,
            srcOffset = IntOffset(column * cellWidth + sourceInset, row * cellHeight + sourceInset),
            srcSize = IntSize(cellWidth - sourceInset * 2, cellHeight - sourceInset * 2),
            dstSize = IntSize(this.size.width.toInt(), this.size.height.toInt())
        )
    }
}

fun String?.toCategoryIcon(type: String): FinanceIcon = when (this?.lowercase()) {
    "shopping" -> FinanceIcon.Shopping
    "transport" -> FinanceIcon.Transport
    "health", "medicine", "medical" -> FinanceIcon.Health
    "home" -> FinanceIcon.Home
    "food", "restaurant" -> FinanceIcon.Food
    "sport", "sports" -> FinanceIcon.Sport
    "salary", "cash", "bonus" -> FinanceIcon.Cash
    "work" -> FinanceIcon.Work
    "gift" -> FinanceIcon.Gift
    "other" -> FinanceIcon.Other
    else -> if (type == "INCOME") FinanceIcon.Cash else FinanceIcon.Tag
}

fun String?.categoryColor(type: String): Color = when (this?.lowercase()) {
    "shopping" -> Color(0xFF8B5CF6)
    "transport" -> Color(0xFF2563EB)
    "health", "medicine", "medical" -> Color(0xFFE11D48)
    "home" -> Color(0xFF0F766E)
    "food", "restaurant" -> Color(0xFFF97316)
    "sport", "sports" -> Color(0xFF16A34A)
    "salary", "cash" -> Color(0xFF059669)
    "work" -> Color(0xFF4F46E5)
    "gift", "bonus" -> Color(0xFFDB2777)
    "other" -> Color(0xFF64748B)
    else -> if (type == "INCOME") AppGreen else AppRed
}

private fun String?.categorySpriteIndex(type: String): Int {
    val key = this?.lowercase()
    val directIndex = key
        ?.removePrefix("sprite_")
        ?.takeIf { it.all(Char::isDigit) }
        ?.toIntOrNull()
    if (directIndex != null) return directIndex

    return when (key) {
    "food", "restaurant" -> 0
    "groceries" -> 1
    "transport" -> 2
    "taxi" -> 3
    "travel_bag" -> 4
    "fuel", "gas" -> 5
    "home", "rent" -> 6
    "internet", "phone" -> 7
    "health", "medicine", "medical" -> 8
    "sport", "sports", "fitness" -> 9
    "entertainment" -> 10
    "cinema" -> 12
    "shopping" -> 13
    "gift", "gifts", "bonus" -> 14
    "travel" -> 15
    "education" -> 16
    "books" -> 17
    "salary", "income" -> 18
    "work", "freelance" -> 19
    "clothes" -> 20
    "gift_box" -> 21
    "savings", "piggy_bank" -> 22
    "bank", "account" -> 23
    "children" -> 24
    "cash" -> 25
    "transfer" -> 26
    "music", "subscription" -> 27
    "pets" -> 28
    "beauty" -> 29
    "coffee" -> 30
    "family" -> 31
    "dining" -> 32
    "repair", "tools" -> 33
    "car_service" -> 34
    "insurance" -> 35
    "business" -> 36
    "investment" -> 51
    "taxes" -> 37
    "charity" -> 38
    "loan" -> 39
    "pharmacy" -> 40
    "electronics", "camera" -> 41
    "games", "gamepad" -> 42
    "headphones" -> 43
    "vacation" -> 44
    "hotel" -> 45
    "airplane" -> 46
    "bus" -> 2
    "doctor" -> 50
    "cleaning" -> 43
    "furniture" -> 44
    "garden" -> 45
    "bar", "alcohol" -> 46
    "wallet", "card", "credit_card" -> 47
    "fines" -> 48
    "wallet_cash" -> 49
    "analytics" -> 52
    "receipt" -> 52
    "upload", "income_other" -> 53
    "custom" -> 54
    "other" -> 55
    else -> if (type == "INCOME") 53 else 55
    }
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
    FinanceIcon.Shopping -> Icons.Filled.ShoppingCart
    FinanceIcon.Transport -> Icons.Filled.DirectionsBus
    FinanceIcon.Health -> Icons.Filled.MedicalServices
    FinanceIcon.Food -> Icons.Filled.Restaurant
    FinanceIcon.Sport -> Icons.Filled.SportsSoccer
    FinanceIcon.Work -> Icons.Filled.Work
    FinanceIcon.Gift -> Icons.Filled.CardGiftcard
    FinanceIcon.Cash -> Icons.Filled.AttachMoney
    FinanceIcon.Other -> Icons.Filled.MoreHoriz
}
