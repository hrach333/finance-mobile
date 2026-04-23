package com.hrach.financeapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hrach.financeapp.ui.utils.categoryIcon

@Composable
fun QuickCategoryChip(name: String, type: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(categoryIcon(name, type), null, tint = MaterialTheme.colorScheme.primary)
        Text(name, style = MaterialTheme.typography.labelLarge)
    }
}
