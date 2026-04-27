package com.surajbhavsar.fintrack.feature.recurring

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surajbhavsar.fintrack.core.common.toDisplayDate
import com.surajbhavsar.fintrack.core.common.toMoney
import com.surajbhavsar.fintrack.core.designsystem.CategoryAvatar
import com.surajbhavsar.fintrack.core.designsystem.EmptyStateView
import com.surajbhavsar.fintrack.core.designsystem.PrimaryAmount
import com.surajbhavsar.fintrack.core.designsystem.Shapes
import com.surajbhavsar.fintrack.core.designsystem.Spacing
import com.surajbhavsar.fintrack.core.designsystem.style
import com.surajbhavsar.fintrack.domain.model.Category
import com.surajbhavsar.fintrack.domain.model.Frequency
import com.surajbhavsar.fintrack.domain.model.RecurringRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringRulesScreen(
    onBack: () -> Unit,
    viewModel: RecurringRulesViewModel = hiltViewModel(),
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
        topBar = {
            TopAppBar(
                title = { Text("Recurring expenses", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::startAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New rule") },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.rules.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding)) {
                EmptyStateView(
                    icon = Icons.Outlined.Schedule,
                    title = "No recurring rules",
                    subtitle = "Auto-create expenses on a schedule, like monthly rent",
                    actionLabel = "Add rule",
                    onAction = viewModel::startAdd,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(state.rules, key = { it.id }) { rule ->
                    RuleRow(
                        rule = rule,
                        currencyCode = state.currencyCode,
                        onDelete = { viewModel.delete(rule.id) },
                    )
                }
                item { Spacer(Modifier.padding(Spacing.xxl)) }
            }
        }
    }

    state.editor?.let { editor -> RuleEditorDialog(editor, viewModel) }
}

@Composable
private fun RuleRow(rule: RecurringRule, currencyCode: String, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Shapes.Lg),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(rule.category, size = 44.dp)
            Spacer(Modifier.padding(end = Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(rule.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${rule.frequency.label()} · next ${rule.nextRunAt.toDisplayDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PrimaryAmount(
                rule.amountMinor.toMoney(currencyCode),
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

private fun Frequency.label(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuleEditorDialog(editor: RuleEditor, viewModel: RecurringRulesViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = viewModel::cancelEditor,
        title = { Text("New recurring expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = editor.title,
                    onValueChange = viewModel::onTitle,
                    label = { Text("Title (e.g. Rent)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Shapes.Md),
                )
                OutlinedTextField(
                    value = editor.amount,
                    onValueChange = viewModel::onAmount,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Shapes.Md),
                )
                Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(Category.entries) { c ->
                        val style = c.style()
                        FilterChip(
                            selected = editor.category == c,
                            onClick = { viewModel.onCategory(c) },
                            leadingIcon = { Icon(style.icon, contentDescription = null, modifier = Modifier.padding(2.dp)) },
                            label = { Text(c.displayName) },
                            shape = RoundedCornerShape(Shapes.Pill),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = style.color.copy(alpha = 0.15f),
                                selectedLabelColor = style.color,
                                selectedLeadingIconColor = style.color,
                            ),
                        )
                    }
                }
                Text("Frequency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Frequency.entries.forEach { f ->
                        FilterChip(
                            selected = editor.frequency == f,
                            onClick = { viewModel.onFrequency(f) },
                            label = { Text(f.label()) },
                            shape = RoundedCornerShape(Shapes.Pill),
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(Shapes.Md),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                ) {
                    Column(Modifier.padding(Spacing.md)) {
                        Text("Next run", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(editor.nextRunAt.toDisplayDate(), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::saveEditor) { Text("Save") } },
        dismissButton = { TextButton(onClick = viewModel::cancelEditor) { Text("Cancel") } },
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = editor.nextRunAt)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(viewModel::onNextRunAt)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
