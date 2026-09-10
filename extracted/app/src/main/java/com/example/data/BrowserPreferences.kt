package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BrowserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexium_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "dark") ?: "dark")
    val themeMode: StateFlow<String> = _themeMode

    private val _searchEngine = MutableStateFlow(prefs.getString(KEY_SEARCH_ENGINE, "Google") ?: "Google")
    val searchEngine: StateFlow<String> = _searchEngine

    private val _adBlockEnabled = MutableStateFlow(prefs.getBoolean(KEY_AD_BLOCK, true))
    val adBlockEnabled: StateFlow<Boolean> = _adBlockEnabled

    private val _desktopMode = MutableStateFlow(prefs.getBoolean(KEY_DESKTOP_MODE, false))
    val desktopMode: StateFlow<Boolean> = _desktopMode

    private val _javascriptEnabled = MutableStateFlow(prefs.getBoolean(KEY_JS, true))
    val javascriptEnabled: StateFlow<Boolean> = _javascriptEnabled

    private val _cookiesEnabled = MutableStateFlow(prefs.getBoolean(KEY_COOKIES, true))
    val cookiesEnabled: StateFlow<Boolean> = _cookiesEnabled

    private val _popupsBlocked = MutableStateFlow(prefs.getBoolean(KEY_POPUPS, true))
    val popupsBlocked: StateFlow<Boolean> = _popupsBlocked

    private val _doNotTrack = MutableStateFlow(prefs.getBoolean(KEY_DNT, true))
    val doNotTrack: StateFlow<Boolean> = _doNotTrack

    private val _forceHttps = MutableStateFlow(prefs.getBoolean(KEY_FORCE_HTTPS, false))
    val forceHttps: StateFlow<Boolean> = _forceHttps

    private val _totalAdsBlocked = MutableStateFlow(prefs.getLong(KEY_TOTAL_ADS_BLOCKED, 0L))
    val totalAdsBlocked: StateFlow<Long> = _totalAdsBlocked

    private val _selectedVpnServer = MutableStateFlow(prefs.getString(KEY_VPN_SERVER, "us_east") ?: "us_east")
    val selectedVpnServer: StateFlow<String> = _selectedVpnServer

    private val _customSearchUrl = MutableStateFlow(prefs.getString(KEY_CUSTOM_SEARCH_URL, "https://www.google.com/search?q=") ?: "https://www.google.com/search?q=")
    val customSearchUrl: StateFlow<String> = _customSearchUrl

    private val _textZoom = MutableStateFlow(prefs.getInt(KEY_TEXT_ZOOM, 100))
    val textZoom: StateFlow<Int> = _textZoom

    private val _safeBrowsing = MutableStateFlow(prefs.getBoolean(KEY_SAFE_BROWSING, true))
    val safeBrowsing: StateFlow<Boolean> = _safeBrowsing

    private val _webRtcProtection = MutableStateFlow(prefs.getBoolean(KEY_WEBRTC_PROTECT, true))
    val webRtcProtection: StateFlow<Boolean> = _webRtcProtection

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setSearchEngine(engine: String) {
        prefs.edit().putString(KEY_SEARCH_ENGINE, engine).apply()
        _searchEngine.value = engine
    }

    fun setCustomSearchUrl(url: String) {
        prefs.edit().putString(KEY_CUSTOM_SEARCH_URL, url).apply()
        _customSearchUrl.value = url
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AD_BLOCK, enabled).apply()
        _adBlockEnabled.value = enabled
    }

    fun setDesktopMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DESKTOP_MODE, enabled).apply()
        _desktopMode.value = enabled
    }

    fun setJavascriptEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_JS, enabled).apply()
        _javascriptEnabled.value = enabled
    }

    fun setCookiesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_COOKIES, enabled).apply()
        _cookiesEnabled.value = enabled
    }

    fun setPopupsBlocked(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_POPUPS, enabled).apply()
        _popupsBlocked.value = enabled
    }

    fun setDoNotTrack(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DNT, enabled).apply()
        _doNotTrack.value = enabled
    }

    fun setForceHttps(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FORCE_HTTPS, enabled).apply()
        _forceHttps.value = enabled
    }

    fun incrementTotalAdsBlocked() {
        val count = _totalAdsBlocked.value + 1
        prefs.edit().putLong(KEY_TOTAL_ADS_BLOCKED, count).apply()
        _totalAdsBlocked.value = count
    }

    fun setSelectedVpnServer(serverId: String) {
        prefs.edit().putString(KEY_VPN_SERVER, serverId).apply()
        _selectedVpnServer.value = serverId
    }

    fun setTextZoom(zoomPercent: Int) {
        prefs.edit().putInt(KEY_TEXT_ZOOM, zoomPercent).apply()
        _textZoom.value = zoomPercent
    }

    fun setSafeBrowsing(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SAFE_BROWSING, enabled).apply()
        _safeBrowsing.value = enabled
    }

    fun setWebRtcProtection(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WEBRTC_PROTECT, enabled).apply()
        _webRtcProtection.value = enabled
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SEARCH_ENGINE = "search_engine"
        private const val KEY_CUSTOM_SEARCH_URL = "custom_search_url"
        private const val KEY_AD_BLOCK = "ad_block_enabled"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_JS = "javascript_enabled"
        private const val KEY_COOKIES = "cookies_enabled"
        private const val KEY_POPUPS = "popups_blocked"
        private const val KEY_DNT = "do_not_track"
        private const val KEY_FORCE_HTTPS = "force_https"
        private const val KEY_TOTAL_ADS_BLOCKED = "total_ads_blocked"
        private const val KEY_VPN_SERVER = "selected_vpn_server"
        private const val KEY_TEXT_ZOOM = "text_zoom"
        private const val KEY_SAFE_BROWSING = "safe_browsing"
        private const val KEY_WEBRTC_PROTECT = "webrtc_protect"
    }
}
