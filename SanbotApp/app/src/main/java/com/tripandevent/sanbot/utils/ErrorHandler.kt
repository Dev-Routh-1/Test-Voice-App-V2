package com.tripandevent.sanbot.utils

import com.tripandevent.sanbot.data.models.ApiError

/**
 * Handles API errors with specific error codes from the API contract.
 * Provides human-readable error messages and recovery suggestions.
 */
object ErrorHandler {
    
    fun getErrorMessage(error: ApiError?): String {
        return error?.let { apiError ->
            when (apiError.code) {
                "INVALID_API_KEY" -> "API authentication failed. Please check your API key."
                "INVALID_REQUEST" -> "Invalid request format: ${apiError.message}"
                "MISSING_FIELD" -> "Missing required field: ${apiError.field}"
                "INVALID_PHONE" -> "Phone number format is invalid. Use international format (e.g., +971501234567)"
                "INVALID_EMAIL" -> "Email address format is invalid."
                "PACKAGE_NOT_FOUND" -> "Package not found. Please select a valid package."
                "LEAD_NOT_FOUND" -> "Lead not found. Please create a new lead first."
                "RATE_LIMIT_EXCEEDED" -> "Too many requests. Please try again in a few minutes."
                "SERVER_ERROR" -> "Server error: ${apiError.message}"
                "SERVICE_UNAVAILABLE" -> "Service is temporarily unavailable. Please try again later."
                "AUDIO_TOO_LARGE" -> "Audio file is too large (max 5MB). Please record a shorter audio."
                else -> "Error: ${apiError.message}"
            }
        } ?: "An unknown error occurred"
    }
    
    fun getErrorCode(error: ApiError?): String {
        return error?.code ?: "UNKNOWN_ERROR"
    }
    
    fun getRecoverySuggestion(errorCode: String): String {
        return when (errorCode) {
            "INVALID_API_KEY" -> "Go to Settings and verify your API key."
            "RATE_LIMIT_EXCEEDED" -> "Wait a few moments before trying again."
            "SERVER_ERROR", "SERVICE_UNAVAILABLE" -> "Please try again later."
            "NETWORK_ERROR" -> "Check your internet connection and try again."
            "INVALID_PHONE", "INVALID_EMAIL" -> "Please enter a valid contact information."
            else -> "Please try again."
        }
    }
    
    fun isRetryable(httpCode: Int): Boolean {
        return httpCode in listOf(408, 429, 500, 502, 503, 504)
    }
    
    fun isRetryableError(errorCode: String): Boolean {
        return errorCode in listOf(
            "RATE_LIMIT_EXCEEDED",
            "SERVER_ERROR",
            "SERVICE_UNAVAILABLE",
            "NETWORK_ERROR"
        )
    }
}

/**
 * Categorizes API errors for better handling and user feedback.
 */
enum class ErrorCategory {
    AUTHENTICATION,
    VALIDATION,
    NOT_FOUND,
    RATE_LIMIT,
    SERVER_ERROR,
    NETWORK_ERROR,
    UNKNOWN
}

fun String.toErrorCategory(): ErrorCategory {
    return when (this) {
        "INVALID_API_KEY" -> ErrorCategory.AUTHENTICATION
        "INVALID_REQUEST", "MISSING_FIELD", "INVALID_PHONE", "INVALID_EMAIL" -> ErrorCategory.VALIDATION
        "PACKAGE_NOT_FOUND", "LEAD_NOT_FOUND" -> ErrorCategory.NOT_FOUND
        "RATE_LIMIT_EXCEEDED" -> ErrorCategory.RATE_LIMIT
        "SERVER_ERROR", "SERVICE_UNAVAILABLE" -> ErrorCategory.SERVER_ERROR
        "NETWORK_ERROR" -> ErrorCategory.NETWORK_ERROR
        else -> ErrorCategory.UNKNOWN
    }
}
