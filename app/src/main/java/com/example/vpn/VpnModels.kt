package com.example.vpn

enum class VpnStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

data class VpnServerModel(
    val id: String,
    val name: String,
    val country: String,
    val countryCode: String,
    val endpoint: String,
    val protocol: String = "UDP/WireGuard", // "UDP/WireGuard", "TCP/TLS", "OpenVPN"
    val port: Int = 51820,
    val dns: String = "1.1.1.1, 8.8.8.8",
    val pingMs: Int = 28,
    val isCustom: Boolean = false
)

data class VpnConnectionStats(
    val durationSeconds: Long = 0,
    val bytesIn: Long = 0,
    val bytesOut: Long = 0,
    val ipAddress: String = "10.8.0.2",
    val serverName: String = "",
    val country: String = ""
)

object VpnServerRepository {
    val defaultServers = listOf(
        VpnServerModel(
            id = "uk_london",
            name = "London Cyber Node 01",
            country = "United Kingdom",
            countryCode = "GB",
            endpoint = "uk-lon.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 32
        ),
        VpnServerModel(
            id = "us_east",
            name = "New York Metro Node",
            country = "United States",
            countryCode = "US",
            endpoint = "us-nyc.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 24
        ),
        VpnServerModel(
            id = "pk_isb",
            name = "Islamabad Cyber Gateway",
            country = "Pakistan",
            countryCode = "PK",
            endpoint = "pk-isb.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 45
        ),
        VpnServerModel(
            id = "de_fra",
            name = "Frankfurt Transit Node",
            country = "Germany",
            countryCode = "DE",
            endpoint = "de-fra.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 18
        ),
        VpnServerModel(
            id = "nl_ams",
            name = "Amsterdam Privacy Hub",
            country = "Netherlands",
            countryCode = "NL",
            endpoint = "nl-ams.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 15
        ),
        VpnServerModel(
            id = "fr_par",
            name = "Paris Cyber Relay",
            country = "France",
            countryCode = "FR",
            endpoint = "fr-par.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 22
        ),
        VpnServerModel(
            id = "sg_sin",
            name = "Singapore Equinix Node",
            country = "Singapore",
            countryCode = "SG",
            endpoint = "sg-sin.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 38
        ),
        VpnServerModel(
            id = "jp_tyo",
            name = "Tokyo Fast Lane",
            country = "Japan",
            countryCode = "JP",
            endpoint = "jp-tyo.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 42
        ),
        VpnServerModel(
            id = "ca_tor",
            name = "Toronto Shield Node",
            country = "Canada",
            countryCode = "CA",
            endpoint = "ca-tor.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 29
        ),
        VpnServerModel(
            id = "au_syd",
            name = "Sydney Oceanic Relay",
            country = "Australia",
            countryCode = "AU",
            endpoint = "au-syd.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 58
        ),
        VpnServerModel(
            id = "in_bom",
            name = "Mumbai Fiber Node",
            country = "India",
            countryCode = "IN",
            endpoint = "in-bom.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 36
        ),
        VpnServerModel(
            id = "ae_dxb",
            name = "Dubai Silicon Gateway",
            country = "UAE",
            countryCode = "AE",
            endpoint = "ae-dxb.nexium-tunnel.net",
            protocol = "UDP/WireGuard",
            port = 51820,
            pingMs = 31
        )
    )

    fun getServerById(id: String): VpnServerModel {
        return defaultServers.firstOrNull { it.id == id } ?: defaultServers[1] // Default US East
    }
}
