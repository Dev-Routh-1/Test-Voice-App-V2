package com.tripandevent.sanbot.ui.screens.packages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripandevent.sanbot.data.models.TourPackage
import com.tripandevent.sanbot.data.repository.SanbotRepository
import com.tripandevent.sanbot.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackageFilter(
    val category: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minRating: Float? = null
)

data class PackagesUiState(
    val isLoading: Boolean = true,
    val packages: List<TourPackage> = emptyList(),
    val filteredPackages: List<TourPackage> = emptyList(),
    val filter: PackageFilter = PackageFilter(),
    val categories: List<String> = emptyList(),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class PackagesViewModel @Inject constructor(
    private val repository: SanbotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackagesUiState())
    val uiState: StateFlow<PackagesUiState> = _uiState.asStateFlow()

    init {
        loadPackages()
    }

    fun loadPackages() {
        viewModelScope.launch {
            repository.getPackages(limit = 50).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is NetworkResult.Success -> {
                        val packages = result.data?.packages ?: emptyList()
                        val categories = packages.map { it.category }.distinct()
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                packages = packages,
                                filteredPackages = applyFilter(packages, state.filter),
                                categories = categories,
                                errorMessage = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = result.message ?: "Failed to load packages"
                            ) 
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadPackages()
    }

    fun updateFilter(filter: PackageFilter) {
        _uiState.update { state ->
            state.copy(
                filter = filter,
                filteredPackages = applyFilter(state.packages, filter)
            )
        }
    }

    fun clearFilter() {
        updateFilter(PackageFilter())
    }

    private fun applyFilter(packages: List<TourPackage>, filter: PackageFilter): List<TourPackage> {
        return packages.filter { pkg ->
            val categoryMatch = filter.category == null || pkg.category == filter.category
            val minPriceMatch = filter.minPrice == null || pkg.price >= filter.minPrice
            val maxPriceMatch = filter.maxPrice == null || pkg.price <= filter.maxPrice
            val ratingMatch = filter.minRating == null || pkg.rating >= filter.minRating
            categoryMatch && minPriceMatch && maxPriceMatch && ratingMatch
        }
    }
}
