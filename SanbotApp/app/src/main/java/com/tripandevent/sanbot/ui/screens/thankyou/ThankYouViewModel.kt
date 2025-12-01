package com.tripandevent.sanbot.ui.screens.thankyou

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripandevent.sanbot.data.models.*
import com.tripandevent.sanbot.data.repository.SanbotRepository
import com.tripandevent.sanbot.utils.NetworkResult
import com.tripandevent.sanbot.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThankYouUiState(
    val leadId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val selectedPackageIds: List<String> = emptyList(),
    val isSendingSms: Boolean = false,
    val isSendingWhatsApp: Boolean = false,
    val isSendingEmail: Boolean = false,
    val actionSuccess: String? = null,
    val actionError: String? = null
)

@HiltViewModel
class ThankYouViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SanbotRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val leadId: String = savedStateHandle["leadId"] ?: ""

    private val _uiState = MutableStateFlow(ThankYouUiState())
    val uiState: StateFlow<ThankYouUiState> = _uiState.asStateFlow()

    init {
        val customerInfo = sessionManager.getCustomerInfo()
        _uiState.update {
            it.copy(
                leadId = leadId,
                customerName = customerInfo?.name ?: "",
                customerPhone = customerInfo?.phone ?: "",
                customerEmail = customerInfo?.email ?: "",
                selectedPackageIds = sessionManager.getSelectedPackages()
            )
        }
    }

    fun sendSms() {
        val state = _uiState.value
        if (state.customerPhone.isBlank() || state.selectedPackageIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingSms = true, actionError = null, actionSuccess = null) }

            val request = SendSmsRequest(
                phone = state.customerPhone,
                packageId = state.selectedPackageIds.first(),
                template = "package_details",
                leadId = state.leadId.takeIf { it.isNotBlank() }
            )

            repository.sendSms(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> { }
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSendingSms = false,
                                actionSuccess = result.data?.message ?: "SMS sent successfully"
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSendingSms = false,
                                actionError = result.message ?: "Failed to send SMS"
                            )
                        }
                    }
                }
            }
        }
    }

    fun sendWhatsApp() {
        val state = _uiState.value
        if (state.customerPhone.isBlank() || state.selectedPackageIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingWhatsApp = true, actionError = null, actionSuccess = null) }

            val request = SendWhatsAppRequest(
                phone = state.customerPhone,
                packageId = state.selectedPackageIds.first(),
                template = "package_details",
                leadId = state.leadId.takeIf { it.isNotBlank() }
            )

            repository.sendWhatsApp(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> { }
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSendingWhatsApp = false,
                                actionSuccess = result.data?.message ?: "WhatsApp message sent successfully"
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSendingWhatsApp = false,
                                actionError = result.message ?: "Failed to send WhatsApp message"
                            )
                        }
                    }
                }
            }
        }
    }

    fun sendEmail() {
        val state = _uiState.value
        if (state.customerEmail.isBlank() || state.selectedPackageIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingEmail = true, actionError = null, actionSuccess = null) }

            val request = SendEmailRequest(
                email = state.customerEmail,
                packageIds = state.selectedPackageIds,
                template = "quote",
                leadId = state.leadId.takeIf { it.isNotBlank() },
                customerName = state.customerName
            )

            repository.sendEmail(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> { }
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSendingEmail = false,
                                actionSuccess = result.data?.message ?: "Email sent successfully"
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSendingEmail = false,
                                actionError = result.message ?: "Failed to send email"
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionSuccess = null, actionError = null) }
    }

    fun clearSession() {
        sessionManager.clearSession()
    }
}
