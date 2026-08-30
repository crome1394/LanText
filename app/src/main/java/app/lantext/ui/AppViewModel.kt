package app.lantext.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.lantext.LanTextApp
import android.os.Build
import app.lantext.data.AppSettings
import app.lantext.data.GatewaySnapshot
import app.lantext.data.PairedDevice
import app.lantext.data.PendingPairing
import app.lantext.data.PermissionState
import app.lantext.net.WifiGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LanTextApp

    val settings: StateFlow<AppSettings> = app.settings.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings(),
    )
    val snapshot: StateFlow<GatewaySnapshot> = app.gateway.snapshot
    val pendingPairing: StateFlow<PendingPairing?> = app.pairing.pendingPairing
    val devices: StateFlow<List<PairedDevice>> = app.pairing.devices.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    private val _permissions = MutableStateFlow(readPermissions())
    val permissions: StateFlow<PermissionState> = _permissions

    fun refreshPermissions() {
        _permissions.value = readPermissions()
    }

    private fun readPermissions(): PermissionState = PermissionState(
        sms = WifiGate.hasSmsPermission(app),
        contacts = WifiGate.hasContactsPermission(app),
        notifications = WifiGate.hasNotificationPermission(app),
        nearbyDevices = WifiGate.hasNearbyDevicesPermission(app),
        location = WifiGate.hasLocationPermission(app),
        nearbyDevicesRequired = Build.VERSION.SDK_INT >= 33,
        locationRequired = true,
        notificationsRequired = Build.VERSION.SDK_INT >= 33,
    )

    fun setEnabled(enabled: Boolean) = viewModelScope.launch {
        app.settings.setEnabled(enabled)
    }

    fun setListenPort(port: Int) = viewModelScope.launch {
        app.settings.setListenPort(port)
    }

    fun finishOnboarding() = viewModelScope.launch {
        app.settings.setOnboardingDone()
    }

    fun addCurrentNetwork() = viewModelScope.launch {
        WifiGate.currentSsid(app)?.let { app.settings.addAllowedSsid(it) }
    }

    fun addNetwork(ssid: String) = viewModelScope.launch {
        app.settings.addAllowedSsid(ssid)
    }

    fun removeNetwork(ssid: String) = viewModelScope.launch {
        app.settings.removeAllowedSsid(ssid)
    }

    fun forgetNetwork(ssid: String) = viewModelScope.launch {
        app.settings.forgetSsid(ssid)
    }

    fun recyclePairing() {
        app.gateway.recyclePairing()
    }

    fun approvePairing(id: String) = viewModelScope.launch {
        app.pairing.approve(id)
    }

    fun denyPairing(id: String) = viewModelScope.launch {
        app.pairing.deny(id)
    }

    fun revoke(id: String) = viewModelScope.launch {
        app.pairing.revoke(id)
    }

    fun currentSsid(): String? = WifiGate.currentSsid(app)
}
