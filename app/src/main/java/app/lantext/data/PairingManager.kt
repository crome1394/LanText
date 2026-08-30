package app.lantext.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.lantext.util.Crypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

private val Context.pairingStore by preferencesDataStore(name = "lantext_pairing")

class PairingManager(context: Context) {
    private val store = context.applicationContext.pairingStore
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    private val pin = MutableStateFlow<String?>(null)
    private val pinCreatedAt = MutableStateFlow(0L)
    private val pending = MutableStateFlow<PendingPairing?>(null)
    private val failures = ConcurrentHashMap<String, AtomicInteger>()
    private val approvedTokens = ConcurrentHashMap<String, String>()
    private val deniedIds = ConcurrentHashMap.newKeySet<String>()

    val pairingPin: StateFlow<String?> = pin
    val pendingPairing: StateFlow<PendingPairing?> = pending

    val devices: Flow<List<PairedDevice>> = store.data.map { prefs ->
        val raw = prefs[KEY_DEVICES].orEmpty()
        if (raw.isBlank()) emptyList() else json.decodeFromString(raw)
    }

    fun rotatePin() {
        pin.value = Crypto.randomPin()
        pinCreatedAt.value = System.currentTimeMillis()
    }

    fun clearPin() {
        pin.value = null
        pinCreatedAt.value = 0L
    }

    suspend fun currentDevices(): List<PairedDevice> = devices.first()

    suspend fun beginPair(pinAttempt: String, clientName: String, clientIp: String): Pair<Boolean, String?> {
        val now = System.currentTimeMillis()
        val fails = failures.getOrPut(clientIp) { AtomicInteger(0) }
        if (fails.get() >= MAX_FAILURES) {
            return false to "locked"
        }
        val expected = pin.value
        if (expected.isNullOrBlank() || now - pinCreatedAt.value > PIN_TTL_MS) {
            fails.incrementAndGet()
            return false to "expired"
        }
        if (pinAttempt.filter { it.isDigit() } != expected) {
            fails.incrementAndGet()
            return false to "invalid"
        }
        val request = PendingPairing(
            id = Crypto.randomId(),
            clientName = clientName.ifBlank { "Browser" }.take(80),
            clientIp = clientIp,
            createdAt = now,
        )
        pending.value = request
        fails.set(0)
        return true to request.id
    }

    fun pendingById(id: String): PendingPairing? = pending.value?.takeIf { it.id == id }

    suspend fun approve(id: String): String? = mutex.withLock {
        val request = pending.value?.takeIf { it.id == id } ?: return null
        pending.value = null
        val token = Crypto.randomToken()
        val device = PairedDevice(
            id = Crypto.randomId(),
            label = request.clientName,
            tokenHash = Crypto.sha256Hex(token),
            createdAt = System.currentTimeMillis(),
            lastUsedAt = System.currentTimeMillis(),
        )
        val next = currentDevices() + device
        persist(next)
        approvedTokens[id] = token
        rotatePin()
        token
    }

    fun deny(id: String) {
        if (pending.value?.id == id) pending.value = null
        deniedIds.add(id)
        rotatePin()
    }

    fun pairStatus(id: String): PairStatus {
        if (approvedTokens.containsKey(id)) return PairStatus.APPROVED
        if (deniedIds.contains(id)) return PairStatus.DENIED
        if (pending.value?.id == id) return PairStatus.PENDING
        return PairStatus.UNKNOWN
    }

    fun consumeApprovedToken(id: String): String? = approvedTokens.remove(id)

    suspend fun deviceForToken(token: String): PairedDevice? {
        val hash = Crypto.sha256Hex(token)
        return currentDevices().firstOrNull { it.tokenHash == hash }
    }

    suspend fun touch(deviceId: String) {
        mutex.withLock {
            val next = currentDevices().map {
                if (it.id == deviceId) it.copy(lastUsedAt = System.currentTimeMillis()) else it
            }
            persist(next)
        }
    }

    suspend fun revoke(deviceId: String) {
        mutex.withLock {
            persist(currentDevices().filterNot { it.id == deviceId })
        }
    }

    suspend fun revokeAll() {
        mutex.withLock { persist(emptyList()) }
    }

    private suspend fun persist(devices: List<PairedDevice>) {
        store.edit { it[KEY_DEVICES] = json.encodeToString(devices) }
    }

    companion object {
        const val PIN_TTL_MS = 5 * 60 * 1000L
        const val MAX_FAILURES = 8
        private val KEY_DEVICES = stringPreferencesKey("devices")
    }

    enum class PairStatus { PENDING, APPROVED, DENIED, UNKNOWN }
}
