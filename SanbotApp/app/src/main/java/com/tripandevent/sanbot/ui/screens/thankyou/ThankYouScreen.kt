package com.tripandevent.sanbot.ui.screens.thankyou

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripandevent.sanbot.R
import com.tripandevent.sanbot.ui.components.*
import com.tripandevent.sanbot.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ThankYouScreen(
    onHomeClick: () -> Unit,
    viewModel: ThankYouViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSuccessAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showSuccessAnimation = true
    }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess != null) {
            delay(3000)
            viewModel.clearActionMessage()
        }
    }

    LaunchedEffect(Unit) {
        delay(30_000)
        viewModel.clearSession()
        onHomeClick()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "success")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = scaleIn(
                    initialScale = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .background(
                            color = SuccessGreen,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 300)) +
                        slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = tween(500, delayMillis = 300)
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.thank_you),
                        style = MaterialTheme.typography.headlineLarge,
                        color = DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.request_submitted),
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 600)) +
                        slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = tween(500, delayMillis = 600)
                        )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.customerPhone.isNotBlank()) {
                        ActionCard(
                            icon = Icons.Default.Sms,
                            title = stringResource(R.string.send_sms),
                            onClick = { viewModel.sendSms() }
                        )

                        ActionCard(
                            icon = Icons.Default.Chat,
                            title = stringResource(R.string.send_whatsapp),
                            onClick = { viewModel.sendWhatsApp() }
                        )
                    }

                    if (uiState.customerEmail.isNotBlank()) {
                        ActionCard(
                            icon = Icons.Default.Email,
                            title = stringResource(R.string.email_quote),
                            onClick = { viewModel.sendEmail() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 900))
            ) {
                TextButton(
                    onClick = {
                        viewModel.clearSession()
                        onHomeClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = BrandBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.back_to_home),
                        style = MaterialTheme.typography.labelLarge,
                        color = BrandBlue
                    )
                }
            }
        }

        if (uiState.isSendingSms || uiState.isSendingWhatsApp || uiState.isSendingEmail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandBlue)
            }
        }

        if (uiState.actionSuccess != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = SuccessGreen
            ) {
                Text(text = uiState.actionSuccess!!)
            }
        }

        if (uiState.actionError != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = ErrorRed
            ) {
                Text(text = uiState.actionError!!)
            }
        }
    }
}
