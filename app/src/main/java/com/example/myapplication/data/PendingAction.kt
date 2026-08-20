package com.example.myapplication.data

import kotlinx.serialization.Serializable

@Serializable
data class PendingAction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tableName: String,
    val operation: String, // "INSERT", "UPDATE", "DELETE"
    val jsonData: String,
    val filterColumn: String? = null,
    val filterValue: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
