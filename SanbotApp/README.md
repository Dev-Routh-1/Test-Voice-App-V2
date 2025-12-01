# Sanbot Android App - TripAndEvent Interactive Assistant

A native Kotlin Android application built with Jetpack Compose for the Sanbot robot platform. This app provides voice-based customer interaction, tour package display, lead capture, and CRM integration for TripAndEvent offices and retail locations.

## Features

- **Voice Interaction**: Real-time voice conversations with customers using speech-to-text and text-to-speech APIs
- **Package Display**: Browse and view tour packages with images, pricing, ratings, and details
- **Lead Capture**: Collect customer information and create leads in the CRM system
- **Customer Actions**: Send package details via SMS, WhatsApp, or Email
- **Media Gallery**: Display promotional videos and images with full-screen playback
- **Settings**: Admin-protected configuration for API endpoints and branch locations
- **Auto-Reset**: Automatic return to welcome screen after 2 minutes of inactivity

## Technology Stack

- **Language**: Kotlin 1.9.x
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Video Player**: ExoPlayer (Media3)
- **Local Storage**: DataStore Preferences
- **Navigation**: Navigation Compose

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34 (API level 34)
- Minimum SDK: 26 (Android 8.0)

## Project Structure

```
SanbotApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/tripandevent/sanbot/
│   │   │   ├── data/
│   │   │   │   ├── api/           # Retrofit API service
│   │   │   │   ├── models/        # API request/response models
│   │   │   │   └── repository/    # Data repositories
│   │   │   ├── di/                # Hilt dependency injection modules
│   │   │   ├── ui/
│   │   │   │   ├── components/    # Reusable UI components
│   │   │   │   ├── navigation/    # Navigation graph
│   │   │   │   ├── screens/       # Screen composables and ViewModels
│   │   │   │   └── theme/         # Material theme, colors, typography
│   │   │   ├── utils/             # Utility classes
│   │   │   ├── MainActivity.kt
│   │   │   └── SanbotApplication.kt
│   │   └── res/                   # Android resources
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml         # Version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

## Setup Instructions

### 1. Clone or Download the Project

```bash
# Copy the SanbotApp folder to your local machine
```

### 2. Open in Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `SanbotApp` folder and select it
4. Wait for Gradle sync to complete

### 3. Add Font Files

Download Poppins font family from Google Fonts and place in `app/src/main/res/font/`:
- `poppins_regular.ttf`
- `poppins_medium.ttf`
- `poppins_semibold.ttf`
- `poppins_bold.ttf`

See `FONTS_README.md` for detailed instructions.

### 4. Configure API Settings

The app is pre-configured with:
- **Base URL**: `https://bot.tripandevent.com/api/`
- **Test API Key**: `test_sanbot_key_abc123xyz789`

To change these settings:
1. Open Settings screen in the app (password: `admin123`)
2. Update the API Base URL
3. Test the connection

For production, update the API key in `SettingsRepository.kt` or through the Settings screen.

### 5. Build and Run

1. Connect an Android device or start an emulator
2. Click "Run" (green play button) in Android Studio
3. Select your target device
4. Wait for the app to build and install

## API Endpoints

The app integrates with the following endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/voice.conversation` | POST | Complete voice flow |
| `/packages` | GET | Get package list |
| `/packages/{id}` | GET | Get package details |
| `/crm/leads` | POST | Create new lead |
| `/actions/send-sms` | POST | Send SMS |
| `/actions/send-whatsapp` | POST | Send WhatsApp |
| `/actions/send-email` | POST | Send email quote |
| `/media` | GET | Get media library |
| `/config` | GET | Get app configuration |
| `/health` | GET | Health check |

## Screens

1. **Welcome Screen**: Animated welcome with company logo and "Start" button
2. **Main Menu**: Four action cards (Talk to Me, Browse Packages, Watch Videos, Contact Us)
3. **Voice Interaction**: Microphone button, waveform animation, conversation history
4. **Package List**: Scrollable package cards with filtering
5. **Package Details**: Image gallery, expandable sections, action buttons
6. **Contact Form**: Lead capture with validation
7. **Thank You**: Success confirmation with sharing options
8. **Media Gallery**: Videos and images with full-screen playback
9. **Settings**: Password-protected admin configuration

## Building for Production

### Generate Signed APK

1. In Android Studio, go to Build > Generate Signed Bundle / APK
2. Select "APK"
3. Create or select a keystore
4. Choose "release" build variant
5. Click "Finish"

The signed APK will be in `app/release/`

### ProGuard Configuration

The app includes ProGuard rules in `proguard-rules.pro` for:
- Retrofit
- Gson
- Hilt
- Compose
- API models

## Security Notes

- API keys are stored in Android EncryptedSharedPreferences via DataStore
- Do NOT hardcode production API keys in the source code
- HTTPS is enforced for all API communications
- Input validation is applied to all forms

## Default Credentials

- **Admin Password**: `admin123` (change this in production)
- **Test API Key**: `test_sanbot_key_abc123xyz789`

## Customization

### Brand Colors

Edit `ui/theme/Color.kt`:
```kotlin
val BrandBlue = Color(0xFF0066CC)
val BrandOrange = Color(0xFFFF6B35)
```

### Typography

Edit `ui/theme/Type.kt` to modify font sizes and weights.

### API Configuration

Edit `data/repository/SettingsRepository.kt` for default settings.

## Troubleshooting

### Gradle Sync Failed
- Ensure you have JDK 17 installed
- Check your internet connection for dependency downloads
- Try File > Invalidate Caches and Restart

### Build Errors
- Clean project: Build > Clean Project
- Rebuild: Build > Rebuild Project

### Font Not Found
- Ensure font files are placed in `res/font/`
- File names must be lowercase with underscores

## License

This project is proprietary to TripAndEvent. All rights reserved.

## Support

For technical support, contact the TripAndEvent development team.
