package com.surajbhavsar.fintrack.navigation

object Routes {
    const val EXPENSES = "expenses"
    const val EXPENSE_EDIT = "expense_edit"
    const val EXPENSE_EDIT_ARG = "expenseId"
    const val BUDGETS = "budgets"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"
    const val RECURRING = "recurring"

    fun expenseEditRoute(id: String? = null): String =
        if (id == null) EXPENSE_EDIT else "$EXPENSE_EDIT?$EXPENSE_EDIT_ARG=$id"
}
