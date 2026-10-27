package com.expensetracker.data.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIcons {
    val iconMap = mapOf<String, ImageVector>(
        "restaurant" to Icons.Default.Restaurant,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "directions_car" to Icons.Default.DirectionsCar,
        "shopping_bag" to Icons.Default.ShoppingBag,
        "receipt_long" to Icons.Default.ReceiptLong,
        "movie" to Icons.Default.Movie,
        "medical_services" to Icons.Default.MedicalServices,
        "school" to Icons.Default.School,
        "flight" to Icons.Default.Flight,
        "spa" to Icons.Default.Spa,
        "more_horiz" to Icons.Default.MoreHoriz,
        "payments" to Icons.Default.Payments,
        "work" to Icons.Default.Work,
        "trending_up" to Icons.Default.TrendingUp,
        "home" to Icons.Default.Home,
        "card_giftcard" to Icons.Default.CardGiftcard,
        "attach_money" to Icons.Default.AttachMoney,
        "account_balance" to Icons.Default.AccountBalance,
        "credit_card" to Icons.Default.CreditCard,
        "account_balance_wallet" to Icons.Default.AccountBalanceWallet,
        "fitness_center" to Icons.Default.FitnessCenter,
        "local_gas_station" to Icons.Default.LocalGasStation,
        "fastfood" to Icons.Default.Fastfood,
        "pets" to Icons.Default.Pets,
        "sports_esports" to Icons.Default.SportsEsports,
        "phone_android" to Icons.Default.PhoneAndroid,
        "wifi" to Icons.Default.Wifi,
        "local_cafe" to Icons.Default.LocalCafe
    )

    fun getIcon(name: String?): ImageVector {
        return iconMap[name] ?: Icons.Default.Category
    }

    val availableIconsList = iconMap.keys.toList()

    val availableColors = listOf(
        "#FF5722", "#4CAF50", "#2196F3", "#E91E63", "#FF9800",
        "#9C27B0", "#F44336", "#3F51B5", "#00BCD4", "#009688",
        "#795548", "#2E7D32", "#1565C0", "#6A1B9A", "#E65100",
        "#607D8B", "#FFEB3B", "#8BC34A", "#03A9F4", "#673AB7"
    )
}
