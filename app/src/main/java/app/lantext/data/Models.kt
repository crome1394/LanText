package app.lantext.data

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val enabled: Boolean = false,
    val onboardingDone: Boolean = false,
    val allowedSsids: Set<String> = emptySet(),
    val knownSsids: Set<String> = emptySet(),
    val listenPort: Int = DEFAULT_PORT,
    val webPalette: String = "fern",
    val webMode: String = "auto",
    val lastNetworkSsid: String? = null,
    val lastNetworkIpv4: String? = null,
    val gifEnabled: Boolean = true,
    val voiceEnabled: Boolean = true,
    val pinnedThreadIds: Set<String> = emptySet(),
    val gifApiKey: String = "",
    val phoneWeb: PhoneWeb = PhoneWeb(),
) {
    companion object {
        const val DEFAULT_PORT = 8743
    }
}

@Serializable
data class PairedDevice(
    val id: String,
    val label: String,
    val tokenHash: String,
    val createdAt: Long,
    val lastUsedAt: Long,
)

@Serializable
data class PendingPairing(
    val id: String,
    val clientName: String,
    val clientIp: String,
    val createdAt: Long,
)

@Serializable
data class PhoneWeb(
    val palette: String = "",
    val mode: String = "",
    val fontScale: Float = 1f,
    val striped: Boolean = false,
    val searchAtBottom: Boolean = false,
    val enterSends: Boolean = false,
    val showSim: Boolean = true,
    val detailDirection: Boolean = true,
    val detailParty: Boolean = true,
    val detailTime: Boolean = true,
    val detailType: Boolean = true,
    val detailSim: Boolean = true,
    val defaultSubId: Int = -1,
    val ribbonRead: Boolean = true,
    val ribbonDelete: Boolean = true,
    val ribbonPin: Boolean = true,
    val launcherIcon: String = "cyan",
    val inboxGroup: String = "none",
)

/**
 * A saved choice wins. A new install that already has an allowed network is on,
 * because allowing the network is what starts the computer inbox.
 */
fun webAccessEnabled(stored: Boolean?, allowedNetworks: Int): Boolean =
    stored ?: (allowedNetworks > 0)

/**
 * A browser user-agent captured at pair time reads as "Chrome on Linux".
 * A name the user chose is kept.
 */
fun pairedDeviceName(label: String): String {
    val raw = label.trim()
    if (raw.isEmpty()) return "Device"
    if (!raw.contains("Mozilla/") && !raw.contains("AppleWebKit/")) return raw.take(80)
    val browser = when {
        raw.contains("Edg/") || raw.contains("Edge/") -> "Edge"
        raw.contains("OPR/") || raw.contains("Opera") -> "Opera"
        raw.contains("Firefox/") -> "Firefox"
        raw.contains("CriOS/") || raw.contains("Chrome/") -> "Chrome"
        raw.contains("Safari/") -> "Safari"
        else -> "Browser"
    }
    val os = when {
        raw.contains("Windows") -> "Windows"
        raw.contains("Android") -> "Android"
        raw.contains("CrOS") -> "ChromeOS"
        raw.contains("iPad") -> "iPad"
        raw.contains("iPhone") -> "iPhone"
        raw.contains("Mac OS") || raw.contains("Macintosh") -> "Mac"
        raw.contains("Linux") -> "Linux"
        else -> null
    }
    return if (os == null) browser else "$browser on $os"
}

enum class GateReason {
    DISABLED,
    NO_NETWORK_SELECTED,
    WRONG_NETWORK,
    NO_WIFI,
    SSID_HIDDEN,
    MISSING_SMS_PERMISSION,
    MISSING_NOTIFICATION_PERMISSION,
    MISSING_LOCAL_NETWORK_PERMISSION,
    LISTENING,
}

data class PermissionState(
    val sms: Boolean = false,
    val contacts: Boolean = false,
    val notifications: Boolean = false,
    val nearbyDevices: Boolean = false,
    val location: Boolean = false,
    val nearbyDevicesRequired: Boolean = false,
    val locationRequired: Boolean = false,
    val notificationsRequired: Boolean = false,
    val localNetwork: Boolean = false,
    val localNetworkRequired: Boolean = false,
) {
    val allGranted: Boolean
        get() = sms && contacts && notifications &&
            (!nearbyDevicesRequired || nearbyDevices) &&
            (!locationRequired || location) &&
            (!localNetworkRequired || localNetwork)
}

data class GatewaySnapshot(
    val enabled: Boolean = false,
    val listening: Boolean = false,
    val ssid: String? = null,
    val bindAddress: String? = null,
    val port: Int = AppSettings.DEFAULT_PORT,
    val url: String? = null,
    val fingerprintSha256: String? = null,
    val pairingPin: String? = null,
    val reason: GateReason = GateReason.DISABLED,
    val clientCount: Int = 0,
    val webPalette: String = "fern",
    val webMode: String = "auto",
)
