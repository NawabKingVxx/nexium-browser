package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val folder: String = "Mobile Bookmarks",
    val favicon: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val visitCount: Int = 1
)

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val downloadId: Long = 0,
    val fileName: String,
    val url: String,
    val mimeType: String = "*/*",
    val filePath: String = "",
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: String = "PENDING", // PENDING, RUNNING, PAUSED, SUCCESSFUL, FAILED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_tabs")
data class SavedTabEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val isIncognito: Boolean = false,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "extensions")
data class ExtensionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val isEnabled: Boolean = true,
    val permissions: String = "All Sites",
    val scriptCode: String = "",
    val cssCode: String = "",
    val matchesPattern: String = "<all_urls>"
)

@Entity(tableName = "adblock_whitelist")
data class AdBlockWhitelistEntity(
    @PrimaryKey val domain: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shortcuts")
data class QuickShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val iconResName: String? = null
)
