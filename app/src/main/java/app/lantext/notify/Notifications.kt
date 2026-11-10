package app.lantext.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import app.lantext.MainActivity
import app.lantext.R
import app.lantext.data.GateReason
import app.lantext.data.GatewaySnapshot
import app.lantext.data.PendingPairing
import app.lantext.net.GatewayService
import app.lantext.net.NotificationActionReceiver

object Notifications {
    const val GATEWAY_ID = 17
    const val PAIRING_ID = 18
    const val EXTRA_OPEN_LAN_MESSAGING = "app.lantext.OPEN_LAN_MESSAGING"
    private const val CHANNEL_GATEWAY = "gateway"
    private const val CHANNEL_PAIRING = "pairing"

    private fun openHost(context: Context): Intent {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launch != null) return launch.putExtra(EXTRA_OPEN_LAN_MESSAGING, true)
        return Intent(context, MainActivity::class.java)
    }

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GATEWAY,
                context.getString(R.string.notification_channel_gateway),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PAIRING,
                context.getString(R.string.notification_channel_pairing),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }

    fun gateway(context: Context, snap: GatewaySnapshot): Notification {
        val open = PendingIntent.getActivity(
            context,
            0,
            openHost(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, NotificationActionReceiver::class.java).setAction(GatewayService.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = if (snap.listening) {
            snap.url ?: listeningTitle(context)
        } else {
            when (snap.reason) {
                GateReason.NO_WIFI -> context.getString(R.string.notification_no_wifi)
                GateReason.SSID_HIDDEN -> context.getString(R.string.notification_ssid_hidden)
                GateReason.WRONG_NETWORK -> context.getString(R.string.notification_wrong_network)
                GateReason.MISSING_LOCAL_NETWORK_PERMISSION -> localNetworkText(context)
                else -> context.getString(R.string.notification_waiting)
            }
        }
        return NotificationCompat.Builder(context, CHANNEL_GATEWAY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                if (snap.listening) listeningTitle(context)
                else context.getString(R.string.notification_waiting),
            )
            .setContentText(text)
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .setColor(context.getColor(R.color.notification_color))
            .addAction(0, context.getString(R.string.notification_stop), stop)
            .build()
    }

    fun updateGateway(context: Context, snap: GatewaySnapshot) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(GATEWAY_ID, gateway(context, snap))
    }

    fun pairing(context: Context, request: PendingPairing) {
        val approve = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, NotificationActionReceiver::class.java)
                .setAction(NotificationActionReceiver.ACTION_APPROVE)
                .putExtra(NotificationActionReceiver.EXTRA_ID, request.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val deny = PendingIntent.getBroadcast(
            context,
            3,
            Intent(context, NotificationActionReceiver::class.java)
                .setAction(NotificationActionReceiver.ACTION_DENY)
                .putExtra(NotificationActionReceiver.EXTRA_ID, request.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val open = PendingIntent.getActivity(
            context,
            4,
            openHost(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_PAIRING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_pairing_title))
            .setContentText(context.getString(R.string.notification_pairing_body, request.clientName))
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.approve), approve)
            .addAction(0, context.getString(R.string.deny), deny)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(PAIRING_ID, notification)
    }

    fun cancelPairing(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(PAIRING_ID)
    }

    private fun productName(context: Context): String? =
        (context.applicationContext as? app.lantext.LanTextApp)?.productLabel?.trim()?.takeIf { it.isNotEmpty() }

    private fun listeningTitle(context: Context): String {
        val name = productName(context)
        return if (name != null) "$name is available on this Wi-Fi"
        else context.getString(R.string.notification_listening)
    }

    private fun localNetworkText(context: Context): String {
        val name = productName(context)
        return if (name != null) "Allow local network access so a computer can open $name"
        else context.getString(R.string.notification_local_network)
    }
}
