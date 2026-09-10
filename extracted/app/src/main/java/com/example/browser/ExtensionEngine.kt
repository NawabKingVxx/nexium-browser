package com.example.browser

import android.webkit.WebView
import com.example.data.ExtensionDao
import com.example.data.ExtensionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExtensionEngine(
    private val extensionDao: ExtensionDao,
    private val scope: CoroutineScope
) {
    val allExtensions: Flow<List<ExtensionEntity>> = extensionDao.getAllExtensions()

    suspend fun getEnabledExtensions(): List<ExtensionEntity> {
        return withContext(Dispatchers.IO) {
            extensionDao.getEnabledExtensions()
        }
    }

    fun setExtensionEnabled(extension: ExtensionEntity, enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            extensionDao.update(extension.copy(isEnabled = enabled))
        }
    }

    fun removeExtension(id: String) {
        scope.launch(Dispatchers.IO) {
            extensionDao.deleteById(id)
        }
    }

    fun installExtension(extension: ExtensionEntity) {
        scope.launch(Dispatchers.IO) {
            extensionDao.insert(extension)
        }
    }

    fun injectExtensionsInto(webView: WebView, pageUrl: String) {
        if (pageUrl.startsWith("nexium:") || pageUrl.startsWith("about:")) return

        scope.launch(Dispatchers.IO) {
            val enabled = extensionDao.getEnabledExtensions()
            withContext(Dispatchers.Main) {
                for (ext in enabled) {
                    if (isUrlMatched(pageUrl, ext.matchesPattern)) {
                        // Inject script
                        if (ext.scriptCode.isNotBlank()) {
                            webView.evaluateJavascript(ext.scriptCode, null)
                        }
                        // Inject CSS if present
                        if (ext.cssCode.isNotBlank()) {
                            val escapedCss = ext.cssCode.replace("`", "\\`").replace("\n", " ")
                            val cssScript = """
                                (function() {
                                    const style = document.createElement('style');
                                    style.setAttribute('data-extension-id', '${ext.id}');
                                    style.textContent = `$escapedCss`;
                                    document.head.appendChild(style);
                                })();
                            """.trimIndent()
                            webView.evaluateJavascript(cssScript, null)
                        }
                    }
                }
            }
        }
    }

    private fun isUrlMatched(url: String, pattern: String): Boolean {
        if (pattern == "<all_urls>" || pattern == "*") return true
        return try {
            val regex = pattern.replace(".", "\\.").replace("*", ".*").toRegex()
            regex.containsMatchIn(url)
        } catch (e: Exception) {
            true
        }
    }
}
