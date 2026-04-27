package com.surajbhavsar.fintrack.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surajbhavsar.fintrack.core.common.toMoney
import com.surajbhavsar.fintrack.core.designsystem.CategoryAvatar
import com.surajbhavsar.fintrack.core.designsystem.EmptyStateView
import com.surajbhavsar.fintrack.core.designsystem.MonthSelector
import com.surajbhavsar.fintrack.core.designsystem.PrimaryAmount
import com.surajbhavsar.fintrack.core.designsystem.ProgressTrack
import com.surajbhavsar.fintrack.core.designsystem.SectionTitle
import com.surajbhavsar.fintrack.core.designsystem.Shapes
import com.surajbhavsar.fintrack.core.designsystem.Spacing
import com.surajbhavsar.fintrack.core.designsystem.StatCard
import com.surajbhavsar.fintrack.core.designsystem.style
import com.surajbhavsar.fintrack.core.ui.UiState
import com.surajbhavsar.fintrack.domain.model.BudgetUsage
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.MonthlyInsight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val monthKey by viewModel.monthKey.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Insights", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                contentAlignment = Alignment.CenterStart,
            ) {
                MonthSelector(monthKey = monthKey, onMonthChange = viewModel::setMonth)
            }

            when (val s = state) {
                is UiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is UiState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { Text(s.message) }

                is UiState.Success -> InsightsBody(s.data, currency)
            }
        }
    }
}

@Composable
private fun InsightsBody(insight: MonthlyInsight, currencyCode: String) {
    if (insight.totalMinor == 0L && insight.budgetUsage.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.Insights,
            title = "No data yet",
            subtitle = "Add expenses or set budgets to see insights",
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    val largestCategory = insight.byCategory.maxByOrNull { it.value }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                StatCard(
                    label = "Total spent",
                    value = insight.totalMinor.toMoney(currencyCode),
                    icon = Icons.Outlined.TrendingUp,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Top category",
                    value = largestCategory?.key?.displayName ?: "—",
                    icon = Icons.Outlined.PieChart,
                    accent = largestCategory?.key?.style()?.color ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (insight.byCategory.isNotEmpty()) {
            item { SectionTitle("By category") }
            val sorted = insight.byCategory.entries.sortedByDescending { it.value }
            items(sorted, key = { "cat-${it.key.name}" }) { entry ->
                CategoryBreakdownRow(
                    category = entry.key,
                    amountMinor = entry.value,
                    totalMinor = insight.totalMinor,
                    currencyCode = currencyCode,
                )
            }
        }

        if (insight.budgetUsage.isNotEmpty()) {
            item { SectionTitle("Budget usage") }
            items(insight.budgetUsage, key = { "budget-${it.category.name}" }) { usage ->
                BudgetUsageRow(usage, currencyCode)
            }
        }

        item { Spacer(Modifier.height(Spacing.xxxl)) }
    }
}

@Composable
private fun CategoryBreakdownRow(
    category: Category,
    amountMinor: Long,
    totalMinor: Long,
    currencyCode: String,
) {
    val fraction = if (totalMinor == 0L) 0f else (amountMinor.toFloat() / totalMinor).coerceIn(0f, 1f)
    val color = category.style().color
    Surface(
        shape = RoundedCornerShape(Shapes.Lg),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryAvatar(category, size = 36.dp)
                Spacer(Modifier.padding(end = Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(category.displayName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${(fraction * 100).toInt()}% of total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PrimaryAmount(
                    amountMinor.toMoney(currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            ProgressTrack(fraction = fraction, color = color)
        }
    }
}

@Composable
private fun BudgetUsageRow(usage: BudgetUsage, currencyCode: String) {
    val pct = usage.percentUsed.coerceAtLeast(0f)
    val barColor = when {
        pct >= 1f -> MaterialTheme.colorScheme.error
        pct >= 0.8f -> MaterialTheme.colorScheme.tertiary
        else -> usage.category.style().color
    }
    val statusLabel = when {
        pct >= 1f -> "Over budget"
        pct >= 0.8f -> "Watch out"
        else -> "On track"
    }
    Surface(
        shape = RoundedCornerShape(Shapes.Lg),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryAvatar(usage.category, size = 36.dp)
                Spacer(Modifier.padding(end = Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(usage.category.displayName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = barColor,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    PrimaryAmount(
                        usage.spentMinor.toMoney(currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "of ${usage.limitMinor.toMoney(currencyCode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(Spacing.sm))
            ProgressTrack(fraction = pct.coerceAtMost(1f), color = barColor)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "${(pct * 100).toInt()}% used",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
