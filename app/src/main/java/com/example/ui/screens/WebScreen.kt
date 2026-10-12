package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebScreen(modifier: Modifier = Modifier) {
  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
      WebView(context).apply {
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          databaseEnabled = true
          cacheMode = WebSettings.LOAD_DEFAULT
          allowFileAccess = true
          allowContentAccess = true
          loadWithOverviewMode = true
          useWideViewPort = true
        }
        webViewClient = WebViewClient()
        webChromeClient = WebChromeClient()
        loadUrl("file:///android_asset/index.html")
      }
    }
  )
}
