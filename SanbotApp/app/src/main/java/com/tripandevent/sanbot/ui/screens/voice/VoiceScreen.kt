package com.tripandevent.sanbot.ui.screens.voice

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripandevent.sanbot.R
import com.tripandevent.sanbot.ui.components.SanbotTopBar
import com.tripandevent.sanbot.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun VoiceScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(uiState.conversationHistory.size) {
        if (uiState.conversationHistory.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(uiState.conversationHistory.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        SanbotTopBar(
            title = stringResource(R.string.talk_to_me),
            onBackClick = onBackClick,
            onHomeClick = onHomeClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VoiceStateIndicator(
                state = uiState.voiceState,
                amplitudeLevel = uiState.amplitudeLevel,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.conversation_history),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.conversationHistory.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start speaking to begin the conversation",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.conversationHistory) { message ->
                                ConversationBubble(message = message)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MicrophoneButton(
                state = uiState.voiceState,
                enabled = hasAudioPermission,
                onClick = { 
                    if (hasAudioPermission) {
                        viewModel.onMicButtonClick() 
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    !hasAudioPermission -> "Tap to grant microphone permission"
                    uiState.voiceState == VoiceState.IDLE -> stringResource(R.string.tap_to_speak)
                    uiState.voiceState == VoiceState.LISTENING -> stringResource(R.string.listening)
                    uiState.voiceState == VoiceState.PROCESSING -> stringResource(R.string.processing)
                    uiState.voiceState == VoiceState.SPEAKING -> stringResource(R.string.speaking)
                    uiState.voiceState == VoiceState.ERROR -> uiState.errorMessage ?: stringResource(R.string.error_occurred)
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    !hasAudioPermission -> WarningYellow
                    uiState.voiceState == VoiceState.ERROR -> ErrorRed
                    else -> OnSurfaceVariant
                }
            )

            if (uiState.voiceState == VoiceState.ERROR) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(text = "Try Again", color = BrandBlue)
                }
            }
            
            if (!hasAudioPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = null,
                            tint = WarningYellow
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Microphone permission required for voice interaction",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Microphone Permission Required") },
            text = {
                Text("Voice interaction requires microphone access. Please grant the permission in Settings to use this feature.")
            },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("OK", color = BrandBlue)
                }
            }
        )
    }
}

@Composable
private fun VoiceStateIndicator(
    state: VoiceState,
    amplitudeLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when (state) {
                    VoiceState.LISTENING -> ErrorRed.copy(alpha = 0.1f)
                    VoiceState.PROCESSING -> BrandBlue.copy(alpha = 0.1f)
                    VoiceState.SPEAKING -> SuccessGreen.copy(alpha = 0.1f)
                    else -> LightGray
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        when (state) {
            VoiceState.LISTENING -> {
                repeat(5) { index ->
                    val delay = index * 100
                    val animatedHeight by infiniteTransition.animateFloat(
                        initialValue = 10f,
                        targetValue = 40f * (amplitudeLevel + 0.2f),
                        animationSpec = infiniteRepeatable(
                            animation = tween(300, delayMillis = delay),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "waveBar$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(8.dp)
                            .height(animatedHeight.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ErrorRed)
                    )
                }
            }
            VoiceState.PROCESSING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = BrandBlue,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.processing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrandBlue
                )
            }
            VoiceState.SPEAKING -> {
                repeat(3) { index ->
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, delayMillis = index * 150),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "speakDot$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size((12 * scale).dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                }
            }
            else -> {
                Text(
                    text = "Ready to listen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MicrophoneButton(
    state: VoiceState,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (state == VoiceState.LISTENING) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "micScale"
    )

    val backgroundColor = when {
        !enabled -> LightGray.copy(alpha = 0.5f)
        state == VoiceState.IDLE || state == VoiceState.ERROR -> LightGray
        state == VoiceState.LISTENING -> ErrorRed
        state == VoiceState.PROCESSING -> BrandBlue.copy(alpha = 0.5f)
        state == VoiceState.SPEAKING -> BrandBlue
        else -> LightGray
    }

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(160.dp)
            .scale(scale),
        shape = CircleShape,
        containerColor = backgroundColor,
        contentColor = if (enabled) White else White.copy(alpha = 0.5f),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Icon(
            imageVector = when {
                !enabled -> Icons.Default.MicOff
                state == VoiceState.SPEAKING -> Icons.Default.Stop
                else -> Icons.Default.Mic
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
private fun ConversationBubble(message: ConversationMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.isUser) BrandBlue else LightGray
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (message.isUser) "You" else "Agent",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isUser) White.copy(alpha = 0.7f) else OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) White else DarkGray
                )
            }
        }
    }
}
