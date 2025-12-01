package com.tripandevent.sanbot.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tripandevent.sanbot.R
import com.tripandevent.sanbot.ui.theme.*

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit
) {
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        logoVisible = true
        kotlinx.coroutines.delay(500)
        textVisible = true
        kotlinx.coroutines.delay(300)
        buttonVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BrandBlue,
                        BrandBlueDark
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStartClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(80.dp)
                        .background(
                            color = White,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TripAndEvent",
                        style = MaterialTheme.typography.headlineMedium,
                        color = BrandBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(500)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.welcome_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.welcome_subtitle),
                        style = MaterialTheme.typography.headlineSmall,
                        color = White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            AnimatedVisibility(
                visible = buttonVisible,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(500)
                )
            ) {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier
                        .width(280.dp)
                        .height(80.dp)
                        .scale(buttonScale),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = BrandBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.start_button),
                        style = MaterialTheme.typography.headlineMedium,
                        color = BrandBlue
                    )
                }
            }
        }
    }
}
