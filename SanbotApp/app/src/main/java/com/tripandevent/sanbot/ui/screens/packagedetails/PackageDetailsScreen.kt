package com.tripandevent.sanbot.ui.screens.packagedetails

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tripandevent.sanbot.R
import com.tripandevent.sanbot.ui.components.*
import com.tripandevent.sanbot.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackageDetailsScreen(
    onBackClick: () -> Unit,
    onBookNowClick: (String) -> Unit,
    onGetQuoteClick: (String) -> Unit,
    viewModel: PackageDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var highlightsExpanded by remember { mutableStateOf(true) }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var itineraryExpanded by remember { mutableStateOf(false) }
    var inclusionsExpanded by remember { mutableStateOf(false) }
    var exclusionsExpanded by remember { mutableStateOf(false) }

    var showShareDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearActionMessage()
        }
    }

    when {
        uiState.isLoading -> {
            LoadingScreen()
        }
        uiState.errorMessage != null -> {
            ErrorScreen(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.loadPackageDetail() }
            )
        }
        uiState.packageDetail != null -> {
            val pkg = uiState.packageDetail!!
            
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceLight)
                        .verticalScroll(scrollState)
                        .padding(bottom = 100.dp)
                ) {
                    Box {
                        val pagerState = rememberPagerState(pageCount = { pkg.images.size.coerceAtLeast(1) })
                        
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) { page ->
                            AsyncImage(
                                model = pkg.images.getOrNull(page),
                                contentDescription = pkg.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                                .background(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = White
                            )
                        }

                        if (pkg.images.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(pkg.images.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (pagerState.currentPage == index) White
                                                else White.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = pkg.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = DarkGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${pkg.currency} ${pkg.price}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = BrandOrange,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " per person",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RatingStars(rating = pkg.rating)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${pkg.rating} (${pkg.reviewsCount} reviews)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = pkg.duration,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ExpandableSection(
                            title = stringResource(R.string.highlights),
                            expanded = highlightsExpanded,
                            onToggle = { highlightsExpanded = !highlightsExpanded }
                        ) {
                            Column {
                                pkg.highlights.forEach { highlight ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = highlight,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        ExpandableSection(
                            title = stringResource(R.string.description),
                            expanded = descriptionExpanded,
                            onToggle = { descriptionExpanded = !descriptionExpanded }
                        ) {
                            Text(
                                text = pkg.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkGray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (pkg.itinerary.isNotEmpty()) {
                            ExpandableSection(
                                title = stringResource(R.string.itinerary),
                                expanded = itineraryExpanded,
                                onToggle = { itineraryExpanded = !itineraryExpanded }
                            ) {
                                Column {
                                    pkg.itinerary.forEach { item ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = item.time,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = BrandBlue,
                                                modifier = Modifier.width(60.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = item.activity,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (pkg.inclusions.isNotEmpty()) {
                            ExpandableSection(
                                title = stringResource(R.string.inclusions),
                                expanded = inclusionsExpanded,
                                onToggle = { inclusionsExpanded = !inclusionsExpanded }
                            ) {
                                Column {
                                    pkg.inclusions.forEach { inclusion ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = inclusion,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (pkg.exclusions.isNotEmpty()) {
                            ExpandableSection(
                                title = stringResource(R.string.exclusions),
                                expanded = exclusionsExpanded,
                                onToggle = { exclusionsExpanded = !exclusionsExpanded }
                            ) {
                                Column {
                                    pkg.exclusions.forEach { exclusion ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = ErrorRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = exclusion,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.addToSelectedPackages()
                                onBookNowClick(pkg.id)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Text(
                                text = stringResource(R.string.book_now),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.addToSelectedPackages()
                                onGetQuoteClick(pkg.id)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.get_quote),
                                style = MaterialTheme.typography.labelLarge,
                                color = BrandBlue
                            )
                        }

                        IconButton(
                            onClick = { showShareDialog = true },
                            modifier = Modifier
                                .size(56.dp)
                                .border(
                                    width = 1.dp,
                                    color = LightGray,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.share),
                                tint = BrandBlue
                            )
                        }
                    }
                }

                if (uiState.actionSuccess != null) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp, start = 16.dp, end = 16.dp),
                        containerColor = SuccessGreen
                    ) {
                        Text(text = uiState.actionSuccess!!)
                    }
                }

                if (uiState.actionError != null) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp, start = 16.dp, end = 16.dp),
                        containerColor = ErrorRed
                    ) {
                        Text(text = uiState.actionError!!)
                    }
                }
            }
        }
    }

    if (showShareDialog) {
        ShareOptionsDialog(
            onDismiss = { showShareDialog = false },
            onSmsClick = { 
                showShareDialog = false
            },
            onWhatsAppClick = { 
                showShareDialog = false
            },
            onEmailClick = { 
                showShareDialog = false
            }
        )
    }
}

@Composable
private fun ShareOptionsDialog(
    onDismiss: () -> Unit,
    onSmsClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Package") },
        text = {
            Column {
                TextButton(
                    onClick = onSmsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = BrandBlue
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Send via SMS", color = DarkGray)
                }
                TextButton(
                    onClick = onWhatsAppClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = SuccessGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Send via WhatsApp", color = DarkGray)
                }
                TextButton(
                    onClick = onEmailClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = BrandOrange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Send via Email", color = DarkGray)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
