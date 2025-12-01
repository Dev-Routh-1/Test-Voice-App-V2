package com.tripandevent.sanbot.di

import android.content.Context
import com.tripandevent.sanbot.utils.AudioPlayer
import com.tripandevent.sanbot.utils.AudioRecorder
import com.tripandevent.sanbot.utils.InactivityTimer
import com.tripandevent.sanbot.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAudioRecorder(): AudioRecorder {
        return AudioRecorder()
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(): AudioPlayer {
        return AudioPlayer()
    }

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return SessionManager()
    }

    @Provides
    @Singleton
    fun provideInactivityTimer(): InactivityTimer {
        return InactivityTimer()
    }
}
