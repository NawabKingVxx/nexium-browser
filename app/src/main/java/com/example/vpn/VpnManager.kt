package com.example.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object VpnManager {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _status = MutableStateFlow(VpnStatus.DISCONNECTED)
    val status: StateFlow<VpnStatus> = _status

    private val _connectedServer = MutableStateFlow<VpnServerModel?>(null)
    val connectedServer: StateFlow<VpnServerModel?> = _connectedServer

    private val _stats = MutableStateFlow(VpnConnectionStats())
    val stats: StateFlow<VpnConnectionStats> = _stats

    private var durationJob: Job? = null
    private var startTimeMillis: Long = 0

    fun checkPermission(context: Context): Intent? {
        return VpnService.prepare(context)
    }

    fun connect(context: Context, server: VpnServerModel) {
        if (_status.value == VpnStatus.CONNECTED || _status.value == VpnStatus.CONNECTING) return

        _status.value = VpnStatus.CONNECTING
        _connectedServer.value = server

        val intent = Intent(context, NexiumVpnService::class.java).apply {
            action = NexiumVpnService.ACTION_CONNECT
            putExtra(NexiumVpnService.EXTRA_SERVER_ID, server.id)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun disconnect(context: Context) {
        if (_status.value == VpnStatus.DISCONNECTED) return

        _status.value = VpnStatus.DISCONNECTING
        val intent = Intent(context, NexiumVpnService::class.java).apply {
            action = NexiumVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun updateStatus(newStatus: VpnStatus) {
        _status.value = newStatus
        if (newStatus == VpnStatus.CONNECTED) {
            startTimeMillis = System.currentTimeMillis()
            startDurationTracker()
        } else if (newStatus == VpnStatus.DISCONNECTED || newStatus == VpnStatus.ERROR) {
            stopDurationTracker()
            _stats.value = _stats.value.copy(durationSeconds = 0, bytesIn = 0, bytesOut = 0)
        }
    }

    fun setConnectedServer(server: VpnServerModel) {
        _connectedServer.value = server
        _stats.value = _stats.value.copy(serverName = server.name, country = server.country)
    }

    fun updateBandwidth(bytesIn: Long, bytesOut: Long) {
        _stats.value = _stats.value.copy(bytesIn = bytesIn, bytesOut = bytesOut)
    }

    private fun startDurationTracker() {
        durationJob?.cancel()
        durationJob = scope.launch {
            while (isActive && _status.value == VpnStatus.CONNECTED) {
                val duration = (System.currentTimeMillis() - startTimeMillis) / 1000
                _stats.value = _stats.value.copy(durationSeconds = duration)
                delay(1000)
            }
        }
    }

    private fun stopDurationTracker() {
        durationJob?.cancel()
        durationJob = null
    }
}
