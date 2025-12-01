package com.tripandevent.sanbot.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InactivityTimer @Inject constructor() {
    
    companion object {
        const val DEFAULT_TIMEOUT_MS = 120_000L
    }
    
    private var timerJob: Job? = null
    private var timeoutMs: Long = DEFAULT_TIMEOUT_MS
    
    private val _timeoutEvent = MutableSharedFlow<Unit>()
    val timeoutEvent: SharedFlow<Unit> = _timeoutEvent.asSharedFlow()
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(timeoutMillis: Long = DEFAULT_TIMEOUT_MS) {
        timeoutMs = timeoutMillis
        reset()
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = scope.launch {
            delay(timeoutMs)
            _timeoutEvent.emit(Unit)
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
    }

    fun setTimeoutDuration(timeoutMillis: Long) {
        timeoutMs = timeoutMillis
    }

    fun destroy() {
        stop()
        scope.cancel()
    }
}
