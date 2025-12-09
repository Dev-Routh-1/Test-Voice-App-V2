package com.tripandevent.sanbot.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.tripandevent.sanbot.BuildConfig
import com.tripandevent.sanbot.data.api.SanbotApiService
import com.tripandevent.sanbot.data.repository.SettingsRepository
import com.tripandevent.sanbot.utils.RateLimitInterceptor
import com.tripandevent.sanbot.utils.RetryInterceptor
import dagger.Module
import dagger.Provides
import javax.inject.Named
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sanbot_settings")

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val DEFAULT_BASE_URL = "https://bot.tripandevent.com/api/"
    private const val DEFAULT_API_KEY = "test_sanbot_key_abc123xyz789"
    
    // Timeout configuration
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    private val currentApiKey = AtomicReference(DEFAULT_API_KEY)
    private val currentBaseUrl = AtomicReference(DEFAULT_BASE_URL)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository {
        return SettingsRepository(dataStore).also { repo ->
            repo.observeSettings(
                onApiKeyChanged = { key -> currentApiKey.set(key.ifEmpty { DEFAULT_API_KEY }) },
                onBaseUrlChanged = { url -> currentBaseUrl.set(url.ifEmpty { DEFAULT_BASE_URL }) }
            )
        }
    }

    @Provides
    @Singleton
    @Named("auth_interceptor")
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val apiKey = currentApiKey.get() ?: DEFAULT_API_KEY
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("base_url_interceptor")
    fun provideBaseUrlInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val currentBase = currentBaseUrl.get() ?: DEFAULT_BASE_URL

            // Build new URL by replacing base with currentBase while keeping the request path
            val base = try {
                // Ensure the base is a valid HttpUrl using Kotlin extension
                currentBase.toHttpUrlOrNull()
            } catch (e: Exception) {
                null
            }

            val newUrl = if (base != null) {
                // Concatenate base and relative path
                val baseStr = currentBase.trimEnd('/')
                val relPath = request.url.encodedPath.trimStart('/')
                val query = request.url.encodedQuery
                val combined = if (query.isNullOrEmpty()) {
                    "$baseStr/$relPath"
                } else {
                    "$baseStr/$relPath?$query"
                }
                try {
                    combined.toHttpUrlOrNull() ?: request.url
                } catch (e: Exception) {
                    request.url
                }
            } else {
                request.url
            }

            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }
    }

    @Provides
    @Singleton
    fun provideRetryInterceptor(): RetryInterceptor {
        return RetryInterceptor(maxRetries = 3, initialDelayMs = 1000)
    }

    @Provides
    @Singleton
    fun provideRateLimitInterceptor(): RateLimitInterceptor {
        return RateLimitInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("base_url_interceptor") baseUrlInterceptor: Interceptor,
        @Named("auth_interceptor") authInterceptor: Interceptor,
        retryInterceptor: RetryInterceptor,
        rateLimitInterceptor: RateLimitInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            // Base URL interceptor must run first to adjust URL to current base
            .addInterceptor(baseUrlInterceptor)
            // Retry logic should be next
            .addInterceptor(retryInterceptor)
            // Rate limit tracking
            .addInterceptor(rateLimitInterceptor)
            // Authentication headers
            .addInterceptor(authInterceptor)
            // Logging for debugging
            .addInterceptor(loggingInterceptor)
            // Connection timeouts
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = currentBaseUrl.get() ?: DEFAULT_BASE_URL
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSanbotApiService(retrofit: Retrofit): SanbotApiService {
        return retrofit.create(SanbotApiService::class.java)
    }
}
