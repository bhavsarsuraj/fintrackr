package com.surajbhavsar.fintrack.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.surajbhavsar.fintrack.domain.model.Category

data class CategoryStyle(val color: Color, val icon: ImageVector)

fun Category.style(): CategoryStyle = when (this) {
    Category.FOOD -> CategoryStyle(CatFood, Icons.Outlined.LocalDining)
    Category.TRANSPORT -> CategoryStyle(CatTransport, Icons.Outlined.DirectionsCar)
    Category.SHOPPING -> CategoryStyle(CatShopping, Icons.Outlined.ShoppingBag)
    Category.BILLS -> CategoryStyle(CatBills, Icons.Outlined.ReceiptLong)
    Category.ENTERTAINMENT -> CategoryStyle(CatEntertainment, Icons.Outlined.MovieCreation)
    Category.HEALTH -> CategoryStyle(CatHealth, Icons.Outlined.LocalHospital)
    Category.GROCERIES -> CategoryStyle(CatGroceries, Icons.Outlined.ShoppingBasket)
    Category.OTHER -> CategoryStyle(CatOther, Icons.Outlined.MoreHoriz)
}
