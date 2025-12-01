package com.tripandevent.sanbot.ui.screens.media

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.tripandevent.sanbot.R
import com.tripandevent.sanbot.ui.components.*
import com.tripandevent.sanbot.ui.theme.*
import com.tripandevent.sanbot.data.models.MediaItem as SanbotMediaItem

@Composable
fun MediaScreen(
    onBackClick: () -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.selectedVideoUrl != null) {
        VideoPlayerScreen(
            videoUrl = uiState.selectedVideoUrl!!,
            onDismiss = { viewModel.clearVideoSelection() }
        )
    } else {
        MediaGalleryContent(
            uiState = uiState,
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}

@Composable
private fun MediaGalleryContent(
    uiState: MediaUiState,
    viewModel: MediaViewModel,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        SanbotTopBar(
            title = stringResource(R.string.media_gallery),
            onBackClick = onBackClick,
            actions = {
                var showFilterMenu by remember { mutableStateOf(false) }
                
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter),
                            tint = White
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                viewModel.filterByCategory(null)
                                showFilterMenu = false
                            }
                        )
                        uiState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.filterByCategory(category)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }
        )

        when {
            uiState.isLoading -> {
                LoadingScreen()
            }
            uiState.errorMessage != null -> {
                ErrorScreen(
                    message = uiState.errorMessage,
                    onRetry = { viewModel.loadMedia() }
                )
            }
            else -> {
                val filteredVideos = viewModel.getFilteredVideos()
                val filteredImages = viewModel.getFilteredImages()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (filteredVideos.isNotEmpty()) {
                        item {
                            Text(
                                text = "Videos",
                                style = MaterialTheme.typography.titleLarge,
                                color = DarkGray
                            )
                        }

                        items(filteredVideos) { video ->
                            VideoCard(
                                video = video,
                                onClick = { viewModel.selectVideo(video.url) }
                            )
                        }
                    }

                    if (filteredImages.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Images",
                                style = MaterialTheme.typography.titleLarge,
                                color = DarkGray
                            )
                        }

                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.height((((filteredImages.size + 2) / 3) * 120).dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredImages) { image ->
                                    ImageThumbnail(image = image)
                                }
                            }
                        }
                    }

                    if (filteredVideos.isEmpty() && filteredImages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = OnSurfaceVariant,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.no_media),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: SanbotMediaItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = video.thumbnail,
                contentDescription = video.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .background(
                        color = DarkGray.copy(alpha = 0.7f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = White,
                    modifier = Modifier.size(40.dp)
                )
            }

            if (video.durationSeconds != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    color = DarkGray.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = formatDuration(video.durationSeconds),
                        style = MaterialTheme.typography.labelSmall,
                        color = White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium,
                color = DarkGray
            )
            Text(
                text = video.category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImageThumbnail(image: SanbotMediaItem) {
    AsyncImage(
        model = image.url,
        contentDescription = image.title,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerScreen(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = White
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}
