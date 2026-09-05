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

enum class GateReason {
    DISABLED,
    NO_NETWORK_SELECTED,
    WRONG_NETWORK,
    NO_WIFI,
    SSID_HIDDEN,
    MISSING_SMS_PERMISSION,
    MISSING_NOTIFICATION_PERMISSION,
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
) {
    val allGranted: Boolean
        get() = sms && contacts && notifications &&
            (!nearbyDevicesRequired || nearbyDevices) &&
            (!locationRequired || location)
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
