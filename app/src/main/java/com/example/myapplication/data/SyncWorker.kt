package com.example.myapplication.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val SUPABASE_URL = "https://htkoyibefvqvaisbolga.supabase.co"
    private val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh0a295aWJlZnZxdmFpc2JvbGdhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU4NzQzNjMsImV4cCI6MjEwMTQ1MDM2M30._ETmzJgURtwBpv5anL760wURMpTt_oPSDeR49mE67-Q"

    private val client = createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
        install(Postgrest)
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val manager = PendingActionManager(applicationContext)
        val actions = manager.getAllPending()
        
        if (actions.isEmpty()) return Result.success()

        for (action in actions) {
            try {
                val table = client.from(action.tableName)
                val element = json.parseToJsonElement(action.jsonData)

                when (action.operation) {
                    "INSERT" -> {
                        table.insert(element.jsonObject)
                    }
                    "UPDATE" -> {
                        if (action.filterColumn != null && action.filterValue != null) {
                            table.update(element.jsonObject) {
                                filter {
                                    eq(action.filterColumn, action.filterValue)
                                }
                            }
                        }
                    }
                    "DELETE" -> {
                        if (action.filterColumn != null && action.filterValue != null) {
                            table.delete {
                                filter {
                                    eq(action.filterColumn, action.filterValue)
                                }
                            }
                        }
                    }
                }
                manager.deleteAction(action.id)
            } catch (e: Exception) {
                e.printStackTrace()
                return Result.retry()
            }
        }

        return Result.success()
    }
}
