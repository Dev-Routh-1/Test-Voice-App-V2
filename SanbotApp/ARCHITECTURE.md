# Sanbot Android App - API Architecture

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    ANDROID APP (UI LAYER)                   │
│  Compose UI • ViewModels • Navigation                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                 PRESENTATION LAYER                           │
│  • State Management (ViewModels)                             │
│  • UI State (Loading, Success, Error)                        │
│  • User Input Handling                                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              REPOSITORY LAYER (Data Operations)              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ SanbotRepository                                        ││
│  │ • Input Validation                                      ││
│  │ • API Call Orchestration                                ││
│  │ • Error Handling                                        ││
│  │ • Flow Management                                       ││
│  └─────────────────────────────────────────────────────────┘│
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              NETWORK LAYER (HTTP Communication)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐ │
│  │ SanbotApiSvc │  │  Retrofit2   │  │  OkHttp Client   │ │
│  │              │  │              │  │                  │ │
│  │ • Endpoints  │  │ • Serialization  • Connection Pool │ │
│  │ • Methods    │  │ • Type Safe  │  │ • Interceptors   │ │
│  │ • Contracts  │  │              │  │                  │ │
│  └──────────────┘  └──────────────┘  └──────────────────┘ │
│         │                  │                   │            │
│         └──────────────────┼───────────────────┘            │
│                            │                                 │
│                  ┌─────────▼─────────┐                      │
│                  │  Interceptors     │                      │
│                  ├───────────────────┤                      │
│                  │ 1. RetryInterceptor (Resilience)        │
│                  │ 2. RateLimitInterceptor (Tracking)      │
│                  │ 3. AuthInterceptor (Bearer Token)       │
│                  │ 4. HttpLoggingInterceptor (Debug)       │
│                  └───────────────────┘                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│          API ERROR HANDLING & UTILITIES LAYER                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ ErrorHandler              ValidationUtils            │  │
│  │ • Error Parsing           • Phone Validation         │  │
│  │ • Human Messages          • Email Validation         │  │
│  │ • Recovery Suggestions    • Name Validation          │  │
│  │ • Error Categorization    • Date Validation          │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ SessionManager            NetworkResult              │  │
│  │ • Session ID Generation   • Success<T>               │  │
│  │ • Session Persistence     • Error<T>                 │  │
│  │ • Session Validation      • Loading<T>               │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│         REMOTE API (TRIPANDEVENT SERVERS)                    │
│  https://bot.tripandevent.com/api/                           │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ • Voice APIs (transcribe, generate, conversation)     │ │
│  │ • Package APIs (list, details, filtering)              │ │
│  │ • CRM APIs (create, update, notes)                     │ │
│  │ • Action APIs (SMS, WhatsApp, Email, Booking)         │ │
│  │ • Media APIs (videos, images)                          │ │
│  │ • Config APIs (settings, feature flags)                │ │
│  │ • Health Check (API status)                            │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 📊 Data Flow

### Successful API Call Flow

```
UI Layer (Compose)
        │
        │ User Action
        ▼
ViewModel
        │
        │ Call Repository
        ▼
Repository (SanbotRepository)
        │
        │ Validation Check
        ▼
        ├─ ✅ Valid → Continue
        │
        ├─ ❌ Invalid → Error (No API Call)
        │           └─→ UI: Show validation error
        │
        ▼
        emit(NetworkResult.Loading)
        │
        ├─→ UI: Show loading indicator
        │
        ▼
API Service (SanbotApiService)
        │
        │ Build Request
        ▼
OkHttp Client
        │
        ├─ AuthInterceptor: Add Bearer token
        ├─ RetryInterceptor: Check for retry eligibility
        ├─ RateLimitInterceptor: Track rate limits
        ├─ HttpLoggingInterceptor: Log request (debug)
        │
        ▼
HTTPS Request
        │
        ▼
Server Response
        │
        ├─ Interceptors: Process response
        │
        ▼
Response Parsing
        │
        ├─ ✅ Success (2xx) → Parse body
        │
        ├─ ❌ Error (4xx/5xx) → Parse error response
        │                    └─ Retry eligibility check
        │                    └─ If retryable: Exponential backoff
        │                    └─ If not retryable: Return error
        │
        ▼
emit(NetworkResult.Success<T> or Error<T>)
        │
        ├─→ UI: Show data or error message
        │
        ▼
UI Update
```

### Error Handling Flow

```
API Error Response
        │
        ▼
Parse JSON Error
        │
        ├─ ErrorResponse:
        │  ├─ success: false
        │  └─ error:
        │     ├─ code: "INVALID_PHONE"
        │     ├─ message: "Phone format invalid"
        │     └─ field: "phone"
        │
        ▼
ErrorHandler.getErrorMessage(error)
        │
        ├─→ "Invalid phone format. Use +971..."
        │
        ▼
ErrorHandler.getRecoverySuggestion(code)
        │
        ├─→ "Enter phone in international format"
        │
        ▼
NetworkResult.Error(message, apiError)
        │
        ▼
UI Layer
        │
        ├─ Show error message
        ├─ Show recovery suggestion
        ├─ Show retry button
        │
        ▼
User Action (Retry/Fix)
```

## 🔄 Retry Logic Flow

```
API Call Fails
        │
        ▼
RetryInterceptor.intercept()
        │
        ├─ Check HTTP Status Code
        │
        ├─ Is retryable? (408, 429, 5xx)
        │
        ├─ ✅ Yes
        │  │
        │  ├─ Calculate backoff delay
        │  │  Formula: min(1000 * 2^attempt + jitter, maxDelay)
        │  │
        │  ├─ Attempt 1: ~1000ms
        │  ├─ Attempt 2: ~2000ms + jitter
        │  ├─ Attempt 3: ~4000ms + jitter
        │  │
        │  ├─ Sleep(delay)
        │  │
        │  └─ Retry API Call
        │     │
        │     └─→ If succeeds: Return response
        │     └─→ If fails: Check attempts
        │
        ├─ ❌ No
        │  │
        │  └─ Return error immediately
        │
        ▼
Final Result
```

## 🏢 Component Interaction

```
┌────────────────────────────────────────────────────────┐
│                    Hilt Dependency Injection            │
│                                                        │
│  @Module NetworkModule {                              │
│    ├─ provideOkHttpClient()                           │
│    │   ├─ RetryInterceptor                            │
│    │   ├─ RateLimitInterceptor                        │
│    │   ├─ AuthInterceptor                             │
│    │   └─ HttpLoggingInterceptor                      │
│    │                                                   │
│    ├─ provideRetrofit(OkHttpClient)                   │
│    │   └─ Base URL: https://bot.tripandevent.com/api/ │
│    │                                                   │
│    └─ provideSanbotApiService(Retrofit)               │
│        └─ SanbotApiService Interface                  │
│                                                        │
│  SettingsRepository                                    │
│    ├─ observeSettings()                               │
│    ├─ updateApiKey()                                  │
│    └─ updateApiBaseUrl()                              │
└────────────────────────────────────────────────────────┘
        │
        ▼ Inject into
┌────────────────────────────────────────────────────────┐
│              SanbotRepository                           │
│                                                        │
│  Inject: SanbotApiService                             │
│  Methods: voice, packages, crm, actions, media, config│
│                                                        │
│  Features:                                            │
│  • Input validation                                   │
│  • Error parsing                                      │
│  • Flow wrapping                                      │
│  • Dispatcher handling                                │
└────────────────────────────────────────────────────────┘
        │
        ▼ Inject into
┌────────────────────────────────────────────────────────┐
│              ViewModels                                │
│                                                        │
│  Collect repository flows                             │
│  Emit UI states                                       │
│  Handle user actions                                  │
└────────────────────────────────────────────────────────┘
        │
        ▼ Observe in
┌────────────────────────────────────────────────────────┐
│              Compose UI                                │
│                                                        │
│  Display loading / success / error states             │
│  User interactions                                    │
│  Visual feedback                                      │
└────────────────────────────────────────────────────────┘
```

## 🔐 Security Layers

```
┌─ INPUT LAYER ──────────────────────────────────┐
│                                                │
│  User Input                                    │
│  ├─ ValidationUtils.isValidPhone()             │
│  ├─ ValidationUtils.isValidEmail()             │
│  ├─ ValidationUtils.isValidName()              │
│  ├─ ValidationUtils.isValidDate()              │
│  └─ Returns: Boolean (blocks invalid data)     │
│                                                │
└────────────────────────────────────────────────┘

┌─ AUTHENTICATION LAYER ──────────────────────────┐
│                                                │
│  NetworkModule.provideAuthInterceptor()        │
│  ├─ Header: Authorization: Bearer <API_KEY>   │
│  ├─ Header: Content-Type: application/json     │
│  ├─ Header: Accept: application/json           │
│  └─ API Key from: Secure DataStore             │
│                                                │
└────────────────────────────────────────────────┘

┌─ TRANSPORT LAYER ──────────────────────────────┐
│                                                │
│  OkHttpClient Configuration                   │
│  ├─ Protocol: HTTPS (enforced)                │
│  ├─ Timeouts: 30 seconds all operations       │
│  ├─ Certificate: Standard (TLS 1.2+)          │
│  └─ Connection Pool: Reuse connections        │
│                                                │
└────────────────────────────────────────────────┘

┌─ ERROR HANDLING LAYER ──────────────────────────┐
│                                                │
│  ErrorHandler Component                       │
│  ├─ Parse API error response                  │
│  ├─ Map to user-friendly message              │
│  ├─ No sensitive data exposed                 │
│  └─ Recovery suggestions provided             │
│                                                │
└────────────────────────────────────────────────┘

┌─ STORAGE LAYER ────────────────────────────────┐
│                                                │
│  SettingsRepository                           │
│  ├─ Storage: Android DataStore                │
│  ├─ Encryption: Automatic (Android)           │
│  ├─ Data: API Key, Base URL                   │
│  └─ Access: Runtime updates supported         │
│                                                │
└────────────────────────────────────────────────┘
```

## 📈 Request/Response Lifecycle

```
┌────────────────┐
│  UI Triggers   │
│  API Call      │
└────────┬───────┘
         │
         ▼
┌────────────────────┐
│  Repository Check  │
│  Input Validation  │
└────────┬───────────┘
         │
         ├─ ❌ Invalid
         │   └─→ emit Error (No API call)
         │
         ├─ ✅ Valid
         │   └─→ Continue to API
         │
         ▼
┌────────────────────┐
│  emit Loading()    │
└────────┬───────────┘
         │
         ▼
┌────────────────────────┐
│  API Service Call      │
│  Build HTTP Request    │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  OkHttp Interceptors   │
│  1. Retry Logic        │
│  2. Rate Limit Track   │
│  3. Add Auth Header    │
│  4. Logging (debug)    │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  HTTP Request Sent     │
│  HTTPS Connection      │
│  Timeout: 30 seconds   │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  Response Received     │
│  Parse Headers & Body  │
└────────┬───────────────┘
         │
         ├─ HTTP 2xx (Success)
         │  └─→ Parse JSON body
         │      └─→ emit Success<T>
         │
         ├─ HTTP 4xx (Client Error)
         │  └─→ Parse error response
         │      └─→ No retry
         │      └─→ emit Error
         │
         ├─ HTTP 429 (Rate Limit)
         │  └─→ Parse error response
         │      └─→ Retry with backoff
         │
         ├─ HTTP 5xx (Server Error)
         │  └─→ Parse error response
         │      └─→ Retry with backoff
         │
         └─ Exception (Network)
            └─→ Retry with backoff
                └─ 3 max attempts
                └─ Exponential delay
         │
         ▼
┌────────────────────────┐
│  emit Result           │
│  Success<T> or Error   │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  UI Observes Result    │
│  Update Display        │
│  Show Data or Error    │
└────────────────────────┘
```

## 🎯 Class Responsibilities

| Class | Responsibility |
|-------|-----------------|
| **SanbotApiService** | Define HTTP endpoints |
| **SanbotRepository** | Orchestrate API calls, validate input |
| **NetworkModule** | Configure network layer, DI |
| **ErrorHandler** | Parse errors, provide messages |
| **ValidationUtils** | Validate user input |
| **SessionManager** | Manage session lifecycle |
| **RetryInterceptor** | Handle retries with backoff |
| **RateLimitInterceptor** | Track rate limits |
| **NetworkResult** | Wrap API results (Loading/Success/Error) |
| **ViewModel** | Manage UI state, delegate to repository |
| **UI (Compose)** | Display data, handle user input |

---

**Architecture is: Clean, Modular, Secure, and Production-Ready ✅**
