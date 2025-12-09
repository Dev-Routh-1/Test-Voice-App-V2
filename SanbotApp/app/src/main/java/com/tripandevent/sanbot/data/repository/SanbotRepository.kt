package com.tripandevent.sanbot.data.repository

import com.google.gson.Gson
import com.tripandevent.sanbot.data.api.SanbotApiService
import com.tripandevent.sanbot.data.models.*
import com.tripandevent.sanbot.utils.ErrorHandler
import com.tripandevent.sanbot.utils.NetworkResult
import com.tripandevent.sanbot.utils.ValidationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SanbotRepository @Inject constructor(
    private val apiService: SanbotApiService
) {

    fun voiceConversation(request: VoiceConversationRequest): Flow<NetworkResult<VoiceConversationResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.voiceConversation(request) })
    }.flowOn(Dispatchers.IO)

    fun transcribeVoice(request: VoiceTranscribeRequest): Flow<NetworkResult<VoiceTranscribeResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.transcribeVoice(request) })
    }.flowOn(Dispatchers.IO)

    fun generateSpeech(request: VoiceGenerateSpeechRequest): Flow<NetworkResult<VoiceGenerateSpeechResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.generateSpeech(request) })
    }.flowOn(Dispatchers.IO)

    fun getPackages(
        category: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        limit: Int? = 20
    ): Flow<NetworkResult<PackageListResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.getPackages(category, minPrice, maxPrice, limit) })
    }.flowOn(Dispatchers.IO)

    fun getPackageDetail(packageId: String): Flow<NetworkResult<PackageDetailResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.getPackageDetail(packageId) })
    }.flowOn(Dispatchers.IO)

    fun createLead(request: CreateLeadRequest): Flow<NetworkResult<CreateLeadResponse>> = flow {
        // Validate input before making API call
        val validationError = validateCreateLeadRequest(request)
        if (validationError != null) {
            emit(NetworkResult.Error(validationError))
            return@flow
        }
        
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.createLead(request) })
    }.flowOn(Dispatchers.IO)

    fun updateLead(leadId: String, request: UpdateLeadRequest): Flow<NetworkResult<UpdateLeadResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.updateLead(leadId, request) })
    }.flowOn(Dispatchers.IO)

    fun addNoteToLead(leadId: String, request: AddNoteRequest): Flow<NetworkResult<AddNoteResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.addNoteToLead(leadId, request) })
    }.flowOn(Dispatchers.IO)

    fun sendSms(request: SendSmsRequest): Flow<NetworkResult<SendSmsResponse>> = flow {
        // Validate phone before SMS
        if (!ValidationUtils.isValidPhone(request.phone)) {
            emit(NetworkResult.Error("Invalid phone number format"))
            return@flow
        }
        
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.sendSms(request) })
    }.flowOn(Dispatchers.IO)

    fun sendWhatsApp(request: SendWhatsAppRequest): Flow<NetworkResult<SendWhatsAppResponse>> = flow {
        // Validate phone before WhatsApp
        if (!ValidationUtils.isValidPhone(request.phone)) {
            emit(NetworkResult.Error("Invalid phone number format"))
            return@flow
        }
        
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.sendWhatsApp(request) })
    }.flowOn(Dispatchers.IO)

    fun sendEmail(request: SendEmailRequest): Flow<NetworkResult<SendEmailResponse>> = flow {
        // Validate email before sending
        if (!ValidationUtils.isValidEmail(request.email)) {
            emit(NetworkResult.Error("Invalid email format"))
            return@flow
        }
        
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.sendEmail(request) })
    }.flowOn(Dispatchers.IO)

    fun bookNow(request: BookNowRequest): Flow<NetworkResult<BookNowResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.bookNow(request) })
    }.flowOn(Dispatchers.IO)

    fun getMedia(type: String? = null, category: String? = null): Flow<NetworkResult<MediaListResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.getMedia(type, category) })
    }.flowOn(Dispatchers.IO)

    fun getConfig(): Flow<NetworkResult<AppConfigResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.getConfig() })
    }.flowOn(Dispatchers.IO)

    fun healthCheck(): Flow<NetworkResult<HealthCheckResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.healthCheck() })
    }.flowOn(Dispatchers.IO)

    /**
     * Enhanced API call handler with comprehensive error management.
     * Parses API error responses and provides detailed error information.
     */
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("Empty response body")
            } else {
                // Try to parse error response
                val errorBody = response.errorBody()?.string()
                val apiError = try {
                    if (errorBody != null) {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.error
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
                
                val errorMessage = apiError?.let { ErrorHandler.getErrorMessage(it) }
                    ?: "HTTP ${response.code()}: ${response.message()}"
                
                NetworkResult.Error(errorMessage, apiError)
            }
        } catch (e: IOException) {
            NetworkResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Validates CreateLeadRequest before sending to API.
     * @return Error message if validation fails, null if valid
     */
    private fun validateCreateLeadRequest(request: CreateLeadRequest): String? {
        // Name validation
        if (!ValidationUtils.isValidName(request.name)) {
            return "Name must be between 2 and 100 characters"
        }
        
        // Phone validation
        if (!ValidationUtils.isValidPhone(request.phone)) {
            return "Invalid phone number format. Use international format (e.g., +971501234567)"
        }
        
        // Email validation (optional but validate if provided)
        if (!request.email.isNullOrEmpty() && !ValidationUtils.isValidEmail(request.email)) {
            return "Invalid email format"
        }
        
        return null
    }
}

/**
 * Helper data class for parsing error responses from API.
 */
data class ErrorResponse(
    @com.google.gson.annotations.SerializedName("success") val success: Boolean = false,
    @com.google.gson.annotations.SerializedName("error") val error: ApiError? = null
)
