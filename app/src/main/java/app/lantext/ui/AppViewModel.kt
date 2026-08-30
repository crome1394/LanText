package app.lantext.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.lantext.LanTextApp
import app.lantext.data.AppSettings
import app.lantext.data.GatewaySnapshot
import app.lantext.data.PairedDevice
import app.lantext.data.PendingPairing
import app.lantext.net.WifiGate
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

    fun setEnabled(enabled: Boolean) = viewModelScope.launch {
        app.settings.setEnabled(enabled)
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

    fun hasSms(): Boolean = WifiGate.hasSmsPermission(app)
    fun hasContacts(): Boolean = WifiGate.hasContactsPermission(app)
    fun hasNotifications(): Boolean = WifiGate.hasNotificationPermission(app)
    fun hasWifiPerm(): Boolean = WifiGate.hasNearbyWifiPermission(app)
}
