# Sanbot Android App - Project Overview

## Project Summary
This is a **native Kotlin Android application** for the Sanbot robot platform, providing voice-based customer interaction, tour package display, CRM integration, and lead capture for TripAndEvent offices.

## Important Note
**This project cannot be run in Replit** because it's a native Android application that requires:
- Android Studio IDE
- Android SDK
- Gradle with Android plugin
- Android emulator or physical device

The user has confirmed they will import this project into their local Android Studio to build and run.

## Project Structure
```
SanbotApp/                    # Main Android project folder
├── app/                      # Android app module
│   ├── src/main/
│   │   ├── java/com/tripandevent/sanbot/
│   │   │   ├── data/         # API models, services, repositories
│   │   │   ├── di/           # Hilt dependency injection
│   │   │   ├── ui/           # Jetpack Compose screens and components
│   │   │   └── utils/        # Utility classes (audio, session, etc.)
│   │   └── res/              # Android resources (layouts, strings, etc.)
│   └── build.gradle.kts      # App-level build configuration
├── gradle/libs.versions.toml # Dependency version catalog
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts       # Project settings
└── README.md                 # Setup and usage documentation
```

## Technology Stack
- **Kotlin 1.9.x** with Jetpack Compose
- **Material 3** for UI design
- **Hilt** for dependency injection
- **Retrofit + OkHttp** for API calls
- **Coil** for image loading
- **ExoPlayer** for video playback
- **DataStore** for settings persistence

## Key Features Implemented
1. Welcome screen with animations
2. Main menu with navigation
3. Voice interaction with audio recording
4. Package list with filtering
5. Package details with image gallery
6. Contact form/Lead capture
7. Thank you screen with sharing
8. Media gallery with video player
9. Settings with password protection
10. Complete API integration

## API Integration
- Base URL: `https://bot.tripandevent.com/api/`
- Test API Key: `test_sanbot_key_abc123xyz789`
- All endpoints from the API contract are implemented

## To Build This Project
1. Copy the `SanbotApp` folder to your local machine
2. Open it in Android Studio
3. Add Poppins font files (see FONTS_README.md)
4. Sync Gradle and build
5. Run on Android device or emulator (API 26+)
