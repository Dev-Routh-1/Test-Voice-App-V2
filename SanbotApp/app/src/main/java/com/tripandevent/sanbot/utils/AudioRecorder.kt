package com.tripandevent.sanbot.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor() {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    fun startRecording(context: Context): Boolean {
        if (isRecording) return false
        
        return try {
            outputFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.wav")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioSamplingRate(16000)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            false
        }
    }

    fun stopRecording(): String? {
        if (!isRecording) return null
        
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            outputFile?.let { file ->
                if (file.exists()) {
                    encodeFileToBase64(file)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            null
        }
    }

    fun cancelRecording() {
        cleanup()
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    fun getMaxAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun cleanup() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder = null
        isRecording = false
        outputFile?.delete()
        outputFile = null
    }

    private fun encodeFileToBase64(file: File): String {
        val bytes = FileInputStream(file).use { it.readBytes() }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
