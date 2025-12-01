package com.tripandevent.sanbot.ui.screens.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripandevent.sanbot.data.models.CreateLeadRequest
import com.tripandevent.sanbot.data.repository.SanbotRepository
import com.tripandevent.sanbot.data.repository.SettingsRepository
import com.tripandevent.sanbot.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactFormState(
    val name: String = "",
    val nameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val destination: String = "",
    val numberOfTravelers: String = "1",
    val travelDate: String = "",
    val notes: String = "",
    val selectedPackageIds: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val leadId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val repository: SanbotRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _formState = MutableStateFlow(ContactFormState())
    val formState: StateFlow<ContactFormState> = _formState.asStateFlow()

    init {
        _formState.update { 
            it.copy(selectedPackageIds = sessionManager.getSelectedPackages()) 
        }
        
        sessionManager.getCustomerInfo()?.let { info ->
            _formState.update {
                it.copy(
                    name = info.name,
                    phone = info.phone,
                    email = info.email ?: "",
                    destination = info.destination ?: "",
                    numberOfTravelers = info.numberOfTravelers?.toString() ?: "1",
                    travelDate = info.travelDate ?: "",
                    notes = info.notes ?: ""
                )
            }
        }
    }

    fun updateName(name: String) {
        _formState.update { 
            it.copy(
                name = name,
                nameError = if (name.isNotBlank() && !ValidationUtils.isValidName(name)) {
                    "Name must be 2-100 characters"
                } else null
            ) 
        }
    }

    fun updatePhone(phone: String) {
        _formState.update { 
            it.copy(
                phone = phone,
                phoneError = if (phone.isNotBlank() && !ValidationUtils.isValidPhone(phone)) {
                    "Invalid phone number format"
                } else null
            ) 
        }
    }

    fun updateEmail(email: String) {
        _formState.update { 
            it.copy(
                email = email,
                emailError = if (email.isNotBlank() && !ValidationUtils.isValidEmail(email)) {
                    "Invalid email format"
                } else null
            ) 
        }
    }

    fun updateDestination(destination: String) {
        _formState.update { it.copy(destination = destination) }
    }

    fun updateNumberOfTravelers(count: String) {
        _formState.update { it.copy(numberOfTravelers = count) }
    }

    fun updateTravelDate(date: String) {
        _formState.update { it.copy(travelDate = date) }
    }

    fun updateNotes(notes: String) {
        _formState.update { it.copy(notes = notes) }
    }

    fun togglePackageSelection(packageId: String) {
        _formState.update { state ->
            val newList = if (state.selectedPackageIds.contains(packageId)) {
                state.selectedPackageIds - packageId
            } else {
                state.selectedPackageIds + packageId
            }
            state.copy(selectedPackageIds = newList)
        }
    }

    fun isFormValid(): Boolean {
        val state = _formState.value
        return state.name.isNotBlank() && 
               ValidationUtils.isValidName(state.name) &&
               state.phone.isNotBlank() && 
               ValidationUtils.isValidPhone(state.phone) &&
               (state.email.isBlank() || ValidationUtils.isValidEmail(state.email))
    }

    fun submitForm() {
        if (!isFormValid()) {
            validateAllFields()
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val branchLocation = settingsRepository.branchLocationFlow.first()
            val state = _formState.value

            val request = CreateLeadRequest(
                name = state.name,
                phone = ValidationUtils.formatPhoneNumber(state.phone),
                email = state.email.takeIf { it.isNotBlank() },
                source = "Sanbot",
                location = branchLocation,
                interestedPackages = state.selectedPackageIds.takeIf { it.isNotEmpty() },
                notes = buildNotesString(state),
                destination = state.destination.takeIf { it.isNotBlank() },
                numberOfTravelers = state.numberOfTravelers.toIntOrNull(),
                travelDate = state.travelDate.takeIf { it.isNotBlank() }
            )

            repository.createLead(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> { }
                    is NetworkResult.Success -> {
                        val leadId = result.data?.leadId
                        if (leadId != null) {
                            sessionManager.setLeadId(leadId)
                            sessionManager.setCustomerInfo(
                                CustomerInfo(
                                    name = state.name,
                                    phone = state.phone,
                                    email = state.email.takeIf { it.isNotBlank() },
                                    destination = state.destination.takeIf { it.isNotBlank() },
                                    numberOfTravelers = state.numberOfTravelers.toIntOrNull(),
                                    travelDate = state.travelDate.takeIf { it.isNotBlank() },
                                    notes = state.notes.takeIf { it.isNotBlank() }
                                )
                            )
                        }
                        _formState.update { 
                            it.copy(
                                isSubmitting = false,
                                isSuccess = true,
                                leadId = leadId
                            ) 
                        }
                    }
                    is NetworkResult.Error -> {
                        _formState.update { 
                            it.copy(
                                isSubmitting = false,
                                errorMessage = result.message ?: "Failed to submit form"
                            ) 
                        }
                    }
                }
            }
        }
    }

    private fun validateAllFields() {
        val state = _formState.value
        _formState.update {
            it.copy(
                nameError = if (state.name.isBlank()) "Name is required" 
                           else if (!ValidationUtils.isValidName(state.name)) "Name must be 2-100 characters" 
                           else null,
                phoneError = if (state.phone.isBlank()) "Phone number is required" 
                            else if (!ValidationUtils.isValidPhone(state.phone)) "Invalid phone number format" 
                            else null,
                emailError = if (state.email.isNotBlank() && !ValidationUtils.isValidEmail(state.email)) 
                            "Invalid email format" else null
            )
        }
    }

    private fun buildNotesString(state: ContactFormState): String {
        val parts = mutableListOf<String>()
        if (state.destination.isNotBlank()) parts.add("Destination: ${state.destination}")
        if (state.numberOfTravelers.isNotBlank()) parts.add("Travelers: ${state.numberOfTravelers}")
        if (state.travelDate.isNotBlank()) parts.add("Travel Date: ${state.travelDate}")
        if (state.notes.isNotBlank()) parts.add("Notes: ${state.notes}")
        return parts.joinToString(". ")
    }

    fun clearError() {
        _formState.update { it.copy(errorMessage = null) }
    }
}
