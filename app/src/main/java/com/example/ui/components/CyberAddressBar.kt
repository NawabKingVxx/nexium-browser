package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAccent
import com.example.ui.theme.CyberPrimary
import com.example.ui.viewmodel.BrowserViewModel
import com.example.ui.viewmodel.CurrentSheet
import com.example.vpn.VpnManager
import com.example.vpn.VpnStatus

@Composable
fun CyberAddressBar(
    viewModel: BrowserViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab = viewModel.activeTab
    val tabs by viewModel.tabs.collectAsState()
    val isBookmarked by viewModel.isCurrentPageBookmarked.collectAsState()
    val isEditing by viewModel.isEditingUrl.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val vpnStatus by VpnManager.status.collectAsState()
    val focusManager = LocalFocusManager.current

    val isHome = activeTab?.url == "nexium://home" || activeTab?.url.isNullOrBlank()
    val isHttps = activeTab?.url?.startsWith("https://") == true
    val isLoading = activeTab?.isLoading == true
    val progress = activeTab?.progress ?: 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Address Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                if (activeTab?.isIncognito == true) Color(0xFF9C27B0) else CyberPrimary.copy(alpha = 0.6f),
                                CyberAccent.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Security / Lock icon or Incognito Badge
                    if (activeTab?.isIncognito == true) {
                        Text(
                            text = "🕵️",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    } else if (isHttps) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure Connection",
                            tint = CyberPrimary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    } else if (!isHome) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Not Secure",
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    } else {
                        Text(
                            text = "😈",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }

                    // Input Field
                    Box(modifier = Modifier.weight(1f)) {
                        if (!isEditing && isHome) {
                            Text(
                                text = "Search or type URL...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    viewModel.setIsEditingUrl(true)
                                }
                            )
                        } else if (!isEditing) {
                            Text(
                                text = activeTab?.url ?: "",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    viewModel.setIsEditingUrl(true)
                                }
                            )
                        } else {
                            BasicTextField(
                                value = urlInput,
                                onValueChange = { viewModel.setUrlInput(it) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(CyberPrimary),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Go,
                                    keyboardType = KeyboardType.Uri
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        viewModel.loadUrl(urlInput)
                                        focusManager.clearFocus()
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("address_text_field")
                            )
                        }
                    }

                    // Clear / Refresh button inside address bar
                    if (isEditing) {
                        IconButton(
                            onClick = {
                                viewModel.setUrlInput("")
                                viewModel.setIsEditingUrl(false)
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (isLoading) {
                        IconButton(
                            onClick = { viewModel.stopLoading() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop",
                                tint = CyberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (!isHome) {
                        IconButton(
                            onClick = { viewModel.reload() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // VPN Quick Status Pill (if connected or connecting)
            if (vpnStatus == VpnStatus.CONNECTED || vpnStatus == VpnStatus.CONNECTING) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(if (vpnStatus == VpnStatus.CONNECTED) CyberPrimary.copy(alpha = 0.2f) else Color(0xFFFF9800).copy(alpha = 0.2f))
                        .border(1.dp, if (vpnStatus == VpnStatus.CONNECTED) CyberPrimary else Color(0xFFFF9800), RoundedCornerShape(17.dp))
                        .clickable { viewModel.showSheet(CurrentSheet.VPN) }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "VPN",
                            tint = if (vpnStatus == VpnStatus.CONNECTED) CyberPrimary else Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (vpnStatus == VpnStatus.CONNECTED) "VPN" else "WAIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vpnStatus == VpnStatus.CONNECTED) CyberPrimary else Color(0xFFFF9800)
                        )
                    }
                }
            }

            // Bookmark Button (when browsing a site)
            if (!isHome) {
                IconButton(
                    onClick = { viewModel.toggleBookmarkCurrentPage() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) CyberPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab Counter Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { viewModel.showSheet(CurrentSheet.TABS_OVERVIEW) }
                    .testTag("tab_counter_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tabs.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary
                )
            }

            // Three-dot Menu Button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Loading Progress Bar
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = CyberPrimary,
                trackColor = Color.Transparent
            )
        }
    }
}
