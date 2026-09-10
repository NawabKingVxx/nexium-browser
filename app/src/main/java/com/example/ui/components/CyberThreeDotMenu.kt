package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Shortcut
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAccent
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberPrimary
import com.example.ui.viewmodel.BrowserViewModel
import com.example.ui.viewmodel.CurrentSheet
import com.example.vpn.VpnManager
import com.example.vpn.VpnStatus

@Composable
fun CyberThreeDotMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    val activeTab = viewModel.activeTab
    val isBookmarked by viewModel.isCurrentPageBookmarked.collectAsState()
    val vpnStatus by VpnManager.status.collectAsState()
    val adBlockEnabled by viewModel.preferences.adBlockEnabled.collectAsState()

    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp))) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .widthIn(min = 230.dp, max = 260.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 4.dp)
        ) {
            // New Tab
            MenuItem(
                icon = Icons.Default.Add,
                title = "New Tab",
                onClick = {
                    onDismiss()
                    viewModel.createNewTab(isIncognito = false)
                }
            )

            // New Incognito Tab
            MenuItem(
                icon = Icons.Default.Security,
                title = "New Incognito Tab",
                onClick = {
                    onDismiss()
                    viewModel.createNewTab(isIncognito = true)
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Bookmarks
            MenuItem(
                icon = Icons.Default.Bookmark,
                title = "Bookmarks",
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.BOOKMARKS)
                }
            )

            // History
            MenuItem(
                icon = Icons.Default.History,
                title = "History",
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.HISTORY)
                }
            )

            // Downloads
            MenuItem(
                icon = Icons.Default.Download,
                title = "Downloads",
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.DOWNLOADS)
                }
            )

            // Add to Bookmarks
            MenuItem(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                title = if (isBookmarked) "Bookmarked" else "Add to Bookmarks",
                tint = if (isBookmarked) CyberPrimary else MaterialTheme.colorScheme.onSurface,
                onClick = {
                    onDismiss()
                    viewModel.toggleBookmarkCurrentPage()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Share
            MenuItem(
                icon = Icons.Default.Share,
                title = "Share",
                onClick = {
                    onDismiss()
                    val url = activeTab?.url
                    if (!url.isNullOrBlank() && url != "nexium://home") {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, url)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Webpage"))
                    } else {
                        Toast.makeText(context, "No active page to share", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Find in Page
            MenuItem(
                icon = Icons.Default.FindInPage,
                title = "Find in Page",
                onClick = {
                    onDismiss()
                    viewModel.startFindInPage()
                }
            )

            // Desktop Site (Checkbox toggle)
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Laptop,
                            contentDescription = "Desktop Site",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Desktop Site",
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = activeTab?.desktopMode == true,
                            onCheckedChange = {
                                onDismiss()
                                viewModel.toggleDesktopMode()
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CyberPrimary,
                                checkmarkColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                },
                onClick = {
                    onDismiss()
                    viewModel.toggleDesktopMode()
                }
            )

            // Translate
            MenuItem(
                icon = Icons.Default.Translate,
                title = "Translate",
                onClick = {
                    onDismiss()
                    val currentUrl = activeTab?.url
                    if (!currentUrl.isNullOrBlank() && currentUrl != "nexium://home") {
                        val translateUrl = "https://translate.google.com/translate?sl=auto&tl=en&u=$currentUrl"
                        viewModel.loadUrl(translateUrl)
                    } else {
                        Toast.makeText(context, "Cannot translate home page", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Add to Home Screen
            MenuItem(
                icon = Icons.Default.Shortcut,
                title = "Add to Home Screen",
                onClick = {
                    onDismiss()
                    val title = activeTab?.title ?: "NEXIUM Web"
                    val url = activeTab?.url ?: "nexium://home"
                    viewModel.addShortcut(title, url)
                    Toast.makeText(context, "Added shortcut: $title", Toast.LENGTH_SHORT).show()
                }
            )

            // Save Page / Offline Copy
            MenuItem(
                icon = Icons.Default.Save,
                title = "Save Page",
                onClick = {
                    onDismiss()
                    val webView = activeTab?.webView
                    if (webView != null && activeTab.url != "nexium://home") {
                        val path = "${context.filesDir}/${System.currentTimeMillis()}.mhtml"
                        webView.saveWebArchive(path)
                        Toast.makeText(context, "Webpage saved offline", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No webpage to save", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Print
            MenuItem(
                icon = Icons.Default.Print,
                title = "Print",
                onClick = {
                    onDismiss()
                    val webView = activeTab?.webView
                    if (webView != null && activeTab.url != "nexium://home") {
                        try {
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                            val adapter = webView.createPrintDocumentAdapter("NEXIUM_Document")
                            printManager.print("NEXIUM_Print", adapter, PrintAttributes.Builder().build())
                        } catch (e: Exception) {
                            Toast.makeText(context, "Printing not supported: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No active page to print", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Clear Browsing Data
            MenuItem(
                icon = Icons.Default.DeleteSweep,
                title = "Clear Browsing Data",
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.CLEAR_DATA_DIALOG)
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Extensions
            MenuItem(
                icon = Icons.Default.Extension,
                title = "Extensions",
                badge = "4 Ready",
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.EXTENSIONS)
                }
            )

            // Ad Blocker
            MenuItem(
                icon = Icons.Default.Shield,
                title = "Ad Blocker",
                badge = if (adBlockEnabled) "ON" else "OFF",
                badgeColor = if (adBlockEnabled) CyberPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.AD_BLOCKER)
                }
            )

            // VPN
            MenuItem(
                icon = Icons.Default.VpnKey,
                title = "VPN",
                badge = when (vpnStatus) {
                    VpnStatus.CONNECTED -> "CONNECTED"
                    VpnStatus.CONNECTING -> "CONNECTING"
                    else -> "OFF"
                },
                badgeColor = when (vpnStatus) {
                    VpnStatus.CONNECTED -> CyberPrimary
                    VpnStatus.CONNECTING -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.VPN)
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Settings
            MenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.SETTINGS)
                }
            )

            // App Permissions
            MenuItem(
                icon = Icons.Default.Security,
                title = "App Permissions 😈",
                badge = "ALLOW ALL",
                badgeColor = CyberPrimary,
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.PERMISSIONS)
                }
            )

            // About NEXIUM
            MenuItem(
                icon = Icons.Default.Info,
                title = "About NEXIUM",
                onClick = {
                    onDismiss()
                    viewModel.showSheet(CurrentSheet.ABOUT)
                }
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    badgeColor: androidx.compose.ui.graphics.Color = CyberPrimary,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = tint,
                    modifier = Modifier.weight(1f)
                )
                if (badge != null) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        onClick = onClick
    )
}
