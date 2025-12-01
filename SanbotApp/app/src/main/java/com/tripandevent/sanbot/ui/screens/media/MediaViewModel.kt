package com.tripandevent.sanbot.ui.screens.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripandevent.sanbot.data.models.MediaItem
import com.tripandevent.sanbot.data.repository.SanbotRepository
import com.tripandevent.sanbot.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaUiState(
    val isLoading: Boolean = true,
    val videos: List<MediaItem> = emptyList(),
    val images: List<MediaItem> = emptyList(),
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val errorMessage: String? = null,
    val selectedVideoUrl: String? = null
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val repository: SanbotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    init {
        loadMedia()
    }

    fun loadMedia() {
        viewModelScope.launch {
            repository.getMedia().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is NetworkResult.Success -> {
                        val allMedia = result.data?.media ?: emptyList()
                        val videos = allMedia.filter { it.type == "video" }
                        val images = allMedia.filter { it.type == "image" }
                        val categories = allMedia.map { it.category }.distinct()

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                videos = videos,
                                images = images,
                                categories = categories,
                                errorMessage = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Failed to load media"
                            )
                        }
                    }
                }
            }
        }
    }

    fun filterByCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectVideo(url: String) {
        _uiState.update { it.copy(selectedVideoUrl = url) }
    }

    fun clearVideoSelection() {
        _uiState.update { it.copy(selectedVideoUrl = null) }
    }

    fun getFilteredVideos(): List<MediaItem> {
        val state = _uiState.value
        return if (state.selectedCategory != null) {
            state.videos.filter { it.category == state.selectedCategory }
        } else {
            state.videos
        }
    }

    fun getFilteredImages(): List<MediaItem> {
        val state = _uiState.value
        return if (state.selectedCategory != null) {
            state.images.filter { it.category == state.selectedCategory }
        } else {
            state.images
        }
    }
}
