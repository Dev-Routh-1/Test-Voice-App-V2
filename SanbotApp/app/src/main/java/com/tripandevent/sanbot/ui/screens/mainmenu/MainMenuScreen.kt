package com.tripandevent.sanbot.ui.screens.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tripandevent.sanbot.R
import com.tripandevent.sanbot.ui.components.ActionCard
import com.tripandevent.sanbot.ui.theme.*

@Composable
fun MainMenuScreen(
    onTalkToMeClick: () -> Unit,
    onBrowsePackagesClick: () -> Unit,
    onWatchVideosClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BrandBlue,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = White,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TripAndEvent",
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandBlue
                    )
                }
                
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.main_greeting),
                style = MaterialTheme.typography.headlineMedium,
                color = DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            ActionCard(
                icon = Icons.Default.Mic,
                title = stringResource(R.string.talk_to_me),
                onClick = onTalkToMeClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionCard(
                icon = Icons.Default.CardTravel,
                title = stringResource(R.string.browse_packages),
                onClick = onBrowsePackagesClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionCard(
                icon = Icons.Default.PlayCircle,
                title = stringResource(R.string.watch_videos),
                onClick = onWatchVideosClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionCard(
                icon = Icons.Default.Phone,
                title = stringResource(R.string.contact_us),
                onClick = onContactUsClick
            )
        }
    }
}
