package com.tripandevent.sanbot.data.repository

import com.tripandevent.sanbot.data.api.SanbotApiService
import com.tripandevent.sanbot.data.models.*
import com.tripandevent.sanbot.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
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
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.sendSms(request) })
    }.flowOn(Dispatchers.IO)

    fun sendWhatsApp(request: SendWhatsAppRequest): Flow<NetworkResult<SendWhatsAppResponse>> = flow {
        emit(NetworkResult.Loading())
        emit(safeApiCall { apiService.sendWhatsApp(request) })
    }.flowOn(Dispatchers.IO)

    fun sendEmail(request: SendEmailRequest): Flow<NetworkResult<SendEmailResponse>> = flow {
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

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error("Empty response body")
            } else {
                NetworkResult.Error("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}
