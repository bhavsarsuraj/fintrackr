package com.surajbhavsar.fintrack.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surajbhavsar.fintrack.core.common.toMonthKey
import com.surajbhavsar.fintrack.domain.model.Category
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CategoryAvatar(
    category: Category,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val style = category.style()
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(style.color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = category.displayName,
            tint = style.color,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(top = Spacing.lg, bottom = Spacing.sm),
    )
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Shapes.Lg),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(Spacing.sm))
                }
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.lg))
            androidx.compose.material3.FilledTonalButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun MonthSelector(
    monthKey: String,
    onMonthChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayLabel = remember(monthKey) { formatMonthLabel(monthKey) }
    val isCurrent = remember(monthKey) { monthKey == System.currentTimeMillis().toMonthKey() }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Shapes.Pill),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.xs),
        ) {
            IconButton(onClick = { onMonthChange(shiftMonth(monthKey, -1)) }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                )
            }
            Text(
                displayLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = Spacing.sm),
            )
            IconButton(
                onClick = { onMonthChange(shiftMonth(monthKey, 1)) },
                enabled = !isCurrent,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                )
            }
        }
    }
}

private fun shiftMonth(monthKey: String, delta: Int): String {
    val parts = monthKey.split("-")
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, parts[0].toInt())
        set(Calendar.MONTH, parts[1].toInt() - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, delta)
    }
    return cal.timeInMillis.toMonthKey()
}

private fun formatMonthLabel(monthKey: String): String {
    val parts = monthKey.split("-")
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, parts[0].toInt())
        set(Calendar.MONTH, parts[1].toInt() - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val now = Calendar.getInstance()
    val pattern = if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) "MMMM" else "MMMM yyyy"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(cal.time)
}

@Composable
fun CategoryFilterChips(
    selected: Category?,
    onSelect: (Category?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.lg),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("All") },
                shape = RoundedCornerShape(Shapes.Pill),
            )
        }
        items(Category.entries) { category ->
            val style = category.style()
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(if (selected == category) null else category) },
                leadingIcon = {
                    Icon(style.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                label = { Text(category.displayName) },
                shape = RoundedCornerShape(Shapes.Pill),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = style.color.copy(alpha = 0.15f),
                    selectedLabelColor = style.color,
                    selectedLeadingIconColor = style.color,
                ),
            )
        }
    }
}

@Composable
fun ProgressTrack(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progress",
    )
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 300),
        label = "progressColor",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(Shapes.Pill))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animatedFraction)
                .height(height)
                .background(animatedColor),
        )
    }
}

@Composable
fun ListSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val base = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(Shapes.Lg))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Shapes.Lg))
    Box(
        modifier.then(if (onClick != null) base.clickable(onClick = onClick) else base),
    ) { content() }
}

@Composable
fun PrimaryAmount(
    amountText: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = amountText,
        style = style,
        color = color,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

