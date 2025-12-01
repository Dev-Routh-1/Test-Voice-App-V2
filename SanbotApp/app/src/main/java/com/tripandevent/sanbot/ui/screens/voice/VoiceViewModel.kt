package com.tripandevent.sanbot.ui.screens.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripandevent.sanbot.data.models.VoiceConversationRequest
import com.tripandevent.sanbot.data.repository.SanbotRepository
import com.tripandevent.sanbot.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VoiceState {
    IDLE, LISTENING, PROCESSING, SPEAKING, ERROR
}

data class ConversationMessage(
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class VoiceUiState(
    val voiceState: VoiceState = VoiceState.IDLE,
    val conversationHistory: List<ConversationMessage> = emptyList(),
    val errorMessage: String? = null,
    val amplitudeLevel: Float = 0f
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SanbotRepository,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var amplitudeJob: Job? = null
    private var recordingTimeoutJob: Job? = null
    
    private val maxRecordingDurationMs = 30_000L

    fun onMicButtonClick() {
        when (_uiState.value.voiceState) {
            VoiceState.IDLE, VoiceState.ERROR -> startRecording()
            VoiceState.LISTENING -> stopRecordingAndSend()
            VoiceState.SPEAKING -> {
                audioPlayer.stop()
                _uiState.update { it.copy(voiceState = VoiceState.IDLE) }
            }
            VoiceState.PROCESSING -> { }
        }
    }

    private fun startRecording() {
        val success = audioRecorder.startRecording(context)
        if (success) {
            _uiState.update { 
                it.copy(
                    voiceState = VoiceState.LISTENING,
                    errorMessage = null
                ) 
            }
            startAmplitudeMonitoring()
            startRecordingTimeout()
        } else {
            _uiState.update { 
                it.copy(
                    voiceState = VoiceState.ERROR,
                    errorMessage = "Failed to start recording. Please check microphone permissions."
                ) 
            }
        }
    }

    private fun startAmplitudeMonitoring() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (audioRecorder.isCurrentlyRecording()) {
                val amplitude = audioRecorder.getMaxAmplitude()
                val normalizedAmplitude = (amplitude / 32767f).coerceIn(0f, 1f)
                _uiState.update { it.copy(amplitudeLevel = normalizedAmplitude) }
                delay(100)
            }
        }
    }

    private fun startRecordingTimeout() {
        recordingTimeoutJob?.cancel()
        recordingTimeoutJob = viewModelScope.launch {
            delay(maxRecordingDurationMs)
            if (_uiState.value.voiceState == VoiceState.LISTENING) {
                stopRecordingAndSend()
            }
        }
    }

    private fun stopRecordingAndSend() {
        amplitudeJob?.cancel()
        recordingTimeoutJob?.cancel()
        
        val audioBase64 = audioRecorder.stopRecording()
        if (audioBase64 != null) {
            sendVoiceRequest(audioBase64)
        } else {
            _uiState.update { 
                it.copy(
                    voiceState = VoiceState.ERROR,
                    errorMessage = "Failed to capture audio. Please try again."
                ) 
            }
        }
    }

    private fun sendVoiceRequest(audioBase64: String) {
        _uiState.update { it.copy(voiceState = VoiceState.PROCESSING) }

        viewModelScope.launch {
            val request = VoiceConversationRequest(
                audio = audioBase64,
                format = "wav",
                sessionId = sessionManager.getSessionId(),
                language = "en",
                voice = "alloy"
            )

            repository.voiceConversation(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.update { it.copy(voiceState = VoiceState.PROCESSING) }
                    }
                    is NetworkResult.Success -> {
                        val response = result.data
                        if (response?.success == true) {
                            val userMessage = ConversationMessage(
                                isUser = true,
                                text = response.transcript ?: "..."
                            )
                            val agentMessage = ConversationMessage(
                                isUser = false,
                                text = response.response?.text ?: ""
                            )
                            
                            _uiState.update { state ->
                                state.copy(
                                    voiceState = VoiceState.SPEAKING,
                                    conversationHistory = state.conversationHistory + userMessage + agentMessage,
                                    errorMessage = null
                                )
                            }
                            
                            response.response?.audioUrl?.let { url ->
                                playAudioResponse(url)
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    voiceState = VoiceState.ERROR,
                                    errorMessage = response?.error?.message ?: "Unknown error occurred"
                                ) 
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                voiceState = VoiceState.ERROR,
                                errorMessage = result.message ?: "Failed to process voice request"
                            ) 
                        }
                    }
                }
            }
        }
    }

    private fun playAudioResponse(audioUrl: String) {
        audioPlayer.playFromUrl(audioUrl) {
            _uiState.update { it.copy(voiceState = VoiceState.IDLE) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, voiceState = VoiceState.IDLE) }
    }

    fun resetConversation() {
        audioPlayer.stop()
        audioRecorder.cancelRecording()
        sessionManager.startNewSession()
        _uiState.value = VoiceUiState()
    }

    override fun onCleared() {
        super.onCleared()
        amplitudeJob?.cancel()
        recordingTimeoutJob?.cancel()
        audioPlayer.release()
        audioRecorder.cancelRecording()
    }
}
