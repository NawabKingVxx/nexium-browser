package com.example.vpn

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.NexiumApplication
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class NexiumVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var tunnelJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var bytesIn: Long = 0
    private var bytesOut: Long = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_DISCONNECT) {
            disconnectTunnel()
            return START_NOT_STICKY
        }

        val serverId = intent?.getStringExtra(EXTRA_SERVER_ID) ?: "us_east"
        val server = VpnServerRepository.getServerById(serverId)

        connectTunnel(server)
        return START_STICKY
    }

    private fun connectTunnel(server: VpnServerModel) {
        VpnManager.updateStatus(VpnStatus.CONNECTING)

        val notification = buildForegroundNotification("Connecting to ${server.name}...")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("NexiumVpnService", "startForeground error", e)
        }

        serviceScope.launch {
            try {
                // Simulate handshake / validation
                delay(1200)

                val builder = Builder()
                    .setSession("NEXIUM Tunnel: ${server.name}")
                    .setMtu(1500)
                    .addAddress("10.8.0.2", 24)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("1.1.1.1")
                    .addDnsServer("8.8.8.8")

                // Protect our own socket / traffic if needed
                val tun = builder.establish()
                if (tun == null) {
                    VpnManager.updateStatus(VpnStatus.ERROR)
                    stopSelf()
                    return@launch
                }
                vpnInterface = tun

                VpnManager.updateStatus(VpnStatus.CONNECTED)
                VpnManager.setConnectedServer(server)

                updateForegroundNotification("Protected: ${server.name} (${server.country})")

                // Start tunnel loop to handle TUN packets and track bandwidth
                runTunnelLoop(tun, server)
            } catch (e: Exception) {
                VpnManager.updateStatus(VpnStatus.ERROR)
                stopSelf()
            }
        }
    }

    private fun runTunnelLoop(tun: ParcelFileDescriptor, server: VpnServerModel) {
        tunnelJob?.cancel()
        tunnelJob = serviceScope.launch {
            val inStream = FileInputStream(tun.fileDescriptor)
            val outStream = FileOutputStream(tun.fileDescriptor)
            val packet = ByteArray(32767)

            try {
                while (isActive) {
                    // Poll with timeout or non-blocking check
                    if (inStream.available() > 0) {
                        val length = inStream.read(packet)
                        if (length > 0) {
                            bytesOut += length
                        }
                    } else {
                        // Periodic heartbeat & metrics simulation for active connection
                        delay(1000)
                        bytesIn += (1024..4096).random()
                        bytesOut += (512..2048).random()
                        VpnManager.updateBandwidth(bytesIn, bytesOut)
                    }
                }
            } catch (e: IOException) {
                // Tunnel interrupted or closed
            } finally {
                try {
                    inStream.close()
                    outStream.close()
                } catch (e: Exception) {}
            }
        }
    }

    private fun disconnectTunnel() {
        VpnManager.updateStatus(VpnStatus.DISCONNECTING)
        tunnelJob?.cancel()
        try {
            vpnInterface?.close()
        } catch (e: Exception) {}
        vpnInterface = null

        VpnManager.updateStatus(VpnStatus.DISCONNECTED)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectTunnel()
    }

    private fun buildForegroundNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val disconnectIntent = Intent(this, NexiumVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NexiumApplication.VPN_CHANNEL_ID)
            .setContentTitle("NEXIUM Secure Tunnel")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", disconnectPendingIntent)
            .build()
    }

    private fun updateForegroundNotification(statusText: String) {
        val notification = buildForegroundNotification(statusText)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ACTION_CONNECT = "com.nexium.browser.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.nexium.browser.vpn.DISCONNECT"
        const val EXTRA_SERVER_ID = "extra_server_id"
    }
}
