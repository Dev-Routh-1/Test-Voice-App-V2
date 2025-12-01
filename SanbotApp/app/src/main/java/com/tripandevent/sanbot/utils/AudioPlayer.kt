package com.tripandevent.sanbot.utils

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor() {
    
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    fun playFromUrl(url: String, onCompletion: () -> Unit = {}) {
        stop()
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { mp ->
                    _duration.value = mp.duration
                    mp.start()
                    _isPlaying.value = true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    _isPlaying.value = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    _isPlaying.value = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            _isPlaying.value = false
            _currentPosition.value = 0
            _duration.value = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
            _currentPosition.value = position
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePosition() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    _currentPosition.value = it.currentPosition
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        stop()
    }
}
