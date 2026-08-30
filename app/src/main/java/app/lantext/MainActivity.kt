package app.lantext

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lantext.ui.AppViewModel
import app.lantext.ui.LanTextAppUi
import app.lantext.ui.LanTextTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refreshPermissions()
        LanTextApp.instance.wifi.reregister()
        LanTextApp.instance.gateway.onNetworkChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanTextTheme {
                val settings by viewModel.settings.collectAsStateWithLifecycle()
                val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
                val pending by viewModel.pendingPairing.collectAsStateWithLifecycle()
                val devices by viewModel.devices.collectAsStateWithLifecycle()
                val permissions by viewModel.permissions.collectAsStateWithLifecycle()
                LanTextAppUi(
                    settings = settings,
                    snapshot = snapshot,
                    pending = pending,
                    devices = devices,
                    currentSsid = viewModel.currentSsid(),
                    permissions = permissions,
                    onRequestPermissions = { requestNeeded() },
                    onFinishOnboarding = viewModel::finishOnboarding,
                    onEnabled = viewModel::setEnabled,
                    onListenPort = viewModel::setListenPort,
                    onAddCurrent = viewModel::addCurrentNetwork,
                    onAddNetwork = viewModel::addNetwork,
                    onRemoveNetwork = viewModel::removeNetwork,
                    onForgetNetwork = viewModel::forgetNetwork,
                    onRecyclePairing = viewModel::recyclePairing,
                    onApprove = viewModel::approvePairing,
                    onDeny = viewModel::denyPairing,
                    onRevoke = viewModel::revoke,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
        LanTextApp.instance.wifi.reregister()
        LanTextApp.instance.gateway.onNetworkChanged()
    }

    private fun requestNeeded() {
        val needed = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
        )
        if (Build.VERSION.SDK_INT >= 33) {
            needed += Manifest.permission.POST_NOTIFICATIONS
            needed += Manifest.permission.NEARBY_WIFI_DEVICES
        }
        needed += Manifest.permission.ACCESS_FINE_LOCATION
        permissionLauncher.launch(needed.toTypedArray())
    }
}
