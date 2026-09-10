package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        DownloadItemEntity::class,
        SavedTabEntity::class,
        ExtensionEntity::class,
        AdBlockWhitelistEntity::class,
        QuickShortcutEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexiumDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun savedTabDao(): SavedTabDao
    abstract fun extensionDao(): ExtensionDao
    abstract fun adBlockDao(): AdBlockDao
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: NexiumDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NexiumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexiumDatabase::class.java,
                    "nexium_browser.db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            populateInitialData(getDatabase(context, scope))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: NexiumDatabase) {
            // Prepopulate default shortcuts
            val defaultShortcuts = listOf(
                QuickShortcutEntity(title = "Google", url = "https://www.google.com"),
                QuickShortcutEntity(title = "YouTube", url = "https://m.youtube.com"),
                QuickShortcutEntity(title = "GitHub", url = "https://github.com"),
                QuickShortcutEntity(title = "Wikipedia", url = "https://en.m.wikipedia.org"),
                QuickShortcutEntity(title = "Reddit", url = "https://www.reddit.com"),
                QuickShortcutEntity(title = "HackerNews", url = "https://news.ycombinator.com"),
                QuickShortcutEntity(title = "DuckDuckGo", url = "https://duckduckgo.com"),
                QuickShortcutEntity(title = "AI Studio", url = "https://ai.google.dev")
            )
            database.shortcutDao().insertAll(defaultShortcuts)

            // Prepopulate default bookmarks
            val defaultBookmarks = listOf(
                BookmarkEntity(title = "NEXIUM Browser Portal", url = "https://nawabkingmods.com", folder = "Favorites"),
                BookmarkEntity(title = "Google Search", url = "https://www.google.com", folder = "Search Engines"),
                BookmarkEntity(title = "GitHub Code Repositories", url = "https://github.com", folder = "Development"),
                BookmarkEntity(title = "DuckDuckGo Privacy", url = "https://duckduckgo.com", folder = "Privacy")
            )
            defaultBookmarks.forEach { database.bookmarkDao().insert(it) }

            // Prepopulate real extensions
            val defaultExtensions = listOf(
                ExtensionEntity(
                    id = "dark_reader",
                    name = "Dark Reader Cyber",
                    version = "1.2.0",
                    description = "Injects cyber high-contrast dark mode into websites lacking native dark themes.",
                    author = "NEXIUM Lab",
                    isEnabled = true,
                    permissions = "All Sites",
                    scriptCode = """
                        (function() {
                            if (window.__nexium_dark_injected) return;
                            window.__nexium_dark_injected = true;
                            const style = document.createElement('style');
                            style.id = 'nexium-dark-reader';
                            style.textContent = `
                                html {
                                    filter: invert(90%) hue-rotate(180deg) !important;
                                    background: #070B14 !important;
                                }
                                img, video, canvas, iframe, svg, [style*="background-image"] {
                                    filter: invert(100%) hue-rotate(180deg) !important;
                                }
                            `;
                            document.head.appendChild(style);
                        })();
                    """.trimIndent()
                ),
                ExtensionEntity(
                    id = "video_controller",
                    name = "Video Boost & PiP",
                    version = "1.1.0",
                    description = "Adds automatic Picture-in-Picture capability and playback speed unlocker for HTML5 videos.",
                    author = "NEXIUM Lab",
                    isEnabled = true,
                    permissions = "All Sites",
                    scriptCode = """
                        (function() {
                            document.querySelectorAll('video').forEach(v => {
                                v.setAttribute('playsinline', '');
                                v.setAttribute('webkit-playsinline', '');
                                v.playbackRate = 1.0;
                            });
                        })();
                    """.trimIndent()
                ),
                ExtensionEntity(
                    id = "cookie_dismiss",
                    name = "Cookie Consent Dismiss",
                    version = "1.0.4",
                    description = "Automatically dismisses annoying GDPR/cookie consent prompts.",
                    author = "NEXIUM Privacy",
                    isEnabled = true,
                    permissions = "All Sites",
                    scriptCode = """
                        (function() {
                            setTimeout(() => {
                                const selectors = [
                                    '#onetrust-accept-btn-handler',
                                    '.cookie-banner button',
                                    'button[id*="accept"]',
                                    'button[class*="accept"]',
                                    'button[aria-label*="Accept"]',
                                    '#accept-cookie',
                                    '.cmp-intro-accept-all'
                                ];
                                for (const s of selectors) {
                                    const btn = document.querySelector(s);
                                    if (btn) {
                                        try { btn.click(); } catch(e) {}
                                        break;
                                    }
                                }
                            }, 1000);
                        })();
                    """.trimIndent()
                ),
                ExtensionEntity(
                    id = "clean_reader",
                    name = "Clean Reader View",
                    version = "1.0.1",
                    description = "Optimizes web typography and cleans reading layout for articles.",
                    author = "Nawabkingmods",
                    isEnabled = false,
                    permissions = "News & Article Sites",
                    scriptCode = """
                        (function() {
                            const style = document.createElement('style');
                            style.textContent = `
                                article, main, .post-content {
                                    max-width: 720px !important;
                                    margin: 0 auto !important;
                                    font-size: 18px !important;
                                    line-height: 1.8 !important;
                                    font-family: sans-serif !important;
                                }
                            `;
                            document.head.appendChild(style);
                        })();
                    """.trimIndent()
                )
            )
            database.extensionDao().insertAll(defaultExtensions)
        }
    }
}
