package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NexiumApplication
import com.example.browser.NexiumDownloadManager
import com.example.browser.SearchEngine
import com.example.browser.TabModel
import com.example.browser.UrlHelper
import com.example.data.BookmarkEntity
import com.example.data.HistoryEntity
import com.example.data.QuickShortcutEntity
import com.example.data.SavedTabEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Stack

enum class CurrentSheet {
    NONE,
    TABS_OVERVIEW,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    VPN,
    AD_BLOCKER,
    EXTENSIONS,
    SETTINGS,
    PERMISSIONS,
    ABOUT,
    PRIVACY_POLICY,
    CLEAR_DATA_DIALOG,
    ADD_BOOKMARK_DIALOG,
    ADD_SHORTCUT_DIALOG,
    ADD_EXTENSION_DIALOG
}

data class FindInPageState(
    val isActive: Boolean = false,
    val query: String = "",
    val activeMatchIndex: Int = 0,
    val totalMatches: Int = 0
)

data class SslErrorState(
    val isShowing: Boolean = false,
    val url: String = "",
    val primaryError: String = "",
    val onProceed: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NexiumApplication
    private val db = app.database
    val preferences = app.preferences
    val adBlockEngine = app.adBlockEngine
    val downloadManager = NexiumDownloadManager(application, db.downloadDao(), viewModelScope)

    // Tabs
    private val _tabs = MutableStateFlow<List<TabModel>>(emptyList())
    val tabs: StateFlow<List<TabModel>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>("")
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: TabModel?
        get() = _tabs.value.firstOrNull { it.id == _activeTabId.value } ?: _tabs.value.firstOrNull()

    private val closedTabsStack = Stack<TabModel>()

    // Navigation and URL bar
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isEditingUrl = MutableStateFlow(false)
    val isEditingUrl: StateFlow<Boolean> = _isEditingUrl.asStateFlow()

    // Active BottomSheet / Dialog
    private val _currentSheet = MutableStateFlow(CurrentSheet.NONE)
    val currentSheet: StateFlow<CurrentSheet> = _currentSheet.asStateFlow()

    // Fullscreen video
    private val _customVideoView = MutableStateFlow<View?>(null)
    val customVideoView: StateFlow<View?> = _customVideoView.asStateFlow()
    var customViewCallback: WebChromeClient.CustomViewCallback? = null

    // File upload callback
    var filePathCallback: ValueCallback<Array<Uri>>? = null

    // Find in Page
    private val _findInPage = MutableStateFlow(FindInPageState())
    val findInPage: StateFlow<FindInPageState> = _findInPage.asStateFlow()

    // SSL Error dialog
    private val _sslError = MutableStateFlow(SslErrorState())
    val sslError: StateFlow<SslErrorState> = _sslError.asStateFlow()

    // Room Flows
    val bookmarks: StateFlow<List<BookmarkEntity>> = db.bookmarkDao().getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = db.historyDao().getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyVisited: StateFlow<List<HistoryEntity>> = db.historyDao().getRecentlyVisited(8)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostVisited: StateFlow<List<HistoryEntity>> = db.historyDao().getMostVisited(8)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortcuts: StateFlow<List<QuickShortcutEntity>> = db.shortcutDao().getAllShortcuts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val extensions = db.extensionDao().getAllExtensions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adBlockWhitelist = db.adBlockDao().getAllWhitelist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarked state for active tab
    private val _isCurrentPageBookmarked = MutableStateFlow(false)
    val isCurrentPageBookmarked: StateFlow<Boolean> = _isCurrentPageBookmarked.asStateFlow()

    init {
        restoreSavedTabs()
    }

    private fun restoreSavedTabs() {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = db.savedTabDao().getAllSavedTabs()
            withContext(Dispatchers.Main) {
                if (saved.isNotEmpty()) {
                    val list = saved.map {
                        TabModel(
                            id = it.id,
                            url = it.url,
                            title = it.title,
                            isIncognito = false
                        )
                    }
                    _tabs.value = list
                    _activeTabId.value = list.first().id
                    _urlInput.value = if (list.first().url == "nexium://home") "" else list.first().url
                } else {
                    createNewTab(isIncognito = false, url = "nexium://home")
                }
            }
        }
    }

    private fun saveTabsToDb() {
        val standardTabs = _tabs.value.filter { !it.isIncognito }
        viewModelScope.launch(Dispatchers.IO) {
            db.savedTabDao().clearAll()
            val entities = standardTabs.mapIndexed { index, tab ->
                SavedTabEntity(
                    id = tab.id,
                    url = tab.url,
                    title = tab.title,
                    isIncognito = false,
                    position = index
                )
            }
            db.savedTabDao().insertAll(entities)
        }
    }

    fun createNewTab(isIncognito: Boolean = false, url: String = "nexium://home"): TabModel {
        val newTab = TabModel(
            url = url,
            title = if (url == "nexium://home") (if (isIncognito) "Incognito Tab" else "New Tab") else url,
            isIncognito = isIncognito
        )
        val updated = _tabs.value + newTab
        _tabs.value = updated
        _activeTabId.value = newTab.id
        _urlInput.value = if (url == "nexium://home") "" else url
        _isEditingUrl.value = false
        checkIfCurrentBookmarked(url)
        saveTabsToDb()
        return newTab
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        val tabToClose = currentList.firstOrNull { it.id == tabId } ?: return

        // Push to closed stack for reopen
        if (!tabToClose.isIncognito && tabToClose.url != "nexium://home") {
            closedTabsStack.push(tabToClose.copy(webView = null))
        }

        // Clean up webview safely
        safelyDestroyWebView(tabToClose.webView)
        tabToClose.webView = null

        val updated = currentList.filter { it.id != tabId }
        if (updated.isEmpty()) {
            _tabs.value = emptyList()
            createNewTab(isIncognito = false, url = "nexium://home")
        } else {
            _tabs.value = updated
            if (_activeTabId.value == tabId) {
                val nextActive = updated.last()
                _activeTabId.value = nextActive.id
                _urlInput.value = if (nextActive.url == "nexium://home") "" else nextActive.url
                checkIfCurrentBookmarked(nextActive.url)
            }
        }
        saveTabsToDb()
    }

    fun closeAllTabs() {
        _tabs.value.forEach {
            safelyDestroyWebView(it.webView)
            it.webView = null
        }
        _tabs.value = emptyList()
        createNewTab(isIncognito = false, url = "nexium://home")
        saveTabsToDb()
    }

    fun selectTab(tabId: String) {
        val target = _tabs.value.firstOrNull { it.id == tabId } ?: return
        _activeTabId.value = target.id
        _urlInput.value = if (target.url == "nexium://home") "" else target.url
        checkIfCurrentBookmarked(target.url)
        _currentSheet.value = CurrentSheet.NONE
    }

    fun duplicateTab(tabId: String) {
        val tab = _tabs.value.firstOrNull { it.id == tabId } ?: return
        createNewTab(isIncognito = tab.isIncognito, url = tab.url)
    }

    fun reopenClosedTab() {
        if (!closedTabsStack.isEmpty()) {
            val tab = closedTabsStack.pop()
            createNewTab(isIncognito = false, url = tab.url)
        }
    }

    fun loadUrl(input: String) {
        val current = activeTab ?: return
        val engine = SearchEngine.fromName(preferences.searchEngine.value)
        val targetUrl = UrlHelper.resolveInputToUrl(input, engine, preferences.customSearchUrl.value)

        current.url = targetUrl
        current.isError = false
        current.errorDescription = null
        _urlInput.value = targetUrl
        _isEditingUrl.value = false

        current.webView?.loadUrl(targetUrl)
        checkIfCurrentBookmarked(targetUrl)
        triggerRecomposition()
    }

    fun goBack() {
        val current = activeTab ?: return
        if (current.webView?.canGoBack() == true) {
            current.webView?.goBack()
        } else if (current.url != "nexium://home") {
            current.url = "nexium://home"
            current.title = "New Tab"
            _urlInput.value = ""
            triggerRecomposition()
        }
    }

    fun goForward() {
        activeTab?.webView?.goForward()
    }

    fun reload() {
        val current = activeTab ?: return
        if (current.url == "nexium://home") return
        current.isError = false
        current.errorDescription = null
        if (current.webView != null) {
            current.webView?.reload()
        }
        triggerRecomposition()
    }

    fun stopLoading() {
        activeTab?.webView?.stopLoading()
    }

    fun toggleDesktopMode() {
        val current = activeTab ?: return
        val newMode = !current.desktopMode
        current.desktopMode = newMode
        current.webView?.settings?.let { settings ->
            if (newMode) {
                settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
            } else {
                settings.userAgentString = null
                settings.useWideViewPort = false
                settings.loadWithOverviewMode = false
            }
        }
        current.webView?.reload()
        triggerRecomposition()
    }

    fun setUrlInput(value: String) {
        _urlInput.value = value
    }

    fun setIsEditingUrl(editing: Boolean) {
        _isEditingUrl.value = editing
        if (!editing) {
            val current = activeTab
            _urlInput.value = if (current?.url == "nexium://home") "" else (current?.url ?: "")
        }
    }

    fun showSheet(sheet: CurrentSheet) {
        _currentSheet.value = sheet
    }

    fun hideSheet() {
        _currentSheet.value = CurrentSheet.NONE
    }

    fun showCustomVideoView(view: View, callback: WebChromeClient.CustomViewCallback) {
        _customVideoView.value = view
        customViewCallback = callback
    }

    fun hideCustomVideoView() {
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        _customVideoView.value = null
    }

    // Bookmarks management
    fun toggleBookmarkCurrentPage() {
        val current = activeTab ?: return
        if (current.url.isBlank() || current.url == "nexium://home") return

        viewModelScope.launch(Dispatchers.IO) {
            val existing = db.bookmarkDao().getBookmarkByUrl(current.url)
            if (existing != null) {
                db.bookmarkDao().delete(existing)
                _isCurrentPageBookmarked.value = false
            } else {
                val title = if (current.title.isBlank() || current.title == "New Tab") UrlHelper.extractDomain(current.url) else current.title
                db.bookmarkDao().insert(
                    BookmarkEntity(
                        title = title,
                        url = current.url,
                        folder = "Mobile Bookmarks"
                    )
                )
                _isCurrentPageBookmarked.value = true
            }
        }
    }

    fun addBookmark(title: String, url: String, folder: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.bookmarkDao().insert(
                BookmarkEntity(
                    title = title.ifBlank { url },
                    url = url,
                    folder = folder.ifBlank { "Mobile Bookmarks" }
                )
            )
            checkIfCurrentBookmarked(activeTab?.url ?: "")
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.bookmarkDao().delete(bookmark)
            checkIfCurrentBookmarked(activeTab?.url ?: "")
        }
    }

    private fun checkIfCurrentBookmarked(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = db.bookmarkDao().getBookmarkByUrl(url)
            _isCurrentPageBookmarked.value = existing != null
        }
    }

    // History recording
    fun recordHistory(url: String, title: String) {
        val current = activeTab
        if (current?.isIncognito == true || url.isBlank() || url == "nexium://home") return

        viewModelScope.launch(Dispatchers.IO) {
            val existing = db.historyDao().getByUrl(url)
            if (existing != null) {
                db.historyDao().updateVisit(url, title.ifBlank { existing.title }, System.currentTimeMillis())
            } else {
                db.historyDao().insert(
                    HistoryEntity(
                        title = title.ifBlank { UrlHelper.extractDomain(url) },
                        url = url,
                        visitedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteHistoryItem(item: HistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.historyDao().deleteById(item.id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            db.historyDao().clearAll()
        }
    }

    // Clear browsing data
    fun clearBrowsingData(
        clearHistory: Boolean,
        clearCookies: Boolean,
        clearCache: Boolean,
        clearStorage: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (clearHistory) {
                db.historyDao().clearAll()
            }
            withContext(Dispatchers.Main) {
                if (clearCookies) {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                }
                if (clearCache) {
                    _tabs.value.forEach { it.webView?.clearCache(true) }
                }
                if (clearStorage) {
                    _tabs.value.forEach { it.webView?.clearFormData() }
                }
            }
        }
        _currentSheet.value = CurrentSheet.NONE
    }

    // Find in Page
    fun startFindInPage() {
        _findInPage.value = FindInPageState(isActive = true)
    }

    fun closeFindInPage() {
        activeTab?.webView?.clearMatches()
        _findInPage.value = FindInPageState(isActive = false)
    }

    fun updateFindQuery(query: String) {
        _findInPage.value = _findInPage.value.copy(query = query)
        val webView = activeTab?.webView ?: return
        if (query.isNotBlank()) {
            webView.findAllAsync(query)
            webView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                _findInPage.value = _findInPage.value.copy(
                    activeMatchIndex = if (numberOfMatches > 0) activeMatchOrdinal + 1 else 0,
                    totalMatches = numberOfMatches
                )
            }
        } else {
            webView.clearMatches()
            _findInPage.value = _findInPage.value.copy(activeMatchIndex = 0, totalMatches = 0)
        }
    }

    fun findNext() {
        activeTab?.webView?.findNext(true)
    }

    fun findPrevious() {
        activeTab?.webView?.findNext(false)
    }

    // SSL Error Handling
    fun showSslError(url: String, primaryError: String, onProceed: () -> Unit, onCancel: () -> Unit) {
        _sslError.value = SslErrorState(
            isShowing = true,
            url = url,
            primaryError = primaryError,
            onProceed = onProceed,
            onCancel = onCancel
        )
    }

    fun dismissSslError() {
        _sslError.value = SslErrorState(isShowing = false)
    }

    // Quick Shortcuts
    fun addShortcut(title: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolvedUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            db.shortcutDao().insert(QuickShortcutEntity(title = title, url = resolvedUrl))
        }
    }

    fun deleteShortcut(shortcut: QuickShortcutEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.shortcutDao().delete(shortcut)
        }
    }

    // Update tab state helpers
    fun updateTabLoading(tab: TabModel, isLoading: Boolean, progress: Int) {
        tab.isLoading = isLoading
        tab.progress = progress
        triggerRecomposition()
    }

    fun updateTabMetadata(tab: TabModel, title: String?, url: String?, favicon: Bitmap?) {
        var changed = false
        if (title != null && title != tab.title) {
            tab.title = title
            changed = true
        }
        if (url != null && url != tab.url) {
            tab.url = url
            if (tab.id == _activeTabId.value && !_isEditingUrl.value) {
                _urlInput.value = if (url == "nexium://home") "" else url
            }
            checkIfCurrentBookmarked(url)
            recordHistory(url, tab.title)
            changed = true
        }
        if (favicon != null) {
            tab.favicon = favicon
            changed = true
        }
        if (changed) {
            triggerRecomposition()
            saveTabsToDb()
        }
    }

    fun updateTabError(tab: TabModel, isError: Boolean, errorDescription: String?) {
        tab.isError = isError
        tab.errorDescription = errorDescription
        triggerRecomposition()
    }

    fun updateTabNavState(tab: TabModel, canBack: Boolean, canForward: Boolean) {
        tab.canGoBack = canBack
        tab.canGoForward = canForward
        triggerRecomposition()
    }

    private fun safelyDestroyWebView(webView: WebView?) {
        if (webView == null) return
        try {
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.clearHistory()
            webView.loadUrl("about:blank")
            webView.onPause()
            webView.removeAllViews()
            webView.destroy()
        } catch (e: Exception) {
            android.util.Log.e("BrowserViewModel", "Error safely destroying WebView", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _tabs.value.forEach {
            safelyDestroyWebView(it.webView)
            it.webView = null
        }
    }

    private fun triggerRecomposition() {
        _tabs.value = ArrayList(_tabs.value)
    }
}
