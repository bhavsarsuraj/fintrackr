package com.surajbhavsar.fintrack.domain.model

enum class Category(val displayName: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    GROCERIES("Groceries"),
    OTHER("Other");

    companion object {
        fun fromName(name: String?): Category =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
