package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.browser.AdBlockEngine
import com.example.data.BrowserPreferences
import com.example.data.NexiumDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NexiumApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: NexiumDatabase by lazy {
        NexiumDatabase.getDatabase(this, applicationScope)
    }

    val preferences: BrowserPreferences by lazy {
        BrowserPreferences(this)
    }

    val adBlockEngine: AdBlockEngine by lazy {
        AdBlockEngine(preferences, database.adBlockDao(), applicationScope)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // VPN Service Channel
            val vpnChannel = NotificationChannel(
                VPN_CHANNEL_ID,
                "NEXIUM VPN Tunnel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active VPN connection status"
            }
            notificationManager.createNotificationChannel(vpnChannel)

            // Downloads Channel
            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "NEXIUM Downloads",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows file download notifications and progress"
            }
            notificationManager.createNotificationChannel(downloadChannel)
        }
    }

    companion object {
        const val VPN_CHANNEL_ID = "nexium_vpn_channel"
        const val DOWNLOAD_CHANNEL_ID = "nexium_downloads_channel"

        lateinit var instance: NexiumApplication
            private set
    }
}
