package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.SearchEngine
import com.example.ui.theme.CyberAccent
import com.example.ui.theme.CyberNeonTeal
import com.example.ui.theme.CyberPrimary
import com.example.ui.viewmodel.BrowserViewModel
import com.example.ui.viewmodel.CurrentSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val preferences = viewModel.preferences

    val themeMode by preferences.themeMode.collectAsState()
    val searchEngine by preferences.searchEngine.collectAsState()
    val customSearchUrl by preferences.customSearchUrl.collectAsState()
    val jsEnabled by preferences.javascriptEnabled.collectAsState()
    val cookiesEnabled by preferences.cookiesEnabled.collectAsState()
    val popupsBlocked by preferences.popupsBlocked.collectAsState()
    val forceHttps by preferences.forceHttps.collectAsState()
    val dntEnabled by preferences.doNotTrack.collectAsState()
    val desktopMode by preferences.desktopMode.collectAsState()
    val textZoom by preferences.textZoom.collectAsState()
    val safeBrowsing by preferences.safeBrowsing.collectAsState()
    val webRtcProtection by preferences.webRtcProtection.collectAsState()

    var showThemeMenu by remember { mutableStateOf(false) }
    var showEngineMenu by remember { mutableStateOf(false) }
    var showZoomMenu by remember { mutableStateOf(false) }

    var customUrlInput by remember { mutableStateOf(customSearchUrl) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyberPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("😈", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Settings & Functions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "All browser configurations active",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            // Section: App Permissions
            item {
                SectionTitle("DEVICE PERMISSIONS & PRIVACY")
            }

            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Security,
                        title = "App Permissions (Allowed / Granted)",
                        subtitle = "Camera, Mic, Location, Storage & Notifications control",
                        onClick = { viewModel.showSheet(CurrentSheet.PERMISSIONS) }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberNeonTeal.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "MANAGE ⚡",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberNeonTeal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Section: Appearance & Display
            item {
                SectionTitle("APPEARANCE & DISPLAY")
            }

            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Brightness4,
                        title = "Theme Mode",
                        subtitle = themeMode.replaceFirstChar { it.uppercase() },
                        onClick = { showThemeMenu = true }
                    ) {
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            listOf("Dark", "Light", "System").forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text(theme) },
                                    onClick = {
                                        preferences.setThemeMode(theme)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    SettingsRow(
                        icon = Icons.Default.FormatSize,
                        title = "Text Zoom",
                        subtitle = "$textZoom%",
                        onClick = { showZoomMenu = true }
                    ) {
                        DropdownMenu(
                            expanded = showZoomMenu,
                            onDismissRequest = { showZoomMenu = false }
                        ) {
                            listOf(50, 75, 100, 125, 150, 200).forEach { zoom ->
                                DropdownMenuItem(
                                    text = { Text("$zoom%") },
                                    onClick = {
                                        preferences.setTextZoom(zoom)
                                        showZoomMenu = false
                                    }
                                )
                            }
                        }
                    }

                    SettingsToggleRow(
                        icon = Icons.Default.DesktopWindows,
                        title = "Desktop Site by Default",
                        subtitle = "Request desktop version of websites automatically",
                        checked = desktopMode,
                        onCheckedChange = { preferences.setDesktopMode(it) }
                    )
                }
            }

            // Section: Search Engine
            item {
                SectionTitle("SEARCH ENGINE")
            }

            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Search,
                        title = "Default Search Engine",
                        subtitle = searchEngine,
                        onClick = { showEngineMenu = true }
                    ) {
                        DropdownMenu(
                            expanded = showEngineMenu,
                            onDismissRequest = { showEngineMenu = false }
                        ) {
                            SearchEngine.entries.forEach { engine ->
                                DropdownMenuItem(
                                    text = { Text(engine.displayName) },
                                    onClick = {
                                        preferences.setSearchEngine(engine.displayName)
                                        showEngineMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (searchEngine.equals("Custom", ignoreCase = true)) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            OutlinedTextField(
                                value = customUrlInput,
                                onValueChange = {
                                    customUrlInput = it
                                    preferences.setCustomSearchUrl(it)
                                },
                                label = { Text("Custom Search URL (%s or ?q=)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Section: Security & Shield
            item {
                SectionTitle("SECURITY & WEB SHIELD")
            }

            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.Language,
                        title = "Enable JavaScript",
                        subtitle = "Required for modern dynamic web applications",
                        checked = jsEnabled,
                        onCheckedChange = { preferences.setJavascriptEnabled(it) }
                    )

                    SettingsToggleRow(
                        icon = Icons.Default.Security,
                        title = "Accept Cookies",
                        subtitle = "Allow websites to store sessions and site preferences",
                        checked = cookiesEnabled,
                        onCheckedChange = { preferences.setCookiesEnabled(it) }
                    )

                    SettingsToggleRow(
                        icon = Icons.Default.Block,
                        title = "Block Pop-up Windows",
                        subtitle = "Prevent websites from triggering unwanted pop-ups",
                        checked = popupsBlocked,
                        onCheckedChange = { preferences.setPopupsBlocked(it) }
                    )

                    SettingsToggleRow(
                        icon = Icons.Default.Lock,
                        title = "Force HTTPS Only",
                        subtitle = "Automatically upgrade unencrypted HTTP requests",
                        checked = forceHttps,
                        onCheckedChange = { preferences.setForceHttps(it) }
                    )

                    SettingsToggleRow(
                        icon = Icons.Default.GppGood,
                        title = "Safe Browsing Protection",
                        subtitle = "Block known deceptive and phishing domains",
                        checked = safeBrowsing,
                        onCheckedChange = { preferences.setSafeBrowsing(it) }
                    )

                    SettingsToggleRow(
                        icon = Icons.Default.VpnLock,
                        title = "WebRTC Leak Protection",
                        subtitle = "Prevent local IP address leaks over WebRTC",
                        checked = webRtcProtection,
                        onCheckedChange = { preferences.setWebRtcProtection(it) }
                    )

                    SettingsToggleRow(
                        icon = Icons.Default.Shield,
                        title = "Do Not Track (DNT)",
                        subtitle = "Send DNT header requesting sites not to track you",
                        checked = dntEnabled,
                        onCheckedChange = { preferences.setDoNotTrack(it) }
                    )

                    SettingsRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Browsing Data",
                        subtitle = "Delete history, cookies, cache, and form data",
                        onClick = { viewModel.showSheet(CurrentSheet.CLEAR_DATA_DIALOG) }
                    )
                }
            }

            // Section: Tools & About
            item {
                SectionTitle("CORE TOOLS & SYSTEM")
            }

            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.VpnKey,
                        title = "NEXIUM VPN & Proxy",
                        subtitle = "Encrypted tunneling across international nodes",
                        onClick = { viewModel.showSheet(CurrentSheet.VPN) }
                    )

                    SettingsRow(
                        icon = Icons.Default.Shield,
                        title = "Ad & Tracker Blocker",
                        subtitle = "Filter rules, statistics, and domain whitelist",
                        onClick = { viewModel.showSheet(CurrentSheet.AD_BLOCKER) }
                    )

                    SettingsRow(
                        icon = Icons.Default.Extension,
                        title = "Extensions & Userscripts",
                        subtitle = "Manage and install custom JavaScript extensions",
                        onClick = { viewModel.showSheet(CurrentSheet.EXTENSIONS) }
                    )

                    SettingsRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        subtitle = "Zero log policy and security architecture",
                        onClick = { viewModel.showSheet(CurrentSheet.PRIVACY_POLICY) }
                    )

                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "About NEXIUM Browser",
                        subtitle = "Version 2.0 • Ultra Stylish Devil Face Edition 😈",
                        onClick = { viewModel.showSheet(CurrentSheet.ABOUT) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                0.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        CyberPrimary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    extraContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = CyberPrimary.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        extraContent()
    }
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (checked) CyberPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) CyberPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF070B14),
                checkedTrackColor = CyberPrimary
            )
        )
    }
}
