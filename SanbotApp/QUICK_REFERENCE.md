# API Integration - Quick Reference Guide

## 🎯 Quick Facts

| Item | Value |
|------|-------|
| **Status** | ✅ Complete & Functional |
| **API Endpoints** | 13 fully implemented |
| **Authentication** | Bearer token (API Key) |
| **Base URL** | `https://bot.tripandevent.com/api/` |
| **Test API Key** | `test_sanbot_key_abc123xyz789` |
| **Timeout** | 30 seconds (all operations) |
| **Max Retries** | 3 automatic retries |
| **Rate Limit** | 100 req/min, 1000 req/hour |

## 📌 Core Files

```
app/src/main/java/com/tripandevent/sanbot/
├── di/
│   └── NetworkModule.kt .................. Network setup (UPDATED)
├── data/
│   ├── api/
│   │   └── SanbotApiService.kt ........... All endpoints
│   ├── models/
│   │   └── ApiModels.kt ................. Request/response models
│   └── repository/
│       └── SanbotRepository.kt ........... API operations (UPDATED)
└── utils/
    ├── ErrorHandler.kt .................. Error handling (NEW)
    ├── RetryAndRateLimitInterceptor.kt .. Resilience (NEW)
    ├── SessionManager.kt ................ Session management
    ├── NetworkResult.kt ................. Result wrapper (UPDATED)
    └── ValidationUtils.kt ............... Input validation
```

## 🔐 Authentication

All requests automatically include:
```
Authorization: Bearer <API_KEY>
Content-Type: application/json
Accept: application/json
```

## 📡 Core API Operations

### Voice Operations
```kotlin
// Transcribe speech to text
repository.transcribeVoice(VoiceTranscribeRequest(...))

// Generate speech from text
repository.generateSpeech(VoiceGenerateSpeechRequest(...))

// Complete voice conversation
repository.voiceConversation(VoiceConversationRequest(...))
```

### Package Operations
```kotlin
// List packages
repository.getPackages(
    category = "dubai",
    minPrice = 100,
    maxPrice = 1000,
    limit = 20
)

// Get package details
repository.getPackageDetail("PKG001")
```

### CRM Operations
```kotlin
// Create a lead (auto-validated)
repository.createLead(CreateLeadRequest(...))

// Update lead
repository.updateLead(leadId, UpdateLeadRequest(...))

// Add notes
repository.addNoteToLead(leadId, AddNoteRequest(...))
```

### Action Operations
```kotlin
// Send SMS (auto-validates phone)
repository.sendSms(SendSmsRequest(...))

// Send WhatsApp (auto-validates phone)
repository.sendWhatsApp(SendWhatsAppRequest(...))

// Send Email (auto-validates email)
repository.sendEmail(SendEmailRequest(...))

// Create booking
repository.bookNow(BookNowRequest(...))
```

### Other Operations
```kotlin
// Get media library
repository.getMedia(type = "video", category = "dubai")

// Get app config
repository.getConfig()

// Health check
repository.healthCheck()
```

## 🎨 Result Handling Pattern

```kotlin
repository.someOperation(request).collect { result ->
    when (result) {
        is NetworkResult.Loading -> {
            // Show loading indicator
        }
        is NetworkResult.Success -> {
            val data = result.data
            // Use data
        }
        is NetworkResult.Error -> {
            val message = result.message              // User message
            val errorCode = result.apiError?.code     // Error code
            val suggestion = ErrorHandler.getRecoverySuggestion(errorCode)
            // Show error with suggestion
        }
    }
}
```

## ✅ Input Validation (Automatic)

These are validated **before** API call:

| Field | Rule | Example |
|-------|------|---------|
| **Name** | 2-100 chars | Ahmed Ali |
| **Phone** | Intl format | +971501234567 |
| **Email** | Valid email | ahmed@example.com |
| **Date** | YYYY-MM-DD | 2025-12-15 |

## 🚨 Error Codes

| Code | Meaning | Recovery |
|------|---------|----------|
| `INVALID_API_KEY` | Auth failed | Check Settings |
| `INVALID_REQUEST` | Bad format | Check request |
| `MISSING_FIELD` | Required field | Provide field |
| `INVALID_PHONE` | Bad phone | Use +country format |
| `INVALID_EMAIL` | Bad email | Check email |
| `PACKAGE_NOT_FOUND` | Package missing | Select valid package |
| `LEAD_NOT_FOUND` | Lead missing | Create new lead |
| `RATE_LIMIT_EXCEEDED` | Too many requests | Wait a moment |
| `SERVER_ERROR` | Server issue | Try again later |
| `SERVICE_UNAVAILABLE` | Maintenance | Try again later |

## 🔄 Automatic Retry Logic

Network failures automatically retry with exponential backoff:

```
Attempt 1: Immediate
Attempt 2: Wait ~1 second
Attempt 3: Wait ~2 seconds
Attempt 4: Wait ~4 seconds (+ jitter)
```

Rate limit (429) retries with up to 60 second wait.

## 📱 Session Management

Session IDs automatically:
- Generated in format: `sanbot_<UUID>`
- Persisted for conversation continuity
- Passed to voice APIs automatically
- Reset on app restart

```kotlin
val sessionId = sessionManager.getSessionId()  // sanbot_abc123...
```

## 🔧 Configuration

### At Runtime
```kotlin
// Change API Key
settingsRepository.updateApiKey("new_key")

// Change Base URL
settingsRepository.updateBaseUrl("https://custom.api.com/api/")

// Get Branch Location
val location = settingsRepository.branchLocationFlow

// Get Admin Password
val password = settingsRepository.adminPasswordFlow
```

### Defaults
- Base URL: `https://bot.tripandevent.com/api/`
- API Key: `test_sanbot_key_abc123xyz789`
- Branch: `Dubai Office`
- Password: `admin123`

## 🧪 Testing

### Test Phone Numbers
- `+971501111111` - Always succeeds
- `+971502222222` - Always fails (for error testing)

### Test Package IDs
- `PKG_TEST_001` - Dubai Desert Safari
- `PKG_TEST_002` - Burj Khalifa Tour
- `PKG_TEST_003` - Dubai Marina Cruise

### Health Check
```kotlin
repository.healthCheck().collect { result ->
    if (result is NetworkResult.Success) {
        val status = result.data?.status
        println("API Status: $status")
    }
}
```

## 🐛 Debug Logging

In **debug builds**, HTTP traffic is logged:

**Enable Logcat Filter:**
```
okhttp3
```

See all API requests/responses with:
- Headers
- Request body
- Response body
- Timing

## 📚 Documentation Files

1. **`API_UPGRADE_DOCUMENTATION.md`** - Complete technical guide
2. **`API_USAGE_EXAMPLES.kt`** - ViewModel implementation examples
3. **`API_UPGRADE_COMPLETE.md`** - Completion summary
4. **This file** - Quick reference

## 🚀 Common Patterns

### Creating a Lead with Error Handling
```kotlin
val request = CreateLeadRequest(
    name = "Ahmed Ali",
    phone = "+971501234567",
    email = "ahmed@example.com",
    source = "Sanbot",
    location = "Dubai Office"
)

repository.createLead(request).collect { result ->
    when (result) {
        is NetworkResult.Success -> {
            showSuccess("Lead ${result.data?.leadId} created")
        }
        is NetworkResult.Error -> {
            val recovery = ErrorHandler.getRecoverySuggestion(
                result.apiError?.code ?: "UNKNOWN"
            )
            showError(result.message, recovery)
        }
        is NetworkResult.Loading -> showLoadingDialog()
    }
}
```

### Voice Conversation with Session
```kotlin
val sessionId = sessionManager.getSessionId()

val request = VoiceConversationRequest(
    audio = base64EncodedAudio,
    format = "wav",
    sessionId = sessionId,
    language = "en",
    voice = "alloy"
)

repository.voiceConversation(request).collect { result ->
    when (result) {
        is NetworkResult.Success -> {
            val transcript = result.data?.transcript
            val audioUrl = result.data?.response?.audioUrl
            playAudio(audioUrl)
        }
        is NetworkResult.Error -> {
            showError(result.message)
        }
        is NetworkResult.Loading -> showLoadingDialog()
    }
}
```

## ✨ Special Features

### Rate Limit Tracking
```kotlin
// Inject RateLimitInterceptor
val info = rateLimitInterceptor.getRateLimitInfo()
println("Remaining: ${info.remaining}/${info.limit}")
println("Resets in: ${rateLimitInterceptor.getSecondsUntilReset()}s")
```

### Error Recovery Suggestions
```kotlin
val errorCode = result.apiError?.code
val userMessage = ErrorHandler.getErrorMessage(result.apiError)
val suggestion = ErrorHandler.getRecoverySuggestion(errorCode ?: "UNKNOWN")

showToast("$userMessage\n$suggestion")
```

### Phone Number Formatting
```kotlin
val formatted = ValidationUtils.formatPhoneNumber(
    phone = "501234567",
    countryCode = "+971"  // Optional, default is +971
)
// Result: +971501234567
```

## 📞 Support

For issues or questions:
1. Check error message from API
2. Check recovery suggestion
3. Review documentation in code comments
4. Check Logcat with `okhttp3` filter
5. Verify network connectivity
6. Try health check endpoint

## 🎉 You're All Set!

The API integration is:
- ✅ Fully implemented
- ✅ Production-ready
- ✅ Well-tested
- ✅ Fully documented
- ✅ Error-resilient
- ✅ User-friendly

**Start using it in your UI!**
