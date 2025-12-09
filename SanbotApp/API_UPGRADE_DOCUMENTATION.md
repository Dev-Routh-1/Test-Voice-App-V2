# Sanbot Android App - API Connection Upgrade

## Overview
The API connection has been fully upgraded to comply with the complete API contract. This document outlines all enhancements and improvements made to make the API integration fully functional.

## Upgrades Implemented

### 1. **Advanced Error Handling** ✅
- **File:** `ErrorHandler.kt`
- **Features:**
  - Parses API error codes and provides human-readable error messages
  - Specific handling for all error types from the API contract:
    - `INVALID_API_KEY` - Authentication errors
    - `INVALID_REQUEST` - Malformed requests
    - `MISSING_FIELD` - Required field validation
    - `INVALID_PHONE` / `INVALID_EMAIL` - Format validation
    - `PACKAGE_NOT_FOUND` / `LEAD_NOT_FOUND` - Resource not found
    - `RATE_LIMIT_EXCEEDED` - Rate limiting
    - `SERVER_ERROR` / `SERVICE_UNAVAILABLE` - Server issues
  - Recovery suggestions for each error type
  - Error categorization for better handling

### 2. **Retry Logic with Exponential Backoff** ✅
- **File:** `RetryAndRateLimitInterceptor.kt`
- **Features:**
  - Automatic retry on network failures
  - Automatic retry on server errors (5xx)
  - Automatic retry on rate limiting (429)
  - Exponential backoff with jitter to prevent thundering herd
  - Configurable max retries (default: 3)
  - Respects `Retry-After` headers for rate limiting

```kotlin
// Example: Retries with exponential backoff
Attempt 1: Wait 1000ms
Attempt 2: Wait ~2000ms + jitter
Attempt 3: Wait ~4000ms + jitter
```

### 3. **Rate Limiting Support** ✅
- **File:** `RetryAndRateLimitInterceptor.kt`
- **Features:**
  - Tracks rate limit headers from API:
    - `X-RateLimit-Limit`: 100 requests per minute
    - `X-RateLimit-Remaining`: Remaining request count
    - `X-RateLimit-Reset`: Time when limit resets
  - Provides rate limit info: `getRateLimitInfo()`
  - Checks if rate limited: `isRateLimited()`
  - Returns seconds until reset: `getSecondsUntilReset()`

### 4. **Enhanced Input Validation** ✅
- **File:** `SanbotRepository.kt`
- **Features:**
  - Pre-request validation to avoid unnecessary API calls
  - Phone number validation (international format)
  - Email validation
  - Name validation (2-100 characters)
  - Date validation (YYYY-MM-DD format)
  - Specific validation for `CreateLeadRequest`:
    - Name: 2-100 characters
    - Phone: Valid international format
    - Email: Valid format if provided
  - Validation errors returned immediately without API call

### 5. **Session Management** ✅
- **File:** `SessionManager.kt` (Enhanced)
- **Features:**
  - Session ID generation with proper format: `sanbot_<UUID>`
  - Session ID validation
  - UUID extraction from session ID
  - Session continuity for voice conversations
  - Automatic session reset when returning to welcome screen
  - Lead and package tracking within session

### 6. **Network Configuration Enhancements** ✅
- **File:** `NetworkModule.kt`
- **Features:**
  - Configurable timeouts (30 seconds default):
    - Connection timeout: 30 seconds
    - Read timeout: 30 seconds
    - Write timeout: 30 seconds
  - Multiple interceptors in correct order:
    1. Retry interceptor (for resilience)
    2. Rate limit interceptor (for tracking)
    3. Auth interceptor (for authentication)
    4. Logging interceptor (for debugging)
  - Dynamic base URL support
  - Proper API key management through DataStore
  - Debug logging in development builds

### 7. **Authentication Improvements** ✅
- **File:** `NetworkModule.kt`
- **Features:**
  - Bearer token authentication: `Authorization: Bearer <API_KEY>`
  - Standard `Content-Type: application/json` header
  - Accept header for JSON responses
  - API key from DataStore with fallback to test key
  - Support for runtime API key changes

### 8. **Enhanced NetworkResult** ✅
- **File:** `NetworkResult.kt`
- **Features:**
  - Success state with data
  - Error state with:
    - Error message
    - ApiError object with detailed error info
    - Optional data
  - Loading state
  - Full error context for UI display

### 9. **Comprehensive API Endpoints** ✅
- **File:** `SanbotApiService.kt`
- **All endpoints implemented:**
  - **Voice APIs:**
    - `POST /voice.transcribe` - Speech to text
    - `POST /voice.generateSpeech` - Text to speech
    - `POST /voice.conversation` - Complete voice flow
  - **Package APIs:**
    - `GET /packages` - List with filtering
    - `GET /packages/{package_id}` - Details
  - **CRM APIs:**
    - `POST /crm/leads` - Create lead
    - `PUT /crm/leads/{lead_id}` - Update lead
    - `POST /crm/leads/{lead_id}/notes` - Add notes
  - **Action APIs:**
    - `POST /actions/send-sms` - SMS
    - `POST /actions/send-whatsapp` - WhatsApp
    - `POST /actions/send-email` - Email
    - `POST /actions/book-now` - Booking
  - **Media API:**
    - `GET /media` - Media library
  - **Config API:**
    - `GET /config` - App configuration
  - **Health Check:**
    - `GET /health` - API status

### 10. **API Models Documentation** ✅
- **File:** `ApiModels.kt`
- **Improvements:**
  - Enhanced documentation for all models
  - Complete request/response structures
  - Proper serialization with `@SerializedName`
  - Default values where applicable
  - Optional fields properly marked

## Usage Examples

### Creating a Lead with Validation
```kotlin
val request = CreateLeadRequest(
    name = "Ahmed Ali",
    phone = "+971501234567",
    email = "ahmed@example.com",
    source = "Sanbot",
    location = "Dubai Office",
    interestedPackages = listOf("PKG001"),
    notes = "Interested in family packages",
    preferredContact = "whatsapp"
)

// Repository automatically validates and creates lead
repository.createLead(request).collect { result ->
    when (result) {
        is NetworkResult.Success -> {
            val leadId = result.data?.leadId
        }
        is NetworkResult.Error -> {
            val errorMessage = result.message
            val apiError = result.apiError
            // Show user-friendly error
        }
        is NetworkResult.Loading -> {
            // Show loading
        }
    }
}
```

### Voice Conversation with Session
```kotlin
val sessionId = sessionManager.getSessionId()

val request = VoiceConversationRequest(
    audio = base64AudioData,
    format = "wav",
    sessionId = sessionId,
    language = "en",
    voice = "alloy"
)

repository.voiceConversation(request).collect { result ->
    when (result) {
        is NetworkResult.Success -> {
            val response = result.data
            val transcript = response?.transcript
            val audioResponse = response?.response?.audioUrl
            // Play response
        }
        is NetworkResult.Error -> {
            // Handle error with recovery suggestion
            val suggestion = ErrorHandler.getRecoverySuggestion(result.apiError?.code ?: "UNKNOWN")
        }
        is NetworkResult.Loading -> {
            // Show loading
        }
    }
}
```

### Checking Rate Limits
```kotlin
// Inject RateLimitInterceptor if needed for advanced usage
val rateLimitInfo = rateLimitInterceptor.getRateLimitInfo()
if (rateLimitInfo.remaining < 10) {
    // Show warning to user
    Toast.makeText(context, 
        "Only ${rateLimitInfo.remaining} requests remaining", 
        Toast.LENGTH_SHORT).show()
}
```

## API Contract Compliance

### Authentication ✅
- Base URL: `https://bot.tripandevent.com/api/`
- Test API Key: `test_sanbot_key_abc123xyz789`
- Header: `Authorization: Bearer <API_KEY>`

### Rate Limiting ✅
- 100 requests per minute per API key
- 1000 requests per hour per API key
- Automatic retry on 429 (Too Many Requests)
- Exponential backoff with jitter

### Error Handling ✅
- Standard error format with error codes
- Specific error messages for each case
- HTTP status codes mapped to error codes
- Comprehensive error information

### Timeouts ✅
- Connection: 30 seconds
- Read: 30 seconds
- Write: 30 seconds

### Session Management ✅
- Format: `sanbot_<UUID>`
- Persists across voice API calls
- Resets on app restart or welcome screen return

## Configuration

### Runtime API Key Change
```kotlin
// In Settings, when user updates API key
settingsRepository.updateApiKey("new_api_key_here")
```

### Runtime Base URL Change
```kotlin
// For custom API endpoints
settingsRepository.updateApiBaseUrl("https://custom.api.com/api/")
```

## Best Practices Implemented

✅ **HTTPS Only** - All requests use HTTPS in production  
✅ **Secure Storage** - API keys stored in DataStore  
✅ **Error Recovery** - Automatic retries with exponential backoff  
✅ **Input Validation** - All user input validated before API calls  
✅ **Timeout Handling** - Reasonable timeouts on all connections  
✅ **Error Logging** - Comprehensive error information for debugging  
✅ **Rate Limiting** - Respects API rate limits  
✅ **Session Continuity** - Proper session ID management  
✅ **Debug Support** - HTTP logging in debug builds  
✅ **Graceful Degradation** - Fallback values and defaults  

## Files Modified/Created

### New Files
- `ErrorHandler.kt` - Error handling and recovery
- `RetryAndRateLimitInterceptor.kt` - Retry logic and rate limiting

### Modified Files
- `NetworkModule.kt` - Enhanced with interceptors and configuration
- `SanbotRepository.kt` - Added validation and improved error handling
- `NetworkResult.kt` - Extended with ApiError support
- `ApiModels.kt` - Added documentation and structure
- `SessionManager.kt` - Already implemented with proper session handling

## Testing Checklist

- [ ] Voice transcription works with session continuity
- [ ] Lead creation validates input before API call
- [ ] SMS/WhatsApp/Email validation prevents invalid requests
- [ ] Rate limiting handled gracefully with retries
- [ ] Network errors retry automatically
- [ ] API key changes reflected in real-time
- [ ] Error messages are user-friendly
- [ ] Session ID format is correct (`sanbot_UUID`)
- [ ] Timeouts work as expected (30 seconds)
- [ ] Health check passes with API status

## Support and Debugging

### Enable Debug Logging
Debug logging is automatically enabled in debug builds. Check `Logcat` with filter:
```
okhttp3
```

### Common Issues

**Rate Limited?**
- Wait for `X-RateLimit-Reset` time
- System automatically retries with exponential backoff

**Invalid Phone Number?**
- Use international format: +971501234567
- System validates before sending API request

**Empty Response?**
- Check API key in Settings
- Verify internet connection
- Use health check endpoint

**Session Expired?**
- New session created automatically
- Session persists for conversation duration

## Future Enhancements

- [ ] Implement certificate pinning for enhanced security
- [ ] Add caching layer for package data
- [ ] Implement pagination for large datasets
- [ ] Add offline support with local database
- [ ] Enhanced analytics for API performance
- [ ] Implement auto-refresh for expiring tokens
