package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.browser.SearchEngine
import com.example.browser.UrlHelper
import com.example.data.BookmarkEntity
import com.example.data.HistoryEntity
import com.example.data.QuickShortcutEntity
import com.example.ui.theme.CyberAccent
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDevilNeon
import com.example.ui.theme.CyberDevilRed
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGlowCyan
import com.example.ui.theme.CyberNeonTeal
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.viewmodel.BrowserViewModel
import com.example.ui.viewmodel.CurrentSheet

@Composable
fun HomeScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shortcuts by viewModel.shortcuts.collectAsState()
    val recentlyVisited by viewModel.recentlyVisited.collectAsState()
    val mostVisited by viewModel.mostVisited.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val currentSearchEngine by viewModel.preferences.searchEngine.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showEngineMenu by remember { mutableStateOf(false) }

    // Speech to text launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = spoken?.firstOrNull()
            if (!query.isNullOrBlank()) {
                searchQuery = query
                viewModel.loadUrl(query)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Logo & Hero Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(CyberDevilNeon.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(CyberDevilRed, CyberDevilNeon, CyberPrimary)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_nexium_logo),
                        contentDescription = "NEXIUM Logo",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NEXIUM",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "😈 DEVIL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberDevilRed,
                        modifier = Modifier
                            .background(CyberDevilRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, CyberDevilRed.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "ANONYMOUS • ULTRA FAST • UNSTOPPABLE 😈",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CyberGlowCyan,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Search Bar with Engine Selector & Voice Search
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(CyberPrimary.copy(alpha = 0.6f), CyberAccent.copy(alpha = 0.3f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Engine Picker
                    Box {
                        Text(
                            text = currentSearchEngine.take(3).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberPrimary.copy(alpha = 0.15f))
                                .clickable { showEngineMenu = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )

                        DropdownMenu(
                            expanded = showEngineMenu,
                            onDismissRequest = { showEngineMenu = false }
                        ) {
                            SearchEngine.entries.forEach { engine ->
                                DropdownMenuItem(
                                    text = { Text(engine.displayName) },
                                    onClick = {
                                        viewModel.preferences.setSearchEngine(engine.displayName)
                                        showEngineMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search with $currentSearchEngine or enter URL",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_search_input")
                    )

                    // Voice Search Button
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search NEXIUM")
                            }
                            try {
                                voiceLauncher.launch(intent)
                            } catch (e: Exception) {
                                // Speech recognizer not installed
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = CyberPrimary
                        )
                    }

                    // Go Search Button
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.loadUrl(searchQuery)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Go",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Quick Actions Hub (Cyber Pills)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(CyberDevilNeon.copy(alpha = 0.3f), CyberPrimary.copy(alpha = 0.2f))),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionPill(
                        icon = Icons.Default.Security,
                        label = "Private",
                        color = Color(0xFFCE93D8),
                        onClick = { viewModel.createNewTab(isIncognito = true) }
                    )
                    QuickActionPill(
                        icon = Icons.Default.VpnKey,
                        label = "VPN",
                        color = CyberPrimary,
                        onClick = { viewModel.showSheet(CurrentSheet.VPN) }
                    )
                    QuickActionPill(
                        icon = Icons.Default.Shield,
                        label = "AdBlock",
                        color = CyberNeonTeal,
                        onClick = { viewModel.showSheet(CurrentSheet.AD_BLOCKER) }
                    )
                    QuickActionPill(
                        icon = Icons.Default.Security,
                        label = "Perms 😈",
                        color = CyberEmerald,
                        onClick = { viewModel.showSheet(CurrentSheet.PERMISSIONS) }
                    )
                    QuickActionPill(
                        icon = Icons.Default.Download,
                        label = "Files",
                        color = CyberAccent,
                        onClick = { viewModel.showSheet(CurrentSheet.DOWNLOADS) }
                    )
                    QuickActionPill(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { viewModel.showSheet(CurrentSheet.SETTINGS) }
                    )
                }
            }
        }

        // Quick Shortcuts Section
        item {
            SectionHeader(
                title = "QUICK SHORTCUTS",
                actionText = "+ Add",
                onAction = { viewModel.showSheet(CurrentSheet.ADD_SHORTCUT_DIALOG) }
            )
        }

        item {
            val rows = shortcuts.chunked(4)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { shortcut ->
                            ShortcutItem(
                                title = shortcut.title,
                                url = shortcut.url,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.loadUrl(shortcut.url) }
                            )
                        }
                        // Fill remaining space if row has less than 4 items
                        repeat(4 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Recently Visited Websites
        if (recentlyVisited.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "RECENTLY VISITED",
                    actionText = "History",
                    onAction = { viewModel.showSheet(CurrentSheet.HISTORY) }
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recentlyVisited) { item ->
                        RecentSiteCard(
                            item = item,
                            onClick = { viewModel.loadUrl(item.url) }
                        )
                    }
                }
            }
        }

        // Bookmarks Quick Access
        if (bookmarks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "SAVED BOOKMARKS",
                    actionText = "All (${bookmarks.size})",
                    onAction = { viewModel.showSheet(CurrentSheet.BOOKMARKS) }
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bookmarks.take(4).forEach { b ->
                        BookmarkRowItem(
                            bookmark = b,
                            onClick = { viewModel.loadUrl(b.url) }
                        )
                    }
                }
            }
        }

        // Most Visited Websites
        if (mostVisited.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "MOST VISITED",
                    actionText = null,
                    onAction = {}
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mostVisited.take(5).forEach { site ->
                        BookmarkRowItem(
                            bookmark = BookmarkEntity(
                                title = site.title,
                                url = site.url,
                                folder = "${site.visitCount} visits"
                            ),
                            onClick = { viewModel.loadUrl(site.url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ShortcutItem(
    title: String,
    url: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val initial = title.firstOrNull()?.uppercaseChar()?.toString() ?: "W"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CyberPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecentSiteCard(
    item: HistoryEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CyberPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title.firstOrNull()?.uppercaseChar()?.toString() ?: "H",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = UrlHelper.extractDomain(item.url),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BookmarkRowItem(
    bookmark: BookmarkEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = CyberPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = bookmark.url,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (bookmark.folder.isNotBlank()) {
                Text(
                    text = bookmark.folder,
                    fontSize = 10.sp,
                    color = CyberAccent,
                    modifier = Modifier
                        .background(CyberAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionText != null) {
            Text(
                text = actionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyberPrimary,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(4.dp)
            )
        }
    }
}
