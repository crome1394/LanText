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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.lantext.data.AppSettings
import app.lantext.data.GateReason
import app.lantext.data.GatewaySnapshot
import app.lantext.data.PairedDevice
import app.lantext.data.PendingPairing
import app.lantext.data.PermissionState
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
    permissions: PermissionState,
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
    if (!permissions.allGranted) {
        PermissionRationaleDialog(
            state = permissions,
            onGrantAll = onRequestPermissions,
        )
    }
    if (!settings.onboardingDone) {
        OnboardingScreen(
            permissions = permissions,
            onDone = onFinishOnboarding,
        )
        return
    }

    if (pending != null && permissions.allGranted) {
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
    permissions: PermissionState,
    onDone: () -> Unit,
) {
    LaunchedEffect(permissions.allGranted) { if (permissions.allGranted) onDone() }
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
        Text("A prompt explains each permission Android requires. Nothing those permissions unlock is uploaded.")
    }
}

@Composable
private fun PermissionRationaleDialog(
    state: PermissionState,
    onGrantAll: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Permissions LanText needs", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Android will show a system prompt for each of these. Nothing they unlock is uploaded. Nearby devices and location are only so the inbox can read the Wi-Fi name.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                permissionRationales(state).forEach { item ->
                    PermissionLine(item.title, item.body, item.granted)
                }
                Spacer(Modifier.height(4.dp))
                Button(onClick = onGrantAll, modifier = Modifier.fillMaxWidth()) {
                    Text("Grant all")
                }
                Text(
                    "If Android does not ask again, enable them in Settings → Apps → LanText → Permissions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PermissionLine(title: String, body: String, granted: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                if (granted) "On" else "Needed",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
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
            Text("About, privacy, and permissions")
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
        Text("Mark-as-read in the system inbox may be limited because LanText is not the default SMS app. Your phone messenger remains the source of truth. Delete conversations there, not here.")
        Text("On Xiaomi, Huawei, Samsung, and similar devices, set battery usage to Unrestricted so the server is not killed while you are at home.")

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Why these permissions", style = MaterialTheme.typography.titleMedium)
        Text(
            "Android shows a system prompt for each of these. LanText does not send the data they unlock to anyone else.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nearby devices", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(NEARBY_DEVICES_WHY, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(LOCATION_WHY, style = MaterialTheme.typography.bodyMedium)
            }
        }

        PermissionExplain(title = "SMS", body = SMS_WHY)
        PermissionExplain(title = "Contacts", body = CONTACTS_WHY)
        PermissionExplain(title = "Notifications", body = NOTIFICATIONS_WHY)
        PermissionExplain(
            title = "Network",
            body = "Accept HTTPS connections from a browser on the same Wi-Fi. There is no LanText cloud. The listener binds only to this phone’s Wi-Fi address, and only on networks you allow.",
        )
    }
}

@Composable
private fun PermissionExplain(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}

private data class PermissionRationale(
    val title: String,
    val body: String,
    val granted: Boolean,
)

private fun permissionRationales(state: PermissionState): List<PermissionRationale> = buildList {
    add(PermissionRationale("SMS", SMS_WHY, state.sms))
    add(PermissionRationale("Contacts", CONTACTS_WHY, state.contacts))
    add(PermissionRationale("Notifications", NOTIFICATIONS_WHY, state.notifications))
    if (state.nearbyDevicesRequired) {
        add(PermissionRationale("Nearby devices", NEARBY_DEVICES_WHY, state.nearbyDevices))
    }
    add(PermissionRationale("Location", LOCATION_WHY, state.location))
}

private const val SMS_WHY =
    "Read the system message store so the computer inbox matches your phone, and send SMS or picture messages through your carrier. Fossify, Google Messages, or whatever you already use stays the default app."
private const val CONTACTS_WHY =
    "Show names instead of numbers, and let you save an unknown number or add a second phone to someone you already know from the computer. Contacts stay on this phone."
private const val NOTIFICATIONS_WHY =
    "Tell you when a computer wants to pair, and keep a silent ongoing notification while web access is running. Android requires that notification for a foreground service."
private const val NEARBY_DEVICES_WHY =
    "Android 13 and newer labels this Nearby devices. It is how the phone will tell an app the name of the Wi-Fi you are on. LanText does not search for headphones, speakers, or other phones. It only uses that network name so the computer inbox stays off except on Wi-Fi you allow."
private const val LOCATION_WHY =
    "On Android 12 and older, the Wi-Fi name is gated behind location permission. LanText still asks for it so the allowlist works on every version. It does not read GPS, maps, cell towers, or your street address. Coordinates are never stored and never sent."

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
