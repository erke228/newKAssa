package com.example.myapplication.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

enum class ConsoleType {
    COMMON, VIP
}

enum class ConsoleStatus {
    FREE, BUSY, BOOKED
}

@Serializable
data class InventoryItem(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int = 0,
    @SerialName("cost_price") val costPrice: Double = 0.0
)

@Serializable
data class ExtraItem(
    val name: String,
    val price: Double,
    @SerialName("cost_price") val costPrice: Double = 0.0
)

@Serializable
data class Session(
    @SerialName("start_time") val startTime: String = "",
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("hourly_rate") val hourlyRate: Double = 0.0,
    @SerialName("fixed_price") val fixedPrice: Double? = null,
    @SerialName("package_name") val packageName: String? = null,
    val extras: List<ExtraItem> = emptyList(),
    @SerialName("is_paid") val isPaid: Boolean = false,
    @SerialName("notified_five_min") val notifiedFiveMin: Boolean = false
)

@Serializable
data class Console(
    val id: Int,
    val name: String,
    val type: ConsoleType,
    val status: ConsoleStatus = ConsoleStatus.FREE,
    @SerialName("current_session") val currentSession: Session? = null,
    @SerialName("booked_by") val bookedBy: String? = null
)

sealed class CartItem {
    abstract val price: Double
    abstract val title: String

    data class SessionCart(
        val consoleId: Int,
        val consoleName: String,
        val durationMinutes: Int?,
        val fixedPrice: Double?,
        val packageName: String?,
        override val title: String,
        override val price: Double
    ) : CartItem()

    data class BarCart(
        val itemId: Int,
        val consoleId: Int? = null,
        val quantity: Int = 1,
        override val title: String,
        override val price: Double
    ) : CartItem()
}

@Serializable
data class DailyReport(
    val id: Long,
    val date: String = "",
    val amount: Double = 0.0,
    val transactions: List<Transaction>? = emptyList()
)

@Serializable
data class TransactionItem(
    val title: String = "",
    val price: Double = 0.0,
    @SerialName("cost_price") val costPrice: Double = 0.0
)

@Serializable
data class Transaction(
    val id: Long,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("total_cost") val totalCost: Double = 0.0,
    val timestamp: String = "",
    val items: List<TransactionItem> = emptyList()
)
