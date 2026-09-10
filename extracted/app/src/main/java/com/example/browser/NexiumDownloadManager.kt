package com.example.browser

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.DownloadDao
import com.example.data.DownloadItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NexiumDownloadManager(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val scope: CoroutineScope
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var trackingJob: Job? = null

    val allDownloads: Flow<List<DownloadItemEntity>> = downloadDao.getAllDownloads()

    init {
        startProgressTracking()
    }

    fun enqueueDownload(
        url: String,
        contentDisposition: String? = null,
        mimeType: String? = null,
        userAgent: String? = null
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val guessedFileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                val cleanFileName = if (guessedFileName.isNullOrBlank()) "download_${System.currentTimeMillis()}" else guessedFileName
                val resolvedMime = mimeType ?: getMimeTypeFromFileName(cleanFileName)

                val uri = Uri.parse(url)
                val request = DownloadManager.Request(uri).apply {
                    setTitle("NEXIUM: $cleanFileName")
                    setDescription("Downloading via NEXIUM Browser...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanFileName)
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                    if (userAgent != null) {
                        addRequestHeader("User-Agent", userAgent)
                    }
                }

                val downloadId = downloadManager.enqueue(request)
                val downloadFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    cleanFileName
                )

                val entity = DownloadItemEntity(
                    downloadId = downloadId,
                    fileName = cleanFileName,
                    url = url,
                    mimeType = resolvedMime,
                    filePath = downloadFile.absolutePath,
                    totalBytes = 0,
                    downloadedBytes = 0,
                    status = "RUNNING",
                    timestamp = System.currentTimeMillis()
                )
                downloadDao.insert(entity)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Download started: $cleanFileName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Download error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startProgressTracking() {
        trackingJob?.cancel()
        trackingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(1500)
                try {
                    val query = DownloadManager.Query()
                    val cursor = downloadManager.query(query)
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                            val bytesSoFar = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val totalBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val statusInt = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                            val statusStr = when (statusInt) {
                                DownloadManager.STATUS_PENDING -> "PENDING"
                                DownloadManager.STATUS_RUNNING -> "RUNNING"
                                DownloadManager.STATUS_PAUSED -> "PAUSED"
                                DownloadManager.STATUS_SUCCESSFUL -> "SUCCESSFUL"
                                DownloadManager.STATUS_FAILED -> "FAILED"
                                else -> "UNKNOWN"
                            }

                            val item = downloadDao.getByDownloadManagerId(id)
                            if (item != null) {
                                if (item.status != statusStr || item.downloadedBytes != bytesSoFar || item.totalBytes != totalBytes) {
                                    downloadDao.update(
                                        item.copy(
                                            status = statusStr,
                                            downloadedBytes = bytesSoFar,
                                            totalBytes = if (totalBytes > 0) totalBytes else item.totalBytes
                                        )
                                    )
                                }
                            }
                        }
                        cursor.close()
                    }
                } catch (e: Exception) {
                    // Ignore transient tracking issues
                }
            }
        }
    }

    fun cancelDownload(item: DownloadItemEntity) {
        scope.launch(Dispatchers.IO) {
            if (item.downloadId > 0) {
                downloadManager.remove(item.downloadId)
            }
            downloadDao.update(item.copy(status = "CANCELLED"))
        }
    }

    fun deleteDownload(item: DownloadItemEntity, deleteFile: Boolean = true) {
        scope.launch(Dispatchers.IO) {
            if (item.downloadId > 0) {
                downloadManager.remove(item.downloadId)
            }
            if (deleteFile && item.filePath.isNotBlank()) {
                val f = File(item.filePath)
                if (f.exists()) f.delete()
            }
            downloadDao.delete(item)
        }
    }

    fun openDownloadedFile(item: DownloadItemEntity) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist on disk", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, item.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDownloadedFile(item: DownloadItemEntity) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist on disk", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = item.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeTypeFromFileName(fileName: String): String {
        val ext = MimeTypeMap.getFileExtensionFromUrl(fileName)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
    }
}
