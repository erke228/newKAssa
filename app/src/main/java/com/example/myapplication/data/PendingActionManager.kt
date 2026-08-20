package com.example.myapplication.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PendingActionManager(context: Context) {
    private val file = File(context.filesDir, "pending_actions.json")
    private val json = Json { ignoreUnknownKeys = true }

    fun saveAction(action: PendingAction) {
        val actions = getAllPending().toMutableList()
        actions.add(action)
        file.writeText(json.encodeToString(actions))
    }

    fun getAllPending(): List<PendingAction> {
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString<List<PendingAction>>(file.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteAction(id: String) {
        val actions = getAllPending().filter { it.id != id }
        file.writeText(json.encodeToString(actions))
    }

    fun clear() {
        if (file.exists()) file.delete()
    }
}
