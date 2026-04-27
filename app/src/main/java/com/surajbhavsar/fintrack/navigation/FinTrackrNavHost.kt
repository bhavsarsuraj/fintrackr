package com.surajbhavsar.fintrack.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.surajbhavsar.fintrack.feature.budgets.BudgetsScreen
import com.surajbhavsar.fintrack.feature.expenses.ExpensesScreen
import com.surajbhavsar.fintrack.feature.expenses.edit.ExpenseEditScreen
import com.surajbhavsar.fintrack.feature.insights.InsightsScreen
import com.surajbhavsar.fintrack.feature.recurring.RecurringRulesScreen
import com.surajbhavsar.fintrack.feature.settings.SettingsScreen

private data class BottomDest(
    val route: String,
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDest(Routes.EXPENSES, "Expenses", Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomDest(Routes.BUDGETS, "Budgets", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    BottomDest(Routes.INSIGHTS, "Insights", Icons.Filled.Insights, Icons.Outlined.Insights),
    BottomDest(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun FinTrackrNavHost(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = bottomDestinations.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    bottomDestinations.forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(Routes.EXPENSES) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) dest.iconSelected else dest.iconUnselected,
                                    contentDescription = dest.label,
                                )
                            },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.EXPENSES,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.EXPENSES) {
                ExpensesScreen(
                    onAddExpense = { navController.navigate(Routes.expenseEditRoute()) },
                    onEditExpense = { id -> navController.navigate(Routes.expenseEditRoute(id)) },
                )
            }
            composable(
                route = "${Routes.EXPENSE_EDIT}?${Routes.EXPENSE_EDIT_ARG}={${Routes.EXPENSE_EDIT_ARG}}",
                arguments = listOf(navArgument(Routes.EXPENSE_EDIT_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }),
            ) {
                ExpenseEditScreen(onClose = { navController.popBackStack() })
            }
            composable(Routes.BUDGETS) { BudgetsScreen() }
            composable(Routes.INSIGHTS) { InsightsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenRecurring = { navController.navigate(Routes.RECURRING) },
                )
            }
            composable(Routes.RECURRING) {
                RecurringRulesScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

