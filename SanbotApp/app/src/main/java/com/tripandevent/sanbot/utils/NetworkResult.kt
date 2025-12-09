package com.tripandevent.sanbot.utils

import com.tripandevent.sanbot.data.models.ApiError

/**
 * Sealed class representing the result of a network API call.
 * Can be in one of three states: Success, Error, or Loading.
 */
sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null,
    val apiError: ApiError? = null
) {
    /**
     * Represents a successful API response.
     */
    class Success<T>(data: T) : NetworkResult<T>(data)
    
    /**
     * Represents a failed API response with error details.
     */
    class Error<T>(
        message: String,
        apiError: ApiError? = null,
        data: T? = null
    ) : NetworkResult<T>(data, message, apiError)
    
    /**
     * Represents an API call in progress.
     */
    class Loading<T> : NetworkResult<T>()
}
