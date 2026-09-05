package app.lantext.net

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import app.lantext.data.AppSettings
import app.lantext.data.GateReason
import app.lantext.data.SettingsRepository
import java.net.Inet4Address
import java.net.NetworkInterface

object WifiGate {
    @Volatile var lastSsid: String? = null
        private set

    @Volatile var lastSsidIpv4: String? = null
        private set

    @Volatile private var lastLinkNetwork: Network? = null
    @Volatile private var lastLink: LinkProperties? = null

    fun remember(ssid: String?, ipv4: String? = null) {
        if (ssid == null) {
            lastSsid = null
            lastSsidIpv4 = null
            return
        }
        // A redacted "<unknown ssid>" must not wipe a name we already learned.
        val clean = normalizeSsid(ssid) ?: return
        lastSsid = clean
        if (ipv4 != null) lastSsidIpv4 = ipv4
    }

    fun rememberedSsidFor(ipv4: String?): String? {
        val name = lastSsid ?: return null
        val bound = lastSsidIpv4 ?: return null
        if (ipv4 != null && ipv4 == bound) return name
        return null
    }

    fun rememberLink(network: Network, link: LinkProperties) {
        lastLinkNetwork = network
        lastLink = link
    }

    fun forgetLink(network: Network) {
        if (lastLinkNetwork == network) {
            lastLinkNetwork = null
            lastLink = null
        }
    }

    fun isOnWifi(context: Context): Boolean = wifiStaNetwork(context) != null

    fun currentSsid(context: Context): String? {
        val ip = wifiIpv4(context)
        readVisibleSsid(context)?.let {
            remember(it, ip)
            return it
        }
        // Only reuse a remembered name on the same station IPv4. Otherwise a
        // redacted SSID on a different LAN would look like the allowlist.
        return rememberedSsidFor(ip)
    }

    fun readVisibleSsid(context: Context): String? {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        wifiStaNetwork(context)?.let { net ->
            val caps = cm?.getNetworkCapabilities(net)
            normalizeSsid(wifiInfo(caps, context)?.ssid)?.let { return it }
        }
        @Suppress("DEPRECATION")
        normalizeSsid(
            context.getSystemService(WifiManager::class.java)?.connectionInfo?.ssid,
        )?.let { return it }
        val active = cm?.activeNetwork ?: return null
        val caps = cm.getNetworkCapabilities(active) ?: return null
        return normalizeSsid(wifiInfo(caps, context)?.ssid)
    }

    fun wifiIpv4(context: Context): String? {
        val wm = context.getSystemService(WifiManager::class.java)
        @Suppress("DEPRECATION")
        val dhcp = wm?.dhcpInfo
        val dhcpHost = WifiIpv4.fromLittleEndian(dhcp?.ipAddress ?: 0)
        val gatewayHost = WifiIpv4.fromLittleEndian(dhcp?.gateway ?: 0)
        @Suppress("DEPRECATION")
        val connectionHost = WifiIpv4.fromLittleEndian(wm?.connectionInfo?.ipAddress ?: 0)

        val candidates = mutableListOf<Ipv4Candidate>()
        lastLink?.let { addLinkAddresses(it, candidates) }
        val cm = context.getSystemService(ConnectivityManager::class.java)
        wifiStaNetwork(context)?.let { net ->
            cm?.getLinkProperties(net)?.let { addLinkAddresses(it, candidates) }
        }
        addWlanInterfaceAddresses(candidates)

        return WifiIpv4.select(candidates, dhcpHost ?: connectionHost, gatewayHost)
    }

    fun evaluate(context: Context, settings: AppSettings): GateReason {
        if (!settings.enabled) return GateReason.DISABLED
        if (!hasSmsPermission(context)) return GateReason.MISSING_SMS_PERMISSION
        if (!hasNotificationPermission(context)) return GateReason.MISSING_NOTIFICATION_PERMISSION
        if (settings.allowedSsids.isEmpty()) return GateReason.NO_NETWORK_SELECTED
        val ssid = currentSsid(context)
        if (ssid == null) {
            return if (isOnWifi(context) || wifiIpv4(context) != null) GateReason.SSID_HIDDEN else GateReason.NO_WIFI
        }
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
        if (Build.VERSION.SDK_INT >= 33) {
            hasNearbyDevicesPermission(context)
        } else {
            hasLocationPermission(context)
        }

    fun normalizeSsid(raw: String?): String? {
        val value = raw?.trim()?.trim('"').orEmpty()
        if (value.isEmpty() || value.equals(SettingsRepository.UNKNOWN_SSID, true) || value == "0x") return null
        return value
    }

    private fun wifiInfo(caps: NetworkCapabilities?, context: Context): WifiInfo? {
        if (caps != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val info = caps.transportInfo as? WifiInfo
            if (info != null) return info
        }
        @Suppress("DEPRECATION")
        return context.getSystemService(WifiManager::class.java)?.connectionInfo
    }

    @Suppress("DEPRECATION")
    private fun wifiStaNetwork(context: Context): Network? {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val wifi = cm.allNetworks.filter { net ->
            val caps = cm.getNetworkCapabilities(net) ?: return@filter false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                !caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        }
        return wifi.maxByOrNull { net ->
            val caps = cm.getNetworkCapabilities(net)
            var score = 0
            if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) score += 1
            if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true) score += 1
            score
        } ?: run {
            val active = cm.activeNetwork ?: return null
            val caps = cm.getNetworkCapabilities(active) ?: return null
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            ) {
                active
            } else {
                null
            }
        }
    }

    private fun addLinkAddresses(link: LinkProperties, out: MutableList<Ipv4Candidate>) {
        val iface = link.interfaceName
        for (la in link.linkAddresses) {
            val v4 = la.address as? Inet4Address ?: continue
            val host = v4.hostAddress ?: continue
            out += Ipv4Candidate(host, la.prefixLength, iface)
        }
    }

    private fun addWlanInterfaceAddresses(out: MutableList<Ipv4Candidate>) {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return
        for (iface in interfaces) {
            if (!iface.isUp || iface.isLoopback) continue
            val name = iface.name
            if (!WifiIpv4.isStaIface(name)) continue
            for (addr in iface.inetAddresses) {
                val v4 = addr as? Inet4Address ?: continue
                val host = v4.hostAddress ?: continue
                out += Ipv4Candidate(host, prefixFor(v4, iface), name)
            }
        }
    }

    private fun prefixFor(address: Inet4Address, iface: NetworkInterface): Int {
        val width = iface.interfaceAddresses
            .firstOrNull { it.address == address }
            ?.networkPrefixLength
            ?.toInt()
        return width ?: 24
    }
}


