package com.surajbhavsar.fintrack.feature.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surajbhavsar.fintrack.core.common.toMoney
import com.surajbhavsar.fintrack.core.designsystem.CategoryAvatar
import com.surajbhavsar.fintrack.core.designsystem.EmptyStateView
import com.surajbhavsar.fintrack.core.designsystem.MonthSelector
import com.surajbhavsar.fintrack.core.designsystem.PrimaryAmount
import com.surajbhavsar.fintrack.core.designsystem.Shapes
import com.surajbhavsar.fintrack.core.designsystem.Spacing
import com.surajbhavsar.fintrack.core.designsystem.style
import com.surajbhavsar.fintrack.domain.model.Budget
import com.surajbhavsar.fintrack.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(viewModel: BudgetsViewModel = hiltViewModel()) {
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
        topBar = {
            TopAppBar(
                title = { Text("Budgets", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::startAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New budget") },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                contentAlignment = Alignment.CenterStart,
            ) {
                MonthSelector(monthKey = state.monthKey, onMonthChange = viewModel::setMonth)
            }
            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                state.budgets.isEmpty() -> EmptyStateView(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = "No budgets set",
                    subtitle = "Set monthly limits per category to stay on track",
                    actionLabel = "Add budget",
                    onAction = viewModel::startAdd,
                    modifier = Modifier.fillMaxSize(),
                )

                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(state.budgets, key = { it.id }) { budget ->
                        BudgetRow(
                            budget = budget,
                            currencyCode = state.currencyCode,
                            onClick = { viewModel.startEdit(budget) },
                            onDelete = { viewModel.delete(budget.id) },
                        )
                    }
                    item { Spacer(Modifier.padding(Spacing.xxl)) }
                }
            }
        }
    }

    state.editor?.let { editor ->
        BudgetEditorDialog(editor, viewModel)
    }
}

@Composable
private fun BudgetRow(
    budget: Budget,
    currencyCode: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(Shapes.Lg),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(budget.category, size = 44.dp)
            Spacer(Modifier.padding(end = Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(budget.category.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Monthly limit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PrimaryAmount(
                budget.limitMinor.toMoney(currencyCode),
                style = MaterialTheme.typography.titleMedium,
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

@Composable
private fun BudgetEditorDialog(editor: BudgetEditor, viewModel: BudgetsViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::cancelEditor,
        title = { Text(if (editor.existingId == null) "New budget" else "Edit budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(Category.entries) { category ->
                        val style = category.style()
                        FilterChip(
                            selected = editor.category == category,
                            onClick = { viewModel.onEditorCategory(category) },
                            leadingIcon = { Icon(style.icon, contentDescription = null, modifier = Modifier.padding(2.dp)) },
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
                OutlinedTextField(
                    value = editor.amount,
                    onValueChange = viewModel::onEditorAmount,
                    label = { Text("Monthly limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Shapes.Md),
                )
            }
        },
        confirmButton = { TextButton(onClick = viewModel::saveEditor) { Text("Save") } },
        dismissButton = { TextButton(onClick = viewModel::cancelEditor) { Text("Cancel") } },
    )
}
