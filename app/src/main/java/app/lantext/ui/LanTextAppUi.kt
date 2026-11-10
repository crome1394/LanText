package app.lantext.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.lantext.BuildConfig
import app.lantext.R
import app.lantext.brandProductText
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.lantext.data.AppSettings
import app.lantext.data.GateReason
import app.lantext.data.GatewaySnapshot
import app.lantext.data.PairedDevice
import app.lantext.data.PendingPairing
import app.lantext.data.pairedDeviceName
import app.lantext.data.PermissionState
import app.lantext.util.ListenPort
import java.text.DateFormat
import java.util.Date

private enum class Dest { Home, Networks, Paired, Settings, About }

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
    onListenPort: (Int) -> Unit,
    onGifEnabled: (Boolean) -> Unit,
    onVoiceEnabled: (Boolean) -> Unit,
    onAddCurrent: () -> Unit,
    onAddNetwork: (String) -> Unit,
    onRemoveNetwork: (String) -> Unit,
    onForgetNetwork: (String) -> Unit,
    onRecyclePairing: () -> Unit,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onRevoke: (String) -> Unit,
    embedded: Boolean = false,
    active: Boolean = true,
    appLabel: String = "LanText",
) {
    var dest by remember { mutableStateOf(Dest.Home) }
    BackHandler(enabled = active && dest != Dest.Home) { dest = Dest.Home }
    if (!permissions.allGranted) {
        PermissionRationaleDialog(
            state = permissions,
            appLabel = appLabel,
            onGrantAll = onRequestPermissions,
        )
    }
    if (!settings.onboardingDone) {
        OnboardingScreen(
            permissions = permissions,
            appLabel = appLabel,
            onDone = onFinishOnboarding,
        )
        return
    }

    if (pending != null && permissions.allGranted) {
        AlertDialog(
            onDismissRequest = { onDeny(pending.id) },
            title = { Text("Pair this computer?") },
            text = {
                Text("${pairedDeviceName(pending.clientName)} at ${pending.clientIp} is requesting access to your messages. Approve only if this is you.")
            },
            confirmButton = { TextButton(onClick = { onApprove(pending.id) }) { Text("Approve") } },
            dismissButton = { TextButton(onClick = { onDeny(pending.id) }) { Text("Deny") } },
        )
    }

    val title = when (dest) {
        Dest.Home -> appLabel
        Dest.Networks -> "Wi-Fi networks"
        Dest.Paired -> "Paired devices"
        Dest.Settings -> "Settings"
        Dest.About -> "About"
    }
    val pages: @Composable (Modifier) -> Unit = { modifier ->
        when (dest) {
            Dest.Home -> HomeScreen(
                modifier = modifier,
                snapshot = snapshot,
                settings = settings,
                currentSsid = currentSsid,
                onEnabled = onEnabled,
                onListenPort = onListenPort,
                onNetworks = { dest = Dest.Networks },
                onPaired = { dest = Dest.Paired },
                onSettings = { dest = Dest.Settings },
                onAbout = { dest = Dest.About },
                onAddCurrent = onAddCurrent,
                onRecyclePairing = onRecyclePairing,
            )
            Dest.Networks -> NetworksScreen(
                modifier = modifier,
                settings = settings,
                currentSsid = currentSsid,
                onAddCurrent = onAddCurrent,
                onAddNetwork = onAddNetwork,
                onRemoveNetwork = onRemoveNetwork,
                onForgetNetwork = onForgetNetwork,
            )
            Dest.Paired -> PairedScreen(
                modifier = modifier,
                devices = devices,
                onRename = onRename,
                onRevoke = onRevoke,
            )
            Dest.Settings -> SettingsScreen(
                modifier = modifier,
                settings = settings,
                onGifEnabled = onGifEnabled,
                onVoiceEnabled = onVoiceEnabled,
            )
            Dest.About -> AboutScreen(modifier, permissions, appLabel)
        }
    }
    if (embedded) {
        Column(Modifier.fillMaxSize()) {
            if (dest != Dest.Home) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { dest = Dest.Home }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(title, style = MaterialTheme.typography.titleLarge)
                }
            }
            pages(Modifier.weight(1f))
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
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
            pages(Modifier.padding(padding))
        }
    }
}

@Composable
private fun OnboardingScreen(
    permissions: PermissionState,
    appLabel: String,
    onDone: () -> Unit,
) {
    LaunchedEffect(permissions.allGranted) { if (permissions.allGranted) onDone() }
    val ownApp = appLabel != "LanText"
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Text from your computer — only at home", style = MaterialTheme.typography.headlineMedium)
        if (ownApp) {
            Text("$appLabel opens a private page on Wi-Fi you choose, so you can type on a computer. Nothing is uploaded.")
        } else {
            Text("LanText is a companion for Fossify Messages, Google Messages, or any other SMS app. It never replaces your default messenger.")
        }
        Text("It serves a private web page on Wi-Fi you choose. Nothing is sent to the internet.")
        Text("A prompt explains each permission Android requires. Nothing those permissions unlock is uploaded.")
    }
}

@Composable
private fun PermissionRationaleDialog(
    state: PermissionState,
    appLabel: String,
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
                Text("Permissions $appLabel needs", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Android will show a system prompt for each of these. Nothing they unlock is uploaded. Nearby devices and location are only so the inbox can read the Wi-Fi name. On Android 17, local network access is what lets a computer on your Wi-Fi open the page.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                permissionRationales(state, appLabel).forEach { item ->
                    PermissionLine(item.title, item.body, item.granted)
                }
                Spacer(Modifier.height(4.dp))
                Button(onClick = onGrantAll, modifier = Modifier.fillMaxWidth()) {
                    Text("Grant all")
                }
                Text(
                    "If Android does not ask again, enable them in Settings → Apps → $appLabel → Permissions. On Android 17, allow Local network.",
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
    onListenPort: (Int) -> Unit,
    onNetworks: () -> Unit,
    onPaired: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onAddCurrent: () -> Unit,
    onRecyclePairing: () -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
    // The square tracks the settings card. A fixed bitmap was spilling past the card edges.
    val side = minOf(
        (maxWidth - 48.dp).coerceAtLeast(72.dp),
        (maxHeight * 0.42f).coerceAtLeast(72.dp),
    )
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (snapshot.listening && snapshot.url != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Open on your computer", style = MaterialTheme.typography.titleMedium)
                    Text(
                        snapshot.url,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    val numeric = snapshot.bindAddress
                    if (!numeric.isNullOrBlank()) {
                        Text(
                            "If that name does not open, use https://$numeric:${snapshot.port}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                        )
                    }
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
                        modifier = Modifier.size(side),
                        filterQuality = FilterQuality.None,
                    )
                    Text(
                        snapshot.pairingPin ?: "------",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        "SHA-256  ${formatFp(snapshot.fingerprintSha256)}",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(onClick = onRecyclePairing) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("New PIN and QR")
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Web access", style = MaterialTheme.typography.titleMedium)
                        Text(statusText(snapshot), style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = snapshot.enabled, onCheckedChange = onEnabled)
                }
                AssistChip(onClick = {}, label = { Text(snapshot.ssid ?: currentSsid ?: "Not on Wi-Fi") })
                Collapsible(
                    title = "Listen port",
                    subtitle = settings.listenPort.toString(),
                    expandedByDefault = false,
                ) {
                    PortRow(
                        current = settings.listenPort,
                        onSave = onListenPort,
                    )
                }
                if (snapshot.enabled && snapshot.reason == GateReason.NO_NETWORK_SELECTED && currentSsid != null) {
                    Button(onClick = onAddCurrent, modifier = Modifier.fillMaxWidth()) {
                        Text("Allow this network ($currentSsid)")
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NavButton(Modifier.weight(1f), "Networks", Icons.Outlined.Wifi, onNetworks)
            NavButton(Modifier.weight(1f), "Devices", Icons.Outlined.Devices, onPaired)
            NavButton(Modifier.weight(1f), "Settings", Icons.Outlined.Settings, onSettings)
            NavButton(Modifier.weight(1f), "About", Icons.Outlined.Info, onAbout)
        }
    }
    }
}

@Composable
private fun NavButton(
    modifier: Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                label,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
            )
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
    onForgetNetwork: (String) -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("The computer interface only starts on networks you allow. It turns off on cellular and on any other Wi-Fi.")
        Text("Turn a network off, then tap delete to remove it from this list.")
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!allowed) {
                            IconButton(onClick = { onForgetNetwork(ssid) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Remove $ssid")
                            }
                        }
                        Switch(checked = allowed, onCheckedChange = { on ->
                            if (on) onAddNetwork(ssid) else onRemoveNetwork(ssid)
                        })
                    }
                },
            )
        }
    }
}

@Composable
private fun PairedScreen(
    modifier: Modifier,
    devices: List<PairedDevice>,
    onRename: (String, String) -> Unit,
    onRevoke: (String) -> Unit,
) {
    val format = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }
    var renaming by remember { mutableStateOf<PairedDevice?>(null) }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("A device can read and send texts only after you approve pairing on this phone.")
        Spacer(Modifier.height(12.dp))
        if (devices.isEmpty()) Text("No devices paired yet.")
        devices.forEach { device ->
            val name = pairedDeviceName(device.label)
            ListItem(
                headlineContent = {
                    Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = { Text("Last used ${format.format(Date(device.lastUsedAt))}") },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { renaming = device },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("Rename") }
                        TextButton(
                            onClick = { onRevoke(device.id) },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("Revoke") }
                    }
                },
            )
        }
    }
    val target = renaming
    if (target != null) {
        RenameDeviceDialog(
            initial = pairedDeviceName(target.label),
            onDismiss = { renaming = null },
            onSave = { name ->
                onRename(target.id, name)
                renaming = null
            },
        )
    }
}

@Composable
private fun RenameDeviceDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial.take(80)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename device") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(80) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    settings: AppSettings,
    onGifEnabled: (Boolean) -> Unit,
    onVoiceEnabled: (Boolean) -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Experimental", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "GIF search and voice notes are extras. Turn either off if you do not want them in the computer inbox. SMS and ordinary pictures stay available.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    headlineContent = { Text("GIF search and send") },
                    supportingContent = {
                        Text("Search KLIPY with the key from Settings, GIFs, and send a small GIF as MMS. Experimental.")
                    },
                    trailingContent = {
                        Switch(checked = settings.gifEnabled, onCheckedChange = onGifEnabled)
                    },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Voice messages") },
                    supportingContent = {
                        Text("Record a short voice note in the browser and send it as MMS. Experimental.")
                    },
                    trailingContent = {
                        Switch(checked = settings.voiceEnabled, onCheckedChange = onVoiceEnabled)
                    },
                )
            }
        }
        Text(
            "Threads already keep GIFs and voice notes you received. These switches only hide sending from the computer.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AboutScreen(modifier: Modifier, permissions: PermissionState, appLabel: String) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val ownApp = appLabel != "LanText"
        Text(appLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (ownApp) {
            Text("$appLabel keeps messages on this phone. A computer on your Wi-Fi can read and send after you pair it.")
        } else {
            Text("LanText is a local-network companion. It does not replace Fossify Messages or any other SMS app.")
        }
        Text("Messages stay on your phone and travel only across the Wi-Fi you allow, over HTTPS, to computers you have paired.")
        Text("There is no account, no cloud, no analytics, and no crash reporter.")
        if (ownApp) {
            Text("Send, delete, and mark as read happen in $appLabel. A paired computer can do the same.")
        } else {
            Text("Mark-as-read in the system inbox may be limited because LanText is not the default SMS app. Your phone messenger remains the source of truth. Delete conversations there, not here.")
        }
        Text("On Xiaomi, Huawei, Samsung, and similar devices, set battery usage to Unrestricted so the server is not killed while you are at home.")

        val uriHandler = LocalUriHandler.current
        if (!ownApp) Row(
            Modifier
                .clickable { uriHandler.openUri(GITHUB_URL) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_github),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                "github.com/crome1394/LanText",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Collapsible(
            title = "Why these permissions",
            expandedByDefault = true,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    brandProductText(
                        "These are the permissions this phone actually uses. LanText does not send the data they unlock to anyone else.",
                        appLabel,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                PermissionCard("SMS", if (ownApp) "Read and send texts and picture messages through your carrier. Messages stay on this phone." else SMS_WHY)
                PermissionCard("Contacts", CONTACTS_WHY)
                if (permissions.notificationsRequired) {
                    PermissionCard("Notifications", NOTIFICATIONS_WHY)
                }
                if (permissions.nearbyDevicesRequired) {
                    PermissionCard("Nearby devices", brandProductText(NEARBY_DEVICES_WHY, appLabel))
                }
                if (permissions.locationRequired) {
                    PermissionCard("Location", brandProductText(LOCATION_WHY, appLabel))
                }
                if (permissions.localNetworkRequired) {
                    PermissionCard("Local network", LOCAL_NETWORK_WHY)
                }
                PermissionCard("Network", brandProductText(NETWORK_WHY, appLabel))
            }
        }

        Collapsible(
            title = "For LAN operators",
            expandedByDefault = true,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LAN_OPERATOR_NOTES.forEach { paragraph ->
                    Text(paragraph, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Collapsible(
    title: String,
    subtitle: String? = null,
    expandedByDefault: Boolean,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(expandedByDefault) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (!expanded && subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Hide $title" else "Show $title",
            )
        }
        if (expanded) content()
    }
}

private data class PermissionRationale(
    val title: String,
    val body: String,
    val granted: Boolean,
)

private fun permissionRationales(state: PermissionState, appLabel: String): List<PermissionRationale> = buildList {
    val sms = if (appLabel == "LanText") SMS_WHY else "Read and send texts and picture messages through your carrier. Messages stay on this phone."
    add(PermissionRationale("SMS", sms, state.sms))
    add(PermissionRationale("Contacts", CONTACTS_WHY, state.contacts))
    if (state.notificationsRequired) {
        add(PermissionRationale("Notifications", NOTIFICATIONS_WHY, state.notifications))
    }
    if (state.nearbyDevicesRequired) {
        add(PermissionRationale("Nearby devices", brandProductText(NEARBY_DEVICES_WHY, appLabel), state.nearbyDevices))
    }
    if (state.locationRequired) {
        add(PermissionRationale("Location", brandProductText(LOCATION_WHY, appLabel), state.location))
    }
    if (state.localNetworkRequired) {
        add(PermissionRationale("Local network", LOCAL_NETWORK_WHY, state.localNetwork))
    }
}

private const val SMS_WHY =
    "Read the system message store so the computer inbox matches your phone, and send SMS or picture messages through your carrier. Fossify, Google Messages, or whatever you already use stays the default app."
private const val CONTACTS_WHY =
    "Show names instead of numbers, and let you save an unknown number or add a second phone to someone you already know from the computer. Contacts stay on this phone."
private const val NOTIFICATIONS_WHY =
    "On Android 13 and newer: pairing prompts, and the silent ongoing notice Android requires while the LAN server is a foreground service. Older Android does not prompt for this."
private const val NEARBY_DEVICES_WHY =
    "On Android 13 and newer only. Required so Android will tell LanText the Wi-Fi name while the listener runs in the background. Not a scan for headphones, speakers, or other phones."
private const val LOCATION_WHY =
    "Required on every version LanText supports, including Android 13+. Android hides the current Wi-Fi name without it. Used only to compare that name to your allowlist. Coordinates are never read, stored, or sent."
private const val LOCAL_NETWORK_WHY =
    "On Android 17 and newer, Android drops connections from other devices on your Wi-Fi until you allow this. The phone still answers ping. The browser connection to the inbox times out until it is granted. It does not expose the phone to the internet."
private const val NETWORK_WHY =
    "Lets the listener open a socket. There is no LanText cloud; the listener binds only to this phone’s Wi-Fi address on networks you allow."
private const val GITHUB_URL = "https://github.com/crome1394/LanText"

private val LAN_OPERATOR_NOTES = listOf(
    "The HTTPS certificate is created on this phone (ECDSA P-256). It is not signed by a public CA, so the browser warning is expected. Compare the SHA-256 fingerprint on the home screen with what the browser shows.",
    "The address on the home screen is https://${app.lantext.net.LanDns.HOST} and the port. The phone advertises that name on the allowed Wi-Fi, so the same page can open after the phone gets a new address. If the name does not open, use the numeric address shown under it. The certificate lists ${app.lantext.net.LanDns.HOST}. The browser warning is still expected, because this certificate is not from a public CA.",
    "A DHCP reservation or static IP keeps the URL and QR stable when the phone reconnects.",
    "If you already run Caddy, HAProxy, or OPNsense on the LAN, reverse-proxy to the phone’s IP and port and serve ${app.lantext.net.LanDns.HOST} with an internal CA. Keep that proxy on private addresses only. Pairing (PIN plus Approve) still applies. Do not port-forward this to the internet.",
    "If the computer sleeps or the phone is off the allowed Wi-Fi, Caddy returns HTTP 502 because the phone did not answer. After the first successful visit, the browser keeps a copy of the page and greys out with a Reconnect button instead of Chrome’s 502 screen.",
)

@Composable
private fun PortRow(
    current: Int,
    onSave: (Int) -> Unit,
) {
    var text by remember(current) { mutableStateOf(current.toString()) }
    val parsed = ListenPort.parse(text)
    val dirty = parsed != null && parsed != current
    val invalid = text.isNotBlank() && parsed == null
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { incoming ->
                    if (incoming.length <= 5 && incoming.all { it.isDigit() }) text = incoming
                },
                modifier = Modifier.width(140.dp),
                singleLine = true,
                isError = invalid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text(
                        when {
                            invalid -> "Use ${ListenPort.MIN}–${ListenPort.MAX}"
                            else -> "Default ${AppSettings.DEFAULT_PORT}. The computer URL uses this port."
                        },
                    )
                },
            )
            Button(
                onClick = { parsed?.let(onSave) },
                enabled = dirty,
            ) { Text("Save") }
        }
    }
}

private fun statusText(snapshot: GatewaySnapshot): String = when {
    !snapshot.enabled -> "Off"
    snapshot.listening -> "Listening on ${snapshot.ssid ?: "Wi-Fi"}"
    else -> when (snapshot.reason) {
        GateReason.NO_NETWORK_SELECTED -> "Choose a home Wi-Fi network"
        GateReason.WRONG_NETWORK -> "Waiting for an allowed network"
        GateReason.NO_WIFI -> "Waiting for Wi-Fi"
        GateReason.SSID_HIDDEN -> "Wi-Fi is on, but Android is hiding the network name. Allow Location."
        GateReason.MISSING_SMS_PERMISSION -> "SMS permission needed"
        GateReason.MISSING_NOTIFICATION_PERMISSION -> "Notification permission needed"
        GateReason.MISSING_LOCAL_NETWORK_PERMISSION -> "Local network permission needed"
        else -> "Stopped"
    }
}

private fun formatFp(fp: String?): String {
    if (fp.isNullOrBlank()) return "generating…"
    return fp.chunked(4).joinToString(" ")
}
