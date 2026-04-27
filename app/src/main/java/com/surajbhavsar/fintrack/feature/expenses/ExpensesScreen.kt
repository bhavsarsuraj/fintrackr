package com.surajbhavsar.fintrack.feature.expenses

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surajbhavsar.fintrack.core.common.toDisplayDate
import com.surajbhavsar.fintrack.core.common.toMoney
import com.surajbhavsar.fintrack.core.designsystem.CategoryAvatar
import com.surajbhavsar.fintrack.core.designsystem.CategoryFilterChips
import com.surajbhavsar.fintrack.core.designsystem.EmptyStateView
import com.surajbhavsar.fintrack.core.designsystem.MonthSelector
import com.surajbhavsar.fintrack.core.designsystem.PrimaryAmount
import com.surajbhavsar.fintrack.core.designsystem.Shapes
import com.surajbhavsar.fintrack.core.designsystem.Spacing
import com.surajbhavsar.fintrack.core.designsystem.style
import com.surajbhavsar.fintrack.domain.model.Expense

@Composable
fun ExpensesScreen(
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    viewModel: ExpensesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add expense") },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            HeaderArea(
                monthKey = state.monthKey,
                totalMinor = state.totalMinor,
                currencyCode = state.currencyCode,
                expenseCount = state.expenses.size,
                onMonthChange = viewModel::setMonth,
            )

            CategoryFilterChips(
                selected = state.categoryFilter,
                onSelect = viewModel::setCategoryFilter,
                modifier = Modifier.padding(vertical = Spacing.md),
            )

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                state.expenses.isEmpty() -> EmptyStateView(
                    icon = Icons.Outlined.Receipt,
                    title = if (state.categoryFilter == null) "No expenses this month" else "No ${state.categoryFilter?.displayName} expenses",
                    subtitle = "Tap Add expense to get started",
                    modifier = Modifier.fillMaxSize(),
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.lg, end = Spacing.lg,
                        top = Spacing.xs, bottom = Spacing.xxxl + 56.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(state.expenses, key = { it.id }) { expense ->
                        ExpenseRow(
                            expense = expense,
                            currencyCode = state.currencyCode,
                            onClick = { onEditExpense(expense.id) },
                            onDelete = { viewModel.onDelete(expense.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderArea(
    monthKey: String,
    totalMinor: Long,
    currencyCode: String,
    expenseCount: Int,
    onMonthChange: (String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "FinTrackr",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        "Your expenses, neatly tracked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            BalanceCard(totalMinor, currencyCode, expenseCount, monthKey, onMonthChange)
        }
    }
}

@Composable
private fun BalanceCard(
    totalMinor: Long,
    currencyCode: String,
    expenseCount: Int,
    monthKey: String,
    onMonthChange: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(Shapes.Xl),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
                Spacer(Modifier.padding(end = Spacing.sm))
                Text(
                    "Spent this period",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                totalMinor.toMoney(currencyCode),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "$expenseCount transaction${if (expenseCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
            )
            Spacer(Modifier.height(Spacing.lg))
            MonthSelector(monthKey = monthKey, onMonthChange = onMonthChange)
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    currencyCode: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(Shapes.Lg),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(expense.category, size = 44.dp)
            Spacer(Modifier.padding(end = Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    "${expense.category.displayName} · ${expense.occurredAt.toDisplayDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PrimaryAmount(
                amountText = expense.amountMinor.toMoney(currencyCode),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
