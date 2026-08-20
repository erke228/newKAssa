package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.utils.NetworkObserver
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var updateManager: UpdateManager
    private lateinit var networkObserver: NetworkObserver

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        updateManager = UpdateManager(this)
        networkObserver = NetworkObserver(this)
        
        observeNetwork()
        
        // Создаем WebView программно
        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                // Оптимизация кэширования
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
            
            webChromeClient = WebChromeClient()
        }

        setContentView(webView)

        // Загрузка контента
        loadWebContent()

        // Фоновая проверка обновлений
        lifecycleScope.launch {
            updateManager.checkForUpdates()
        }
    }

    private fun loadWebContent() {
        val otaPath = updateManager.getLocalIndexPath()
        if (otaPath != null) {
            webView.loadUrl(otaPath)
        } else {
            // Если обновлений нет, грузим из Assets
            webView.loadUrl("file:///android_asset/index.html")
        }
    }

    private fun observeNetwork() {
        lifecycleScope.launch {
            networkObserver.isConnected.collectLatest { isConnected ->
                if (!isConnected) {
                    Toast.makeText(this@MainActivity, "Вы вне сети. Приложение работает в автономном режиме.", Toast.LENGTH_LONG).show()
                } else {
                    // Можно добавить уведомление о восстановлении связи, если нужно
                }
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
