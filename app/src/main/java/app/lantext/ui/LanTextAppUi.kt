package app.lantext.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.lantext.data.AppSettings
import app.lantext.data.GateReason
import app.lantext.data.GatewaySnapshot
import app.lantext.data.PairedDevice
import app.lantext.data.PendingPairing
import java.text.DateFormat
import java.util.Date

private enum class Dest { Home, Networks, Paired, About }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanTextAppUi(
    settings: AppSettings,
    snapshot: GatewaySnapshot,
    pending: PendingPairing?,
    devices: List<PairedDevice>,
    currentSsid: String?,
    hasSms: Boolean,
    hasContacts: Boolean,
    hasNotifications: Boolean,
    hasWifiPerm: Boolean,
    onRequestPermissions: () -> Unit,
    onFinishOnboarding: () -> Unit,
    onEnabled: (Boolean) -> Unit,
    onAddCurrent: () -> Unit,
    onAddNetwork: (String) -> Unit,
    onRemoveNetwork: (String) -> Unit,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit,
    onRevoke: (String) -> Unit,
) {
    var dest by remember { mutableStateOf(Dest.Home) }
    if (!settings.onboardingDone) {
        OnboardingScreen(
            hasSms = hasSms,
            hasContacts = hasContacts,
            hasNotifications = hasNotifications,
            hasWifiPerm = hasWifiPerm,
            onRequestPermissions = onRequestPermissions,
            onDone = onFinishOnboarding,
        )
        return
    }

    if (pending != null) {
        AlertDialog(
            onDismissRequest = { onDeny(pending.id) },
            title = { Text("Pair this computer?") },
            text = {
                Text("${pending.clientName} at ${pending.clientIp} is requesting access to your messages. Approve only if this is you.")
            },
            confirmButton = { TextButton(onClick = { onApprove(pending.id) }) { Text("Approve") } },
            dismissButton = { TextButton(onClick = { onDeny(pending.id) }) { Text("Deny") } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (dest) {
                            Dest.Home -> "LanText"
                            Dest.Networks -> "Wi-Fi networks"
                            Dest.Paired -> "Paired computers"
                            Dest.About -> "About"
                        },
                    )
                },
                navigationIcon = {
                    if (dest != Dest.Home) {
                        IconButton(onClick = { dest = Dest.Home }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (dest) {
            Dest.Home -> HomeScreen(
                modifier = Modifier.padding(padding),
                snapshot = snapshot,
                settings = settings,
                currentSsid = currentSsid,
                onEnabled = onEnabled,
                onNetworks = { dest = Dest.Networks },
                onPaired = { dest = Dest.Paired },
                onAbout = { dest = Dest.About },
                onAddCurrent = onAddCurrent,
                onRequestPermissions = onRequestPermissions,
            )
            Dest.Networks -> NetworksScreen(
                modifier = Modifier.padding(padding),
                settings = settings,
                currentSsid = currentSsid,
                onAddCurrent = onAddCurrent,
                onAddNetwork = onAddNetwork,
                onRemoveNetwork = onRemoveNetwork,
            )
            Dest.Paired -> PairedScreen(
                modifier = Modifier.padding(padding),
                devices = devices,
                onRevoke = onRevoke,
            )
            Dest.About -> AboutScreen(Modifier.padding(padding))
        }
    }
}

@Composable
private fun OnboardingScreen(
    hasSms: Boolean,
    hasContacts: Boolean,
    hasNotifications: Boolean,
    hasWifiPerm: Boolean,
    onRequestPermissions: () -> Unit,
    onDone: () -> Unit,
) {
    val ready = hasSms && hasContacts && hasNotifications && hasWifiPerm
    LaunchedEffect(ready) { if (ready) onDone() }
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Text from your computer — only at home", style = MaterialTheme.typography.headlineMedium)
        Text("LanText is a companion for Fossify Messages, Google Messages, or any other SMS app. It never replaces your default messenger.")
        Text("It serves a private web page on Wi-Fi you choose. Nothing is sent to the internet.")
        PermissionLine("SMS", "Read history and send texts through your carrier.", hasSms)
        PermissionLine("Contacts", "Show names instead of numbers.", hasContacts)
        PermissionLine("Notifications", "Tell you when a computer wants to pair, and keep the LAN server alive.", hasNotifications)
        PermissionLine("Nearby Wi-Fi and location", "Read the Wi-Fi name so the server only runs on networks you allow. Location is never sent anywhere; it is only how Android exposes the SSID.", hasWifiPerm)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRequestPermissions, modifier = Modifier.fillMaxWidth()) {
            Text("Grant permissions")
        }
        if (ready) {
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
        }
    }
}

@Composable
private fun PermissionLine(title: String, body: String, granted: Boolean) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(body) },
        trailingContent = { Text(if (granted) "On" else "Needed", color = MaterialTheme.colorScheme.primary) },
    )
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    snapshot: GatewaySnapshot,
    settings: AppSettings,
    currentSsid: String?,
    onEnabled: (Boolean) -> Unit,
    onNetworks: () -> Unit,
    onPaired: () -> Unit,
    onAbout: () -> Unit,
    onAddCurrent: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Web access", style = MaterialTheme.typography.titleMedium)
                        Text(statusText(snapshot), style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = snapshot.enabled, onCheckedChange = onEnabled)
                }
                AssistChip(onClick = {}, label = { Text(snapshot.ssid ?: currentSsid ?: "Not on Wi-Fi") })
                if (snapshot.reason == GateReason.MISSING_SMS_PERMISSION ||
                    snapshot.reason == GateReason.MISSING_NOTIFICATION_PERMISSION
                ) {
                    Button(onClick = onRequestPermissions) { Text("Grant missing permissions") }
                }
                if (snapshot.enabled && snapshot.reason == GateReason.NO_NETWORK_SELECTED && currentSsid != null) {
                    Button(onClick = onAddCurrent) { Text("Allow this network ($currentSsid)") }
                }
            }
        }

        if (snapshot.listening && snapshot.url != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Open on your computer", style = MaterialTheme.typography.titleMedium)
                    Text(snapshot.url, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                    val payload = remember(snapshot.url, snapshot.fingerprintSha256, snapshot.pairingPin) {
                        buildString {
                            append(snapshot.url)
                            snapshot.fingerprintSha256?.let { append("#fp=").append(it) }
                        }
                    }
                    val qr = remember(payload) { QrBitmaps.encode(payload) }
                    Image(
                        bitmap = qr,
                        contentDescription = "Pairing QR code",
                        modifier = Modifier.size(220.dp),
                        filterQuality = FilterQuality.None,
                    )
                    Text("PIN", style = MaterialTheme.typography.labelLarge)
                    Text(
                        snapshot.pairingPin ?: "------",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        "Your browser will warn about the certificate. Compare this fingerprint with what it shows:\n${formatFp(snapshot.fingerprintSha256)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = onNetworks, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Wifi, null)
                Spacer(Modifier.size(8.dp))
                Text("Networks")
            }
            FilledTonalButton(onClick = onPaired, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Devices, null)
                Spacer(Modifier.size(8.dp))
                Text("Computers")
            }
        }
        OutlinedButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Info, null)
            Spacer(Modifier.size(8.dp))
            Text("About and privacy")
        }
    }
}

@Composable
private fun NetworksScreen(
    modifier: Modifier,
    settings: AppSettings,
    currentSsid: String?,
    onAddCurrent: () -> Unit,
    onAddNetwork: (String) -> Unit,
    onRemoveNetwork: (String) -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("The computer interface only starts on networks you allow. It turns off on cellular and on any other Wi-Fi.")
        if (currentSsid != null && currentSsid !in settings.allowedSsids) {
            Button(onClick = onAddCurrent, modifier = Modifier.fillMaxWidth()) {
                Text("Allow current network ($currentSsid)")
            }
        }
        var typed by remember { mutableStateOf("") }
        OutlinedTextField(
            value = typed,
            onValueChange = { typed = it },
            label = { Text("Network name (SSID)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = { onAddNetwork(typed); typed = "" },
            enabled = typed.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Allow this network") }
        val all = (settings.knownSsids + settings.allowedSsids).sorted()
        if (all.isEmpty()) {
            Text("Connect to your home Wi-Fi, then tap allow.")
        }
        all.forEach { ssid ->
            val allowed = ssid in settings.allowedSsids
            ListItem(
                headlineContent = { Text(ssid) },
                supportingContent = { Text(if (ssid == currentSsid) "Connected" else "Saved") },
                trailingContent = {
                    Switch(checked = allowed, onCheckedChange = { on ->
                        if (on) onAddNetwork(ssid) else onRemoveNetwork(ssid)
                    })
                },
            )
        }
    }
}

@Composable
private fun PairedScreen(
    modifier: Modifier,
    devices: List<PairedDevice>,
    onRevoke: (String) -> Unit,
) {
    val format = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("A computer can read and send texts only after you approve pairing on this phone.")
        Spacer(Modifier.height(12.dp))
        if (devices.isEmpty()) Text("No computers paired yet.")
        devices.forEach { device ->
            ListItem(
                headlineContent = { Text(device.label) },
                supportingContent = { Text("Last used ${format.format(Date(device.lastUsedAt))}") },
                trailingContent = { TextButton(onClick = { onRevoke(device.id) }) { Text("Revoke") } },
            )
        }
    }
}

@Composable
private fun AboutScreen(modifier: Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("LanText is a local-network companion. It does not replace Fossify Messages or any other SMS app.")
        Text("Messages stay on your phone and travel only across the Wi-Fi you allow, over HTTPS, to computers you have paired.")
        Text("There is no account, no cloud, no analytics, and no crash reporter.")
        Text("Mark-as-read and delete in the system inbox may be limited because LanText is not the default SMS app. Your phone messenger remains the source of truth.")
        Text("On Xiaomi, Huawei, Samsung, and similar devices, set battery usage to Unrestricted so the server is not killed while you are at home.")
    }
}

private fun statusText(snapshot: GatewaySnapshot): String = when {
    !snapshot.enabled -> "Off"
    snapshot.listening -> "Listening on ${snapshot.ssid ?: "Wi-Fi"}"
    else -> when (snapshot.reason) {
        GateReason.NO_NETWORK_SELECTED -> "Choose a home Wi-Fi network"
        GateReason.WRONG_NETWORK -> "Waiting for an allowed network"
        GateReason.NO_WIFI -> "Waiting for Wi-Fi"
        GateReason.MISSING_SMS_PERMISSION -> "SMS permission needed"
        GateReason.MISSING_NOTIFICATION_PERMISSION -> "Notification permission needed"
        else -> "Stopped"
    }
}

private fun formatFp(fp: String?): String {
    if (fp.isNullOrBlank()) return "generating…"
    return fp.chunked(4).joinToString(" ")
}
