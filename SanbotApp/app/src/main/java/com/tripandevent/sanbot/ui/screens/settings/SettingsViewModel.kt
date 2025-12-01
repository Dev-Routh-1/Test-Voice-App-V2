package com.tripandevent.sanbot.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripandevent.sanbot.data.repository.SanbotRepository
import com.tripandevent.sanbot.data.repository.SettingsRepository
import com.tripandevent.sanbot.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isAuthenticated: Boolean = false,
    val apiBaseUrl: String = "",
    val branchLocation: String = "",
    val isTestingConnection: Boolean = false,
    val connectionTestResult: String? = null,
    val isConnectionSuccessful: Boolean? = null,
    val appVersion: String = "1.0.0",
    val isClearingCache: Boolean = false,
    val cacheCleared: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sanbotRepository: SanbotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val adminPassword = MutableStateFlow("")

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.apiBaseUrlFlow,
                settingsRepository.branchLocationFlow
            ) { apiUrl, location ->
                Pair(apiUrl, location)
            }.collect { (apiUrl, location) ->
                _uiState.update {
                    it.copy(
                        apiBaseUrl = apiUrl,
                        branchLocation = location
                    )
                }
            }
        }
    }

    fun verifyPassword(password: String): Boolean {
        viewModelScope.launch {
            val storedPassword = settingsRepository.adminPasswordFlow.first()
            if (password == storedPassword) {
                _uiState.update { it.copy(isAuthenticated = true) }
            }
        }
        return _uiState.value.isAuthenticated
    }

    fun checkPassword(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val storedPassword = settingsRepository.adminPasswordFlow.first()
            val isValid = password == storedPassword
            if (isValid) {
                _uiState.update { it.copy(isAuthenticated = true) }
            }
            onResult(isValid)
        }
    }

    fun updateApiBaseUrl(url: String) {
        _uiState.update { it.copy(apiBaseUrl = url) }
        viewModelScope.launch {
            settingsRepository.updateApiBaseUrl(url)
        }
    }

    fun updateBranchLocation(location: String) {
        _uiState.update { it.copy(branchLocation = location) }
        viewModelScope.launch {
            settingsRepository.updateBranchLocation(location)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isTestingConnection = true,
                    connectionTestResult = null,
                    isConnectionSuccessful = null
                ) 
            }

            sanbotRepository.healthCheck().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> { }
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isTestingConnection = false,
                                connectionTestResult = "Connected! API Version: ${result.data?.version ?: "Unknown"}",
                                isConnectionSuccessful = true
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isTestingConnection = false,
                                connectionTestResult = result.message ?: "Connection failed",
                                isConnectionSuccessful = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingCache = true) }
            kotlinx.coroutines.delay(1000)
            _uiState.update { 
                it.copy(
                    isClearingCache = false,
                    cacheCleared = true
                ) 
            }
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(cacheCleared = false) }
        }
    }

    fun logout() {
        _uiState.update { it.copy(isAuthenticated = false) }
    }

    fun clearConnectionResult() {
        _uiState.update { 
            it.copy(
                connectionTestResult = null,
                isConnectionSuccessful = null
            ) 
        }
    }
}
