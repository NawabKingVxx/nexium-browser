package com.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.browser.ExtensionEngine
import com.example.ui.components.AddBookmarkDialog
import com.example.ui.components.AddExtensionDialog
import com.example.ui.components.AddShortcutDialog
import com.example.ui.components.ClearDataDialog
import com.example.ui.components.CyberAddressBar
import com.example.ui.components.CyberBottomBar
import com.example.ui.components.CyberThreeDotMenu
import com.example.ui.components.ErrorPageComponent
import com.example.ui.components.NexiumWebViewComponent
import com.example.ui.components.SslErrorDialog
import com.example.ui.screens.AboutSheet
import com.example.ui.screens.AdBlockSheet
import com.example.ui.screens.BookmarksSheet
import com.example.ui.screens.DownloadsSheet
import com.example.ui.screens.ExtensionsSheet
import com.example.ui.screens.HistorySheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PermissionsSheet
import com.example.ui.screens.PrivacyPolicySheet
import com.example.ui.screens.SettingsSheet
import com.example.ui.screens.TabsOverviewSheet
import com.example.ui.screens.VpnSheet
import com.example.ui.theme.NexiumTheme
import com.example.ui.viewmodel.BrowserViewModel
import com.example.ui.viewmodel.CurrentSheet

class MainActivity : ComponentActivity() {
    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.preferences.themeMode.collectAsState()

            NexiumTheme(themeMode = themeMode.lowercase()) {
                NexiumBrowserApp(viewModel = viewModel, onFinish = { finish() })
            }
        }
    }
}

@Composable
fun NexiumBrowserApp(
    viewModel: BrowserViewModel,
    onFinish: () -> Unit
) {
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val currentSheet by viewModel.currentSheet.collectAsState()
    val customVideoView by viewModel.customVideoView.collectAsState()
    val findState by viewModel.findInPage.collectAsState()
    val sslError by viewModel.sslError.collectAsState()

    val activeTab = tabs.firstOrNull { it.id == activeTabId } ?: tabs.firstOrNull()
    var isMenuExpanded by remember { mutableStateOf(false) }

    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as NexiumApplication
    val extensionEngine = remember {
        ExtensionEngine(app.database.extensionDao(), app.applicationScope)
    }

    // File chooser launcher for <input type="file">
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            when {
                data?.clipData != null -> {
                    val count = data.clipData!!.itemCount
                    val array = Array(count) { i -> data.clipData!!.getItemAt(i).uri }
                    array
                }
                data?.data != null -> arrayOf(data.data!!)
                else -> null
            }
        } else {
            null
        }
        viewModel.filePathCallback?.onReceiveValue(uris)
        viewModel.filePathCallback = null
    }

    // Hardware Back Button handling
    BackHandler {
        when {
            customVideoView != null -> viewModel.hideCustomVideoView()
            findState.isActive -> viewModel.closeFindInPage()
            currentSheet != CurrentSheet.NONE -> viewModel.hideSheet()
            isMenuExpanded -> isMenuExpanded = false
            activeTab?.canGoBack == true -> viewModel.goBack()
            activeTab?.url != "nexium://home" && !activeTab?.url.isNullOrBlank() -> viewModel.loadUrl("nexium://home")
            else -> onFinish()
        }
    }

    // If fullscreen video is active, show the video overlay
    if (customVideoView != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black)
        ) {
            AndroidView(
                factory = {
                    (customVideoView!!.parent as? ViewGroup)?.removeView(customVideoView)
                    customVideoView!!
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // Standard Browser Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Status bar padding + Address Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            CyberAddressBar(
                viewModel = viewModel,
                onMenuClick = { isMenuExpanded = true }
            )

            // Three-dot dropdown menu
            CyberThreeDotMenu(
                expanded = isMenuExpanded,
                onDismiss = { isMenuExpanded = false },
                viewModel = viewModel
            )
        }

        // Web Content / Home Screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeTab == null || activeTab.url == "nexium://home" || activeTab.url.isBlank()) {
                HomeScreen(viewModel = viewModel)
            } else if (activeTab.isError) {
                ErrorPageComponent(tab = activeTab, viewModel = viewModel)
            } else {
                key(activeTab.id) {
                    NexiumWebViewComponent(
                        tab = activeTab,
                        viewModel = viewModel,
                        onOpenFileChooser = {
                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            }
                            fileChooserLauncher.launch(Intent.createChooser(intent, "Select File"))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Docked Bottom Navigation Bar
        CyberBottomBar(viewModel = viewModel)
    }

    // Dialogs & Sheets Routing
    when (currentSheet) {
        CurrentSheet.TABS_OVERVIEW -> TabsOverviewSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.BOOKMARKS -> BookmarksSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.HISTORY -> HistorySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.DOWNLOADS -> DownloadsSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.VPN -> VpnSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.AD_BLOCKER -> AdBlockSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.EXTENSIONS -> ExtensionsSheet(
            viewModel = viewModel,
            extensionEngine = extensionEngine,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.SETTINGS -> SettingsSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.PERMISSIONS -> PermissionsSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.ABOUT -> AboutSheet(
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.PRIVACY_POLICY -> PrivacyPolicySheet(
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.CLEAR_DATA_DIALOG -> ClearDataDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.ADD_BOOKMARK_DIALOG -> AddBookmarkDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.ADD_SHORTCUT_DIALOG -> AddShortcutDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.ADD_EXTENSION_DIALOG -> AddExtensionDialog(
            onInstall = { extensionEngine.installExtension(it) },
            onDismiss = { viewModel.hideSheet() }
        )
        CurrentSheet.NONE -> { /* No sheet */ }
    }

    // SSL Error Dialog
    if (sslError.isShowing) {
        SslErrorDialog(
            url = sslError.url,
            primaryError = sslError.primaryError,
            onProceed = { sslError.onProceed?.invoke() },
            onCancel = { sslError.onCancel?.invoke() }
        )
    }
}
