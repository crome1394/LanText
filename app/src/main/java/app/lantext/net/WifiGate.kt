package app.lantext.net

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import app.lantext.data.AppSettings
import app.lantext.data.GateReason
import app.lantext.data.SettingsRepository
import app.lantext.util.PrivateNetwork
import java.net.Inet4Address
import java.net.NetworkInterface

object WifiGate {
    @Volatile var lastSsid: String? = null
        private set

    fun remember(ssid: String?) {
        lastSsid = normalizeSsid(ssid)
    }

    fun currentSsid(context: Context): String? {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val network = cm.activeNetwork ?: return null
        val caps = cm.getNetworkCapabilities(network) ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        lastSsid?.let { return it }
        val fromCaps = normalizeSsid(wifiInfo(caps, context)?.ssid)
        if (fromCaps != null) return fromCaps
        @Suppress("DEPRECATION")
        val fromManager = normalizeSsid(
            context.getSystemService(WifiManager::class.java)?.connectionInfo?.ssid,
        )
        return fromManager
    }

    fun wifiIpv4(context: Context): String? {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val network = cm.activeNetwork ?: return null
        val caps = cm.getNetworkCapabilities(network) ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        val link = cm.getLinkProperties(network) ?: return null
        val fromLink = link.linkAddresses
            .mapNotNull { it.address as? Inet4Address }
            .map { it.hostAddress }
            .firstOrNull { PrivateNetwork.isPrivateHost(it) }
        if (fromLink != null) return fromLink
        return firstPrivateIpv4()
    }

    fun evaluate(context: Context, settings: AppSettings): GateReason {
        if (!settings.enabled) return GateReason.DISABLED
        if (!hasSmsPermission(context)) return GateReason.MISSING_SMS_PERMISSION
        if (!hasNotificationPermission(context)) return GateReason.MISSING_NOTIFICATION_PERMISSION
        if (settings.allowedSsids.isEmpty()) return GateReason.NO_NETWORK_SELECTED
        val ssid = currentSsid(context)
        if (ssid == null) return GateReason.NO_WIFI
        if (ssid !in settings.allowedSsids) return GateReason.WRONG_NETWORK
        return GateReason.LISTENING
    }

    fun shouldListen(context: Context, settings: AppSettings): Boolean =
        evaluate(context, settings) == GateReason.LISTENING

    fun hasSmsPermission(context: Context): Boolean {
        val read = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS)
        val send = ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)
        return read == PackageManager.PERMISSION_GRANTED && send == PackageManager.PERMISSION_GRANTED
    }

    fun hasContactsPermission(context: Context): Boolean {
        val read = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        val write = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        return read && write
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun hasNearbyDevicesPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.NEARBY_WIFI_DEVICES) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun hasNearbyWifiPermission(context: Context): Boolean =
        hasNearbyDevicesPermission(context) && hasLocationPermission(context)

    fun normalizeSsid(raw: String?): String? {
        val value = raw?.trim()?.trim('"').orEmpty()
        if (value.isEmpty() || value.equals(SettingsRepository.UNKNOWN_SSID, true) || value == "0x") return null
        return value
    }

    private fun wifiInfo(caps: NetworkCapabilities, context: Context): WifiInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val info = caps.transportInfo as? WifiInfo
            if (info != null) return info
        }
        @Suppress("DEPRECATION")
        return context.getSystemService(WifiManager::class.java)?.connectionInfo
    }

    private fun firstPrivateIpv4(): String? {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
        for (iface in interfaces) {
            if (!iface.isUp || iface.isLoopback) continue
            val name = iface.name.lowercase()
            if (!name.startsWith("wlan") && !name.startsWith("ap") && !name.startsWith("wifi")) continue
            for (addr in iface.inetAddresses) {
                val v4 = addr as? Inet4Address ?: continue
                val host = v4.hostAddress ?: continue
                if (PrivateNetwork.isPrivateHost(host)) return host
            }
        }
        return null
    }
}

fun wifiInfoFrom(network: Network, context: Context): WifiInfo? {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
    val caps = cm.getNetworkCapabilities(network) ?: return null
    if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        caps.transportInfo as? WifiInfo
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(WifiManager::class.java)?.connectionInfo
    }
}
