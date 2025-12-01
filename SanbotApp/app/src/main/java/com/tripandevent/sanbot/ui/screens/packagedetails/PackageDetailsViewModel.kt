package com.tripandevent.sanbot.ui.screens.packagedetails

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

data class PackageDetailsUiState(
    val isLoading: Boolean = true,
    val packageDetail: TourPackageDetail? = null,
    val errorMessage: String? = null,
    val isSendingSms: Boolean = false,
    val isSendingWhatsApp: Boolean = false,
    val isSendingEmail: Boolean = false,
    val actionSuccess: String? = null,
    val actionError: String? = null
)

@HiltViewModel
class PackageDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SanbotRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val packageId: String = checkNotNull(savedStateHandle["packageId"])

    private val _uiState = MutableStateFlow(PackageDetailsUiState())
    val uiState: StateFlow<PackageDetailsUiState> = _uiState.asStateFlow()

    init {
        loadPackageDetail()
    }

    fun loadPackageDetail() {
        viewModelScope.launch {
            repository.getPackageDetail(packageId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is NetworkResult.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                packageDetail = result.data?.tourPackage,
                                errorMessage = null
                            ) 
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Failed to load package details"
                            ) 
                        }
                    }
                }
            }
        }
    }

    fun sendSms(phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingSms = true, actionError = null) }
            
            val request = SendSmsRequest(
                phone = phone,
                packageId = packageId,
                template = "package_details",
                leadId = sessionManager.getLeadId()
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

    fun sendWhatsApp(phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingWhatsApp = true, actionError = null) }
            
            val request = SendWhatsAppRequest(
                phone = phone,
                packageId = packageId,
                template = "package_details",
                leadId = sessionManager.getLeadId()
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

    fun sendEmail(email: String, customerName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingEmail = true, actionError = null) }
            
            val request = SendEmailRequest(
                email = email,
                packageIds = listOf(packageId),
                template = "quote",
                leadId = sessionManager.getLeadId(),
                customerName = customerName
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

    fun addToSelectedPackages() {
        sessionManager.addSelectedPackage(packageId)
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionSuccess = null, actionError = null) }
    }
}
