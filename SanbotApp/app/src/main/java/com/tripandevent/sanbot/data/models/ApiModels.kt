package com.tripandevent.sanbot.data.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: ApiError? = null
)

data class ApiError(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("field") val field: String? = null,
    @SerializedName("details") val details: Map<String, Any>? = null
)

data class VoiceTranscribeRequest(
    @SerializedName("audio") val audio: String,
    @SerializedName("format") val format: String = "wav",
    @SerializedName("language") val language: String = "en"
)

data class VoiceTranscribeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("text") val text: String?,
    @SerializedName("confidence") val confidence: Float?,
    @SerializedName("language") val language: String?,
    @SerializedName("duration_seconds") val durationSeconds: Float?,
    @SerializedName("error") val error: ApiError? = null
)

data class VoiceGenerateSpeechRequest(
    @SerializedName("text") val text: String,
    @SerializedName("voice") val voice: String = "alloy",
    @SerializedName("language") val language: String = "en"
)

data class VoiceGenerateSpeechResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("audio_url") val audioUrl: String?,
    @SerializedName("duration_seconds") val durationSeconds: Float?,
    @SerializedName("format") val format: String?,
    @SerializedName("error") val error: ApiError? = null
)

data class VoiceConversationRequest(
    @SerializedName("audio") val audio: String,
    @SerializedName("format") val format: String = "wav",
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("language") val language: String = "en",
    @SerializedName("voice") val voice: String = "alloy"
)

data class VoiceConversationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("session_id") val sessionId: String?,
    @SerializedName("transcript") val transcript: String?,
    @SerializedName("response") val response: VoiceResponse?,
    @SerializedName("intent") val intent: String?,
    @SerializedName("confidence") val confidence: Float?,
    @SerializedName("error") val error: ApiError? = null
)

data class VoiceResponse(
    @SerializedName("text") val text: String,
    @SerializedName("audio_url") val audioUrl: String,
    @SerializedName("duration_seconds") val durationSeconds: Float
)

data class PackageListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("total_count") val totalCount: Int?,
    @SerializedName("packages") val packages: List<TourPackage>?,
    @SerializedName("error") val error: ApiError? = null
)

data class PackageDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("package") val tourPackage: TourPackageDetail?,
    @SerializedName("error") val error: ApiError? = null
)

data class TourPackage(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("highlights") val highlights: List<String>,
    @SerializedName("description") val description: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("reviews_count") val reviewsCount: Int,
    @SerializedName("available") val available: Boolean
)

data class TourPackageDetail(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("images") val images: List<String>,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("highlights") val highlights: List<String>,
    @SerializedName("description") val description: String,
    @SerializedName("itinerary") val itinerary: List<ItineraryItem>,
    @SerializedName("inclusions") val inclusions: List<String>,
    @SerializedName("exclusions") val exclusions: List<String>,
    @SerializedName("rating") val rating: Float,
    @SerializedName("reviews_count") val reviewsCount: Int,
    @SerializedName("available") val available: Boolean
)

data class ItineraryItem(
    @SerializedName("time") val time: String,
    @SerializedName("activity") val activity: String
)

data class CreateLeadRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("source") val source: String = "Sanbot",
    @SerializedName("location") val location: String,
    @SerializedName("interested_packages") val interestedPackages: List<String>? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("preferred_contact") val preferredContact: String? = null,
    @SerializedName("destination") val destination: String? = null,
    @SerializedName("number_of_travelers") val numberOfTravelers: Int? = null,
    @SerializedName("travel_date") val travelDate: String? = null
)

data class CreateLeadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("lead_id") val leadId: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("error") val error: ApiError? = null
)

data class UpdateLeadRequest(
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("interested_packages") val interestedPackages: List<String>? = null
)

data class UpdateLeadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("lead_id") val leadId: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("error") val error: ApiError? = null
)

data class AddNoteRequest(
    @SerializedName("note") val note: String,
    @SerializedName("author") val author: String = "Sanbot"
)

data class AddNoteResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("note_id") val noteId: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: ApiError? = null
)

data class SendSmsRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("package_id") val packageId: String,
    @SerializedName("template") val template: String = "package_details",
    @SerializedName("lead_id") val leadId: String? = null
)

data class SendSmsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message_id") val messageId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: ApiError? = null
)

data class SendWhatsAppRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("package_id") val packageId: String,
    @SerializedName("template") val template: String = "package_details",
    @SerializedName("lead_id") val leadId: String? = null
)

data class SendWhatsAppResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message_id") val messageId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: ApiError? = null
)

data class SendEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("package_ids") val packageIds: List<String>,
    @SerializedName("template") val template: String = "quote",
    @SerializedName("lead_id") val leadId: String? = null,
    @SerializedName("customer_name") val customerName: String
)

data class SendEmailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("email_id") val emailId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: ApiError? = null
)

data class BookNowRequest(
    @SerializedName("lead_id") val leadId: String,
    @SerializedName("package_id") val packageId: String,
    @SerializedName("travel_date") val travelDate: String,
    @SerializedName("number_of_people") val numberOfPeople: Int,
    @SerializedName("special_requests") val specialRequests: String? = null
)

data class BookNowResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("booking_id") val bookingId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: ApiError? = null
)

data class MediaListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("media") val media: List<MediaItem>?,
    @SerializedName("error") val error: ApiError? = null
)

data class MediaItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String,
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("duration_seconds") val durationSeconds: Int? = null,
    @SerializedName("category") val category: String
)

data class AppConfigResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("config") val config: AppConfig?,
    @SerializedName("error") val error: ApiError? = null
)

data class AppConfig(
    @SerializedName("app_version") val appVersion: String,
    @SerializedName("min_supported_version") val minSupportedVersion: String,
    @SerializedName("welcome_message") val welcomeMessage: String,
    @SerializedName("idle_timeout_seconds") val idleTimeoutSeconds: Int,
    @SerializedName("default_language") val defaultLanguage: String,
    @SerializedName("supported_languages") val supportedLanguages: List<String>,
    @SerializedName("features") val features: AppFeatures
)

data class AppFeatures(
    @SerializedName("voice_enabled") val voiceEnabled: Boolean,
    @SerializedName("video_enabled") val videoEnabled: Boolean,
    @SerializedName("whatsapp_enabled") val whatsappEnabled: Boolean,
    @SerializedName("sms_enabled") val smsEnabled: Boolean,
    @SerializedName("email_enabled") val emailEnabled: Boolean
)

data class HealthCheckResponse(
    @SerializedName("status") val status: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("version") val version: String
)
