package com.example.browser

import android.graphics.Bitmap
import android.webkit.WebView
import java.util.UUID

data class TabModel(
    val id: String = UUID.randomUUID().toString(),
    var url: String = "nexium://home",
    var title: String = "New Tab",
    val isIncognito: Boolean = false,
    var progress: Int = 0,
    var isLoading: Boolean = false,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var desktopMode: Boolean = false,
    var isError: Boolean = false,
    var errorDescription: String? = null,
    var favicon: Bitmap? = null,
    var webView: WebView? = null,
    val createdAt: Long = System.currentTimeMillis()
)
