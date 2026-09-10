package com.example.browser

import android.net.Uri
import android.webkit.WebResourceResponse
import com.example.data.AdBlockDao
import com.example.data.AdBlockWhitelistEntity
import com.example.data.BrowserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

class AdBlockEngine(
    private val preferences: BrowserPreferences,
    private val adBlockDao: AdBlockDao,
    private val scope: CoroutineScope
) {
    private val _sessionBlockedCount = MutableStateFlow(0)
    val sessionBlockedCount: StateFlow<Int> = _sessionBlockedCount

    private val whitelistDomains = mutableSetOf<String>()

    // Core list of known advertising and tracking domain signatures
    private val adDomains = hashSetOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "pubmatic.com",
        "rubiconproject.com",
        "criteo.com",
        "criteo.net",
        "scorecardresearch.com",
        "adnxs.com",
        "amazon-adsystem.com",
        "casalemedia.com",
        "openx.net",
        "taboola.com",
        "outbrain.com",
        "adcolony.com",
        "applovin.com",
        "applvn.com",
        "unityads.unity3d.com",
        "chartboost.com",
        "inmobi.com",
        "vungle.com",
        "admob.com",
        "popads.net",
        "popcash.net",
        "propellerads.com",
        "adsterra.com",
        "adroll.com",
        "moatads.com",
        "advertising.com",
        "serving-sys.com",
        "adtechus.com",
        "quantserve.com",
        "revcontent.com",
        "adblade.com",
        "zergnet.com",
        "bidswitch.net",
        "smartadserver.com",
        "yieldmo.com",
        "sharethrough.com",
        "triplelift.com",
        "adform.net",
        "spotxchange.com",
        "stickyadstv.com",
        "facebook.net/tr",
        "connect.facebook.net/signals",
        "analytics.twitter.com",
        "ads-twitter.com",
        "pixel.advertising.com",
        "telemetry.urs.microsoft.com",
        "adsystem.amazon.com",
        "clicktale.net",
        "hotjar.com",
        "crazyegg.com"
    )

    private val adKeywords = listOf(
        "/pagead/",
        "/ads.js",
        "/ads/ga-audiences",
        "/google-analytics.com/analytics.js",
        "/gtag/js?id=",
        "/ad_status.",
        "/advertisement/",
        "/adserver/",
        "ad_type=",
        "ad_slot=",
        "/adview"
    )

    init {
        scope.launch(Dispatchers.IO) {
            adBlockDao.getAllWhitelist().collect { list ->
                synchronized(whitelistDomains) {
                    whitelistDomains.clear()
                    list.forEach { whitelistDomains.add(it.domain.lowercase()) }
                }
            }
        }
    }

    fun isAdBlockEnabled(): Boolean = preferences.adBlockEnabled.value

    fun isDomainWhitelisted(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        val cleanHost = host.lowercase().removePrefix("www.")
        synchronized(whitelistDomains) {
            return whitelistDomains.any { cleanHost == it || cleanHost.endsWith(".$it") }
        }
    }

    fun shouldBlock(requestUrl: String, pageHost: String?): Boolean {
        if (!isAdBlockEnabled()) return false
        if (isDomainWhitelisted(pageHost)) return false

        val lowerUrl = requestUrl.lowercase()
        val requestHost = try {
            Uri.parse(requestUrl).host?.lowercase()?.removePrefix("www.")
        } catch (e: Exception) {
            null
        }

        // Check if request matches any ad domain
        if (requestHost != null) {
            if (adDomains.any { requestHost == it || requestHost.endsWith(".$it") }) {
                recordBlock()
                return true
            }
        }

        // Check path keywords
        if (adKeywords.any { lowerUrl.contains(it) }) {
            recordBlock()
            return true
        }

        return false
    }

    private fun recordBlock() {
        _sessionBlockedCount.value += 1
        preferences.incrementTotalAdsBlocked()
    }

    fun addWhitelist(domain: String) {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/")
        if (clean.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
                adBlockDao.insert(AdBlockWhitelistEntity(clean))
            }
        }
    }

    fun removeWhitelist(domain: String) {
        scope.launch(Dispatchers.IO) {
            adBlockDao.delete(domain)
        }
    }

    fun createBlockedResponse(): WebResourceResponse {
        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
    }
}
