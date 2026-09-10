package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberPrimary
import com.example.ui.viewmodel.BrowserViewModel
import com.example.ui.viewmodel.CurrentSheet

@Composable
fun CyberBottomBar(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab = viewModel.activeTab
    val tabs by viewModel.tabs.collectAsState()
    val findState by viewModel.findInPage.collectAsState()
    val context = LocalContext.current

    val canBack = activeTab?.canGoBack == true || (activeTab?.url != "nexium://home" && activeTab?.url?.isNotBlank() == true)
    val canForward = activeTab?.canGoForward == true

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Find In Page Bar if active
        if (findState.isActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = findState.query,
                    onValueChange = { viewModel.updateFindQuery(it) },
                    placeholder = { Text("Find in page...", fontSize = 14.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Text(
                    text = if (findState.totalMatches > 0) "${findState.activeMatchIndex}/${findState.totalMatches}" else "0/0",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = { viewModel.findPrevious() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous Match")
                }
                IconButton(onClick = { viewModel.findNext() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Match")
                }
                IconButton(onClick = { viewModel.closeFindInPage() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close Find")
                }
            }
        }

        // Standard Bottom Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = { viewModel.goBack() },
                enabled = canBack,
                modifier = Modifier.size(40.dp).testTag("nav_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (canBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Forward Button
            IconButton(
                onClick = { viewModel.goForward() },
                enabled = canForward,
                modifier = Modifier.size(40.dp).testTag("nav_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (canForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Home Button
            IconButton(
                onClick = { viewModel.loadUrl("nexium://home") },
                modifier = Modifier.size(40.dp).testTag("nav_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (activeTab?.url == "nexium://home") CyberPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Share Button
            IconButton(
                onClick = {
                    val currentUrl = activeTab?.url ?: return@IconButton
                    if (currentUrl.isNotBlank() && currentUrl != "nexium://home") {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, currentUrl)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Link"))
                    }
                },
                enabled = activeTab?.url != "nexium://home" && activeTab?.url?.isNotBlank() == true,
                modifier = Modifier.size(40.dp).testTag("nav_share_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tabs Switcher Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clickable { viewModel.showSheet(CurrentSheet.TABS_OVERVIEW) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tabs.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary
                )
            }
        }
    }
}
