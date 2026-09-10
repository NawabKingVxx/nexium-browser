package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.NexiumApplication
import com.example.browser.TabModel
import com.example.browser.UrlHelper
import com.example.ui.viewmodel.BrowserViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NexiumWebViewComponent(
    tab: TabModel,
    viewModel: BrowserViewModel,
    onOpenFileChooser: (ValueCallback<Array<Uri>>?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as NexiumApplication
    val preferences = app.preferences
    val adBlockEngine = app.adBlockEngine
    val extensionEngine = remember { com.example.browser.ExtensionEngine(app.database.extensionDao(), app.applicationScope) }

    val webView = remember(tab.id, tab.webView) {
        val existing = tab.webView
        if (existing != null) {
            (existing.parent as? ViewGroup)?.removeView(existing)
            existing
        } else {
            WebView(context).apply {
                tab.webView = this
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
                    } catch (e: Exception) {}
                }

                settings.apply {
                    javaScriptEnabled = preferences.javascriptEnabled.value
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    mediaPlaybackRequiresUserGesture = false
                    builtInZoomControls = true
                    displayZoomControls = false
                    setSupportZoom(true)
                    textZoom = preferences.textZoom.value
                    setGeolocationEnabled(true)
                    javaScriptCanOpenWindowsAutomatically = !preferences.popupsBlocked.value
                    setSupportMultipleWindows(true)
                    useWideViewPort = tab.desktopMode
                    loadWithOverviewMode = tab.desktopMode

                    if (tab.desktopMode) {
                        userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            safeBrowsingEnabled = preferences.safeBrowsing.value
                        } catch (e: Exception) {}
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    }
                }

            CookieManager.getInstance().setAcceptCookie(preferences.cookiesEnabled.value)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, preferences.cookiesEnabled.value && !tab.isIncognito)
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    viewModel.updateTabLoading(tab, newProgress < 100, newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    if (!title.isNullOrBlank()) {
                        viewModel.updateTabMetadata(tab, title, null, null)
                    }
                }

                override fun onReceivedIcon(view: WebView?, icon: android.graphics.Bitmap?) {
                    super.onReceivedIcon(view, icon)
                    if (icon != null) {
                        viewModel.updateTabMetadata(tab, null, null, icon)
                    }
                }

                override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                    if (view != null && callback != null) {
                        viewModel.showCustomVideoView(view, callback)
                    }
                }

                override fun onHideCustomView() {
                    viewModel.hideCustomVideoView()
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    viewModel.filePathCallback = filePathCallback
                    onOpenFileChooser(filePathCallback)
                    return true
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {
                    callback?.invoke(origin, true, false)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false

                    // Handle non-web schemes (tel, mailto, intent, etc.)
                    if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("about:") && !url.startsWith("nexium:")) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            return true
                        } catch (e: Exception) {
                            return true
                        }
                    }

                    // Force HTTPS if preferred
                    if (preferences.forceHttps.value && url.startsWith("http://")) {
                        val httpsUrl = url.replaceFirst("http://", "https://")
                        view?.loadUrl(httpsUrl)
                        return true
                    }

                    return false
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val reqUrl = request?.url?.toString() ?: return null
                    val currentHost = tab.url.let { UrlHelper.extractDomain(it) }

                    if (adBlockEngine.shouldBlock(reqUrl, currentHost)) {
                        return adBlockEngine.createBlockedResponse()
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    viewModel.updateTabLoading(tab, true, 10)
                    viewModel.updateTabMetadata(tab, null, url, favicon)
                    viewModel.updateTabNavState(tab, view?.canGoBack() == true, view?.canGoForward() == true)
                    viewModel.updateTabError(tab, false, null)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewModel.updateTabLoading(tab, false, 100)
                    viewModel.updateTabMetadata(tab, view?.title, url, null)
                    viewModel.updateTabNavState(tab, view?.canGoBack() == true, view?.canGoForward() == true)

                    // Inject Do Not Track header script if enabled
                    if (preferences.doNotTrack.value) {
                        view?.evaluateJavascript(
                            "try { Object.defineProperty(navigator, 'doNotTrack', { value: '1', configurable: false, writable: false }); } catch(e) {}",
                            null
                        )
                    }

                    // Inject active extensions
                    if (view != null && url != null) {
                        extensionEngine.injectExtensionsInto(view, url)
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        viewModel.updateTabError(
                            tab,
                            true,
                            error?.description?.toString() ?: "Failed to load page"
                        )
                    }
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    val errorDescription = when (error?.primaryError) {
                        SslError.SSL_EXPIRED -> "Security certificate has expired."
                        SslError.SSL_IDMISMATCH -> "Hostname mismatch in certificate."
                        SslError.SSL_UNTRUSTED -> "Untrusted Certificate Authority."
                        SslError.SSL_NOTYETVALID -> "Certificate is not yet valid."
                        else -> "SSL Certificate Validation Error."
                    }

                    viewModel.showSslError(
                        url = error?.url ?: tab.url,
                        primaryError = errorDescription,
                        onProceed = {
                            handler?.proceed()
                            viewModel.dismissSslError()
                        },
                        onCancel = {
                            handler?.cancel()
                            viewModel.dismissSslError()
                        }
                    )
                }

                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?
                ): Boolean {
                    val didCrash = detail?.didCrash() ?: false
                    val errorMsg = if (didCrash) {
                        "Web renderer terminated unexpectedly. Tap Retry to reload."
                    } else {
                        "Web renderer memory reclaimed by system. Tap Retry to reload."
                    }
                    val targetView = view ?: tab.webView
                    (targetView?.parent as? ViewGroup)?.removeView(targetView)
                    try {
                        targetView?.stopLoading()
                        targetView?.clearHistory()
                        targetView?.loadUrl("about:blank")
                        targetView?.onPause()
                        targetView?.removeAllViews()
                        targetView?.destroy()
                    } catch (e: Exception) {}
                    tab.webView = null
                    viewModel.updateTabError(tab, true, errorMsg)
                    return true
                }
            }

            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                viewModel.downloadManager.enqueueDownload(url, contentDisposition, mimeType, userAgent)
            }

            if (tab.url.isNotBlank() && tab.url != "nexium://home") {
                loadUrl(tab.url)
            }
        }
    }
}

    AndroidView(
        factory = {
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView
        },
        update = { view ->
            if (tab.url.isNotBlank() && tab.url != "nexium://home" && view.url != tab.url && (view.url == null || view.url == "about:blank")) {
                view.loadUrl(tab.url)
            }
        },
        modifier = modifier
    )

    DisposableEffect(tab.id) {
        onDispose {
            (webView.parent as? ViewGroup)?.removeView(webView)
        }
    }
}
