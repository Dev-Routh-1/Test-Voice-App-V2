package com.tripandevent.sanbot.utils

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.math.min
import kotlin.math.pow

/**
 * Implements exponential backoff retry logic with jitter for API calls.
 * Handles rate limiting (429) and network errors.
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        for (attempt in 0 until maxRetries) {
            try {
                response = chain.proceed(request)
                
                // Retry on rate limit (429) or server errors (5xx)
                if (response.code == 429 || response.code >= 500) {
                    if (attempt < maxRetries - 1) {
                        response.close()
                        val delayMs = calculateBackoffDelay(attempt, response.code)
                        Thread.sleep(delayMs)
                        continue
                    }
                }
                
                return response
            } catch (e: IOException) {
                exception = e
                if (attempt < maxRetries - 1) {
                    val delayMs = calculateBackoffDelay(attempt, null)
                    Thread.sleep(delayMs)
                    continue
                }
            }
        }
        
        return response ?: throw exception ?: IOException("Failed after $maxRetries retries")
    }
    
    /**
     * Calculates exponential backoff with jitter.
     * Formula: min(initialDelay * 2^attempt + random jitter, maxDelay)
     */
    private fun calculateBackoffDelay(attempt: Int, statusCode: Int?): Long {
        val exponentialDelay = initialDelayMs * 2.0.pow(attempt.toDouble()).toLong()
        val jitterRange = exponentialDelay / 2
        val jitter = (0..jitterRange).random()
        
        // Use Retry-After header if available (for 429 responses)
        val maxDelay = if (statusCode == 429) 60000L else 10000L
        
        return min(exponentialDelay + jitter, maxDelay)
    }
}

/**
 * Tracks and enforces rate limiting based on API response headers.
 * Parses X-RateLimit-* headers and prevents exceeding limits.
 */
class RateLimitInterceptor : Interceptor {
    
    private var rateLimitLimit: Int = 100
    private var rateLimitRemaining: Int = 100
    private var rateLimitResetTime: Long = System.currentTimeMillis()
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Parse rate limit headers
        response.header("X-RateLimit-Limit")?.toIntOrNull()?.let {
            rateLimitLimit = it
        }
        response.header("X-RateLimit-Remaining")?.toIntOrNull()?.let {
            rateLimitRemaining = it
        }
        response.header("X-RateLimit-Reset")?.toLongOrNull()?.let {
            rateLimitResetTime = it * 1000 // Convert to milliseconds
        }
        
        return response
    }
    
    fun getRateLimitInfo(): RateLimitInfo {
        return RateLimitInfo(
            limit = rateLimitLimit,
            remaining = rateLimitRemaining,
            resetTime = rateLimitResetTime
        )
    }
    
    fun isRateLimited(): Boolean {
        return rateLimitRemaining <= 0 && System.currentTimeMillis() < rateLimitResetTime
    }
    
    fun getSecondsUntilReset(): Long {
        val now = System.currentTimeMillis()
        return if (rateLimitResetTime > now) {
            (rateLimitResetTime - now) / 1000
        } else {
            0
        }
    }
}

data class RateLimitInfo(
    val limit: Int,
    val remaining: Int,
    val resetTime: Long
)
