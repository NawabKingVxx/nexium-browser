package com.example.browser

import android.net.Uri
import java.net.URLEncoder
import java.util.regex.Pattern

enum class SearchEngine(val displayName: String, val searchUrl: String, val suggestUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=", "https://suggestqueries.google.com/complete/search?client=chrome&q="),
    BING("Bing", "https://www.bing.com/search?q=", "https://api.bing.com/osjson.aspx?query="),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com/ac/?q="),
    YAHOO("Yahoo", "https://search.yahoo.com/search?p=", ""),
    CUSTOM("Custom", "", "");

    companion object {
        fun fromName(name: String): SearchEngine {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: GOOGLE
        }
    }
}

object UrlHelper {
    private val URL_PATTERN = Pattern.compile(
        "^(https?://)?" +
        "(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" + // user@
        "(([0-9]{1,3}\\.){3}[0-9]{1,3}|" + // IP- 199.194.52.184
        "([0-9a-z_!~*'()-]+\\.)*" + // tertiary domain(s)
        "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." + // second level domain
        "[a-z]{2,6})" + // first level domain- .com or .museum
        "(:[0-9]{1,5})?" + // port
        "((/?)|" + // a slash isn't required if there is no file name
        "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$",
        Pattern.CASE_INSENSITIVE
    )

    fun resolveInputToUrl(input: String, engine: SearchEngine, customEngineUrl: String = ""): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "https://www.google.com"

        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return trimmed
        }

        // Special schemes
        if (trimmed.startsWith("about:") || trimmed.startsWith("nexium:") || trimmed.startsWith("javascript:")) {
            return trimmed
        }

        // Check if looks like domain or IP
        val hasSpaces = trimmed.contains(" ")
        val looksLikeDomain = !hasSpaces && (
            trimmed.contains(".") ||
            trimmed.startsWith("localhost") ||
            trimmed.matches(Regex("^[0-9.]+(:[0-9]+)?$"))
        )

        if (looksLikeDomain) {
            return "https://$trimmed"
        }

        // Otherwise perform search
        val encodedQuery = try {
            URLEncoder.encode(trimmed, "UTF-8")
        } catch (e: Exception) {
            trimmed
        }

        val baseUrl = if (engine == SearchEngine.CUSTOM && customEngineUrl.isNotBlank()) {
            customEngineUrl
        } else {
            engine.searchUrl
        }

        return baseUrl + encodedQuery
    }

    fun extractDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host?.removePrefix("www.") ?: url
        } catch (e: Exception) {
            url
        }
    }
}
