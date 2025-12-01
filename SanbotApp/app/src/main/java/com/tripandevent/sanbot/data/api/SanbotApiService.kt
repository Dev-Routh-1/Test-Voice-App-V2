package com.tripandevent.sanbot.data.api

import com.tripandevent.sanbot.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface SanbotApiService {

    @POST("voice.transcribe")
    suspend fun transcribeVoice(
        @Body request: VoiceTranscribeRequest
    ): Response<VoiceTranscribeResponse>

    @POST("voice.generateSpeech")
    suspend fun generateSpeech(
        @Body request: VoiceGenerateSpeechRequest
    ): Response<VoiceGenerateSpeechResponse>

    @POST("voice.conversation")
    suspend fun voiceConversation(
        @Body request: VoiceConversationRequest
    ): Response<VoiceConversationResponse>

    @GET("packages")
    suspend fun getPackages(
        @Query("category") category: String? = null,
        @Query("min_price") minPrice: Int? = null,
        @Query("max_price") maxPrice: Int? = null,
        @Query("limit") limit: Int? = 20
    ): Response<PackageListResponse>

    @GET("packages/{package_id}")
    suspend fun getPackageDetail(
        @Path("package_id") packageId: String
    ): Response<PackageDetailResponse>

    @POST("crm/leads")
    suspend fun createLead(
        @Body request: CreateLeadRequest
    ): Response<CreateLeadResponse>

    @PUT("crm/leads/{lead_id}")
    suspend fun updateLead(
        @Path("lead_id") leadId: String,
        @Body request: UpdateLeadRequest
    ): Response<UpdateLeadResponse>

    @POST("crm/leads/{lead_id}/notes")
    suspend fun addNoteToLead(
        @Path("lead_id") leadId: String,
        @Body request: AddNoteRequest
    ): Response<AddNoteResponse>

    @POST("actions/send-sms")
    suspend fun sendSms(
        @Body request: SendSmsRequest
    ): Response<SendSmsResponse>

    @POST("actions/send-whatsapp")
    suspend fun sendWhatsApp(
        @Body request: SendWhatsAppRequest
    ): Response<SendWhatsAppResponse>

    @POST("actions/send-email")
    suspend fun sendEmail(
        @Body request: SendEmailRequest
    ): Response<SendEmailResponse>

    @POST("actions/book-now")
    suspend fun bookNow(
        @Body request: BookNowRequest
    ): Response<BookNowResponse>

    @GET("media")
    suspend fun getMedia(
        @Query("type") type: String? = null,
        @Query("category") category: String? = null
    ): Response<MediaListResponse>

    @GET("config")
    suspend fun getConfig(): Response<AppConfigResponse>

    @GET("health")
    suspend fun healthCheck(): Response<HealthCheckResponse>
}
