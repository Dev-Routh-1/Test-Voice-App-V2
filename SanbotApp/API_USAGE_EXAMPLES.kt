// Example ViewModel implementation using the upgraded API
package com.tripandevent.sanbot.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripandevent.sanbot.data.models.CreateLeadRequest
import com.tripandevent.sanbot.data.repository.SanbotRepository
import com.tripandevent.sanbot.utils.NetworkResult
import com.tripandevent.sanbot.utils.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateLeadViewModel @Inject constructor(
    private val repository: SanbotRepository
) : ViewModel() {
    
    private val _leadCreationState = MutableStateFlow<LeadCreationState>(LeadCreationState.Idle)
    val leadCreationState: StateFlow<LeadCreationState> = _leadCreationState
    
    fun createLead(request: CreateLeadRequest) {
        viewModelScope.launch {
            repository.createLead(request).collect { result ->
                _leadCreationState.value = when (result) {
                    is NetworkResult.Loading -> LeadCreationState.Loading
                    is NetworkResult.Success -> {
                        val leadId = result.data?.leadId ?: "UNKNOWN"
                        LeadCreationState.Success(leadId)
                    }
                    is NetworkResult.Error -> {
                        // Get user-friendly error message
                        val errorMessage = result.message ?: "Unknown error"
                        val errorCode = result.apiError?.code ?: "UNKNOWN_ERROR"
                        val recoverySuggestion = ErrorHandler.getRecoverySuggestion(errorCode)
                        
                        LeadCreationState.Error(
                            message = errorMessage,
                            errorCode = errorCode,
                            recoverySuggestion = recoverySuggestion
                        )
                    }
                }
            }
        }
    }
}

sealed class LeadCreationState {
    object Idle : LeadCreationState()
    object Loading : LeadCreationState()
    data class Success(val leadId: String) : LeadCreationState()
    data class Error(
        val message: String,
        val errorCode: String,
        val recoverySuggestion: String
    ) : LeadCreationState()
}

// In Compose UI:
@Composable
fun CreateLeadScreen(viewModel: CreateLeadViewModel) {
    val state by viewModel.leadCreationState.collectAsState()
    
    when (state) {
        is LeadCreationState.Loading -> {
            CircularProgressIndicator()
        }
        is LeadCreationState.Success -> {
            val leadId = (state as LeadCreationState.Success).leadId
            Text("Lead created successfully: $leadId")
        }
        is LeadCreationState.Error -> {
            val error = state as LeadCreationState.Error
            Column {
                Text(
                    text = error.message,
                    color = Color.Red,
                    fontSize = 16.sp
                )
                Text(
                    text = "Suggestion: ${error.recoverySuggestion}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(onClick = { /* Retry logic */ }) {
                    Text("Retry")
                }
            }
        }
        is LeadCreationState.Idle -> {
            // Show form
        }
    }
}
