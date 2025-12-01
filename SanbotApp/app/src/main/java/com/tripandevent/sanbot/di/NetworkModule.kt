package com.tripandevent.sanbot.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.tripandevent.sanbot.BuildConfig
import com.tripandevent.sanbot.data.api.SanbotApiService
import com.tripandevent.sanbot.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
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
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val apiKey = currentApiKey.get() ?: DEFAULT_API_KEY
            val request = chain.request().newBuilder()
                .addHeader("X-API-KEY", apiKey)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
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
