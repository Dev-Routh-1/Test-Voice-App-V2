package com.tripandevent.sanbot.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tripandevent.sanbot.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val API_BASE_URL_KEY = stringPreferencesKey("api_base_url")
        private val API_KEY_KEY = stringPreferencesKey("api_key")
        private val BRANCH_LOCATION_KEY = stringPreferencesKey("branch_location")
        private val ADMIN_PASSWORD_KEY = stringPreferencesKey("admin_password")
        
        const val DEFAULT_API_BASE_URL = "https://bot.tripandevent.com/api/"
        const val DEFAULT_API_KEY = "test_sanbot_key_abc123xyz789"
        const val DEFAULT_BRANCH_LOCATION = "Dubai Office"
        const val DEFAULT_ADMIN_PASSWORD = "admin123"
    }

    private var apiKeyCallback: ((String) -> Unit)? = null
    private var baseUrlCallback: ((String) -> Unit)? = null
    private var observerJob: kotlinx.coroutines.Job? = null

    fun observeSettings(
        onApiKeyChanged: (String) -> Unit,
        onBaseUrlChanged: (String) -> Unit
    ) {
        apiKeyCallback = onApiKeyChanged
        baseUrlCallback = onBaseUrlChanged
        
        observerJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            dataStore.data.collect { preferences ->
                val apiKey = preferences[API_KEY_KEY] ?: DEFAULT_API_KEY
                val baseUrl = preferences[API_BASE_URL_KEY] ?: DEFAULT_API_BASE_URL
                apiKeyCallback?.invoke(apiKey)
                baseUrlCallback?.invoke(baseUrl)
            }
        }
    }

    val apiBaseUrlFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[API_BASE_URL_KEY] ?: DEFAULT_API_BASE_URL
    }

    val apiKeyFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[API_KEY_KEY] ?: DEFAULT_API_KEY
    }

    val branchLocationFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[BRANCH_LOCATION_KEY] ?: DEFAULT_BRANCH_LOCATION
    }

    val adminPasswordFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[ADMIN_PASSWORD_KEY] ?: DEFAULT_ADMIN_PASSWORD
    }

    suspend fun updateApiBaseUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[API_BASE_URL_KEY] = url
        }
    }

    suspend fun updateApiKey(key: String) {
        dataStore.edit { preferences ->
            preferences[API_KEY_KEY] = key
        }
    }

    suspend fun updateBranchLocation(location: String) {
        dataStore.edit { preferences ->
            preferences[BRANCH_LOCATION_KEY] = location
        }
    }

    suspend fun updateAdminPassword(password: String) {
        dataStore.edit { preferences ->
            preferences[ADMIN_PASSWORD_KEY] = password
        }
    }

    suspend fun clearAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
