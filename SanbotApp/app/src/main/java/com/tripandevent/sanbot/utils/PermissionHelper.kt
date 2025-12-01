package com.tripandevent.sanbot.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    
    private val audioPermissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    
    fun hasAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun shouldShowRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.RECORD_AUDIO
        )
    }
    
    fun requestAudioPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            audioPermissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )
    }
    
    fun checkPermissionResult(
        requestCode: Int,
        grantResults: IntArray
    ): Boolean {
        return requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
               grantResults.isNotEmpty() &&
               grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun rememberAudioPermissionState(
    onPermissionResult: (Boolean) -> Unit = {}
): AudioPermissionState {
    var hasPermission by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        onPermissionResult(isGranted)
    }
    
    return remember(hasPermission) {
        AudioPermissionState(
            hasPermission = hasPermission,
            launchRequest = { launcher.launch(Manifest.permission.RECORD_AUDIO) },
            updatePermissionState = { hasPermission = it }
        )
    }
}

class AudioPermissionState(
    val hasPermission: Boolean,
    val launchRequest: () -> Unit,
    private val updatePermissionState: (Boolean) -> Unit
) {
    fun checkAndUpdate(context: Context) {
        updatePermissionState(PermissionHelper.hasAudioPermission(context))
    }
}
