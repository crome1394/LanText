package app.lantext.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.lantext.util.ListenPort
import app.lantext.widget.WidgetTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "lantext_settings")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore

    val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            enabled = prefs[KEY_ENABLED] == true,
            onboardingDone = prefs[KEY_ONBOARDING] == true,
            allowedSsids = prefs[KEY_ALLOWED] ?: emptySet(),
            knownSsids = prefs[KEY_KNOWN] ?: emptySet(),
            listenPort = prefs[KEY_PORT] ?: AppSettings.DEFAULT_PORT,
            webPalette = prefs[KEY_PALETTE] ?: "fern",
            webMode = prefs[KEY_MODE] ?: "auto",
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setEnabled(enabled: Boolean) {
        store.edit { it[KEY_ENABLED] = enabled }
    }

    suspend fun setOnboardingDone() {
        store.edit { it[KEY_ONBOARDING] = true }
    }

    suspend fun addAllowedSsid(ssid: String) {
        val clean = ssid.trim().trim('"')
        if (clean.isEmpty()) return
        store.edit { prefs ->
            prefs[KEY_ALLOWED] = (prefs[KEY_ALLOWED] ?: emptySet()) + clean
            prefs[KEY_KNOWN] = (prefs[KEY_KNOWN] ?: emptySet()) + clean
        }
    }

    suspend fun removeAllowedSsid(ssid: String) {
        store.edit { prefs ->
            prefs[KEY_ALLOWED] = (prefs[KEY_ALLOWED] ?: emptySet()) - ssid
        }
    }

    suspend fun forgetSsid(ssid: String) {
        store.edit { prefs ->
            prefs[KEY_ALLOWED] = (prefs[KEY_ALLOWED] ?: emptySet()) - ssid
            prefs[KEY_KNOWN] = (prefs[KEY_KNOWN] ?: emptySet()) - ssid
        }
    }

    suspend fun setListenPort(port: Int) {
        require(ListenPort.isValid(port)) {
            "Port must be between ${ListenPort.MIN} and ${ListenPort.MAX}"
        }
        store.edit { it[KEY_PORT] = port }
    }

    suspend fun setAppearance(palette: String, mode: String) {
        val p = WidgetTheme.normalizePalette(palette)
        val m = WidgetTheme.normalizeMode(mode)
        store.edit {
            it[KEY_PALETTE] = p
            it[KEY_MODE] = m
        }
    }

    suspend fun rememberSsid(ssid: String) {
        val clean = ssid.trim().trim('"')
        if (clean.isEmpty() || clean == UNKNOWN_SSID) return
        if (clean in current().knownSsids) return
        store.edit { prefs ->
            prefs[KEY_KNOWN] = (prefs[KEY_KNOWN] ?: emptySet()) + clean
        }
    }

    companion object {
        const val UNKNOWN_SSID = "<unknown ssid>"
        private val KEY_ENABLED = booleanPreferencesKey("enabled")
        private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_done")
        private val KEY_ALLOWED = stringSetPreferencesKey("allowed_ssids")
        private val KEY_KNOWN = stringSetPreferencesKey("known_ssids")
        private val KEY_PORT = intPreferencesKey("listen_port")
        private val KEY_PALETTE = stringPreferencesKey("web_palette")
        private val KEY_MODE = stringPreferencesKey("web_mode")
    }
}
