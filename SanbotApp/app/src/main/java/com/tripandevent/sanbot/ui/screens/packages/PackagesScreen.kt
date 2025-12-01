package com.tripandevent.sanbot.ui.screens.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tripandevent.sanbot.R
import com.tripandevent.sanbot.ui.components.*
import com.tripandevent.sanbot.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    onBackClick: () -> Unit,
    onPackageClick: (String) -> Unit,
    viewModel: PackagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        SanbotTopBar(
            title = stringResource(R.string.tour_packages),
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { showFilterSheet = true }) {
                    Badge(
                        containerColor = if (uiState.filter != PackageFilter()) BrandOrange else androidx.compose.ui.graphics.Color.Transparent
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter),
                            tint = White
                        )
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
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.loadPackages() }
                )
            }
            uiState.filteredPackages.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_packages),
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant
                        )
                        if (uiState.filter != PackageFilter()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { viewModel.clearFilter() }) {
                                Text("Clear Filters", color = BrandBlue)
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.filteredPackages,
                        key = { it.id }
                    ) { pkg ->
                        PackageCard(
                            name = pkg.name,
                            price = pkg.price,
                            currency = pkg.currency,
                            duration = pkg.duration,
                            rating = pkg.rating,
                            reviewsCount = pkg.reviewsCount,
                            imageUrl = pkg.thumbnailUrl ?: pkg.imageUrl,
                            onClick = { onPackageClick(pkg.id) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = uiState.filter,
            categories = uiState.categories,
            onApply = { filter ->
                viewModel.updateFilter(filter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false },
            onClear = {
                viewModel.clearFilter()
                showFilterSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    currentFilter: PackageFilter,
    categories: List<String>,
    onApply: (PackageFilter) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentFilter.category) }
    var minPrice by remember { mutableStateOf(currentFilter.minPrice?.toFloat() ?: 0f) }
    var maxPrice by remember { mutableStateOf(currentFilter.maxPrice?.toFloat() ?: 5000f) }
    var minRating by remember { mutableStateOf(currentFilter.minRating ?: 0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter),
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkGray
                )
                TextButton(onClick = onClear) {
                    Text("Clear All", color = BrandBlue)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                color = DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") }
                )
                categories.take(4).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Price Range: AED ${minPrice.toInt()} - ${maxPrice.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            RangeSlider(
                value = minPrice..maxPrice,
                onValueChange = { range ->
                    minPrice = range.start
                    maxPrice = range.endInclusive
                },
                valueRange = 0f..5000f,
                colors = SliderDefaults.colors(
                    thumbColor = BrandBlue,
                    activeTrackColor = BrandBlue
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Minimum Rating: ${String.format("%.1f", minRating)}+",
                style = MaterialTheme.typography.titleMedium,
                color = DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = minRating,
                onValueChange = { minRating = it },
                valueRange = 0f..5f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = BrandBlue,
                    activeTrackColor = BrandBlue
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Apply Filters",
                onClick = {
                    onApply(
                        PackageFilter(
                            category = selectedCategory,
                            minPrice = if (minPrice > 0) minPrice.toInt() else null,
                            maxPrice = if (maxPrice < 5000) maxPrice.toInt() else null,
                            minRating = if (minRating > 0) minRating else null
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
