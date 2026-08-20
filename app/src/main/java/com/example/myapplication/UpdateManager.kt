package com.example.myapplication

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

class UpdateManager(private val context: Context) {
    private val TAG = "UpdateManager"
    
    // TODO: Замените на ваш реальный URL репозитория
    private val GITHUB_RAW_URL = "https://raw.githubusercontent.com/vitsa-club/vitsa-app/main"
    private val VERSION_URL = "$GITHUB_RAW_URL/version.json"
    
    private val PREFS_NAME = "vitsa_ota_prefs"
    private val KEY_VERSION = "current_version"
    
    private val wwwDir = File(context.filesDir, "www")

    init {
        if (!wwwDir.exists()) {
            wwwDir.mkdirs()
        }
    }

    /**
     * Возвращает путь к файлу index.html. 
     * Если во внутренней памяти есть обновленная версия - возвращает её, 
     * иначе null (нужно грузить из Assets).
     */
    fun getLocalIndexPath(): String? {
        val indexFile = File(wwwDir, "index.html")
        return if (indexFile.exists()) {
            "file://${indexFile.absolutePath}"
        } else {
            null
        }
    }

    suspend fun checkForUpdates() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking for updates at $VERSION_URL")
            val connection = URL(VERSION_URL).openConnection()
            connection.connectTimeout = 5000
            val jsonText = connection.getInputStream().bufferedReader().use { it.readText() }
            val json = JSONObject(jsonText)
            
            val remoteVersion = json.getInt("version")
            val currentVersion = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_VERSION, 0)

            Log.d(TAG, "Current version: $currentVersion, Remote version: $remoteVersion")

            if (remoteVersion > currentVersion) {
                Log.d(TAG, "New version found. Starting download...")
                downloadUpdates(json)
                
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(KEY_VERSION, remoteVersion)
                    .apply()
                
                Log.d(TAG, "Update completed successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update check failed: ${e.message}")
        }
    }

    private fun downloadUpdates(json: JSONObject) {
        val files = json.getJSONArray("files")
        for (i in 0 until files.length()) {
            val fileName = files.getString(i)
            val fileUrl = "$GITHUB_RAW_URL/$fileName"
            val targetFile = File(wwwDir, fileName)
            
            try {
                Log.d(TAG, "Downloading $fileName from $fileUrl")
                URL(fileUrl).openStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download $fileName: ${e.message}")
            }
        }
    }
}
