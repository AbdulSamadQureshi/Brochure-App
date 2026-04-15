package com.bonial.brochure.presentation.home

import androidx.lifecycle.viewModelScope
import com.bonial.brochure.presentation.model.CharacterUi
import com.bonial.brochure.presentation.utils.toErrorMessage
import com.bonial.core.base.MviViewModel
import com.bonial.domain.model.network.response.Request
import com.bonial.domain.repository.CharactersPage
import com.bonial.domain.useCase.characters.CharactersParams
import com.bonial.domain.useCase.characters.CharactersUseCase
import com.bonial.domain.useCase.favourites.GetFavouriteCoverUrlsUseCase
import com.bonial.domain.useCase.favourites.ToggleFavouriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharactersState(
    val characters: List<CharacterUi> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val searchQuery: String = "",
    val isInitialLoading: Boolean = true,
)

sealed class CharactersIntent {
    object LoadCharacters : CharactersIntent()
    object LoadNextPage : CharactersIntent()
    data class ToggleFavourite(val character: CharacterUi) : CharactersIntent()
    data class Search(val query: String) : CharactersIntent()
}

sealed class CharactersEffect {
    data class ShowError(val message: String) : CharactersEffect()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val charactersUseCase: CharactersUseCase,
    private val getFavouriteCoverUrlsUseCase: GetFavouriteCoverUrlsUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
) : MviViewModel<CharactersState, CharactersIntent, CharactersEffect>() {

    private val searchQueryFlow = MutableStateFlow("")

    override fun createInitialState(): CharactersState = CharactersState()

    init {
        observeSearchAndFavourites()
        sendIntent(CharactersIntent.LoadCharacters)
    }

    override fun handleIntent(intent: CharactersIntent) {
        when (intent) {
            is CharactersIntent.LoadCharacters -> {
                searchQueryFlow.update { it } // Trigger search with current query
            }
            is CharactersIntent.LoadNextPage -> {
                val state = uiState.value
                if (!state.isLoadingNextPage && state.currentPage < state.totalPages) {
                    loadNextPage(page = state.currentPage + 1, query = state.searchQuery)
                }
            }
            is CharactersIntent.ToggleFavourite -> toggleFavourite(intent.character)
            is CharactersIntent.Search -> {
                setState {
                    copy(
                        searchQuery = intent.query,
                        characters = emptyList(),
                        isLoading = true,
                        error = null
                    )
                }
                searchQueryFlow.value = intent.query
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeSearchAndFavourites() {
        viewModelScope.launch {
            combine(
                searchQueryFlow
                    .debounce { query -> if (query.isEmpty()) 0L else 500L }
                    .distinctUntilChanged(),
                getFavouriteCoverUrlsUseCase(),
            ) { query, favouriteUrls ->
                query to favouriteUrls
            }.flatMapLatest { (query, favouriteUrls) ->
                val sanitizedQuery = query.ifBlank { null }

                charactersUseCase(CharactersParams(page = 1, name = sanitizedQuery))
                    .map { response -> response to favouriteUrls }
            }.collect { (response, savedFavourites) ->
                handleResponse(response, savedFavourites, isNextPage = false, page = 1)
            }
        }
    }

    private fun loadNextPage(page: Int, query: String?) {
        val sanitizedQuery = query?.ifBlank { null }
        viewModelScope.launch {
            setState { copy(isLoadingNextPage = true) }

            val savedFavourites = getFavouriteCoverUrlsUseCase().first()

            charactersUseCase(CharactersParams(page, sanitizedQuery)).collectLatest { response ->
                handleResponse(response, savedFavourites, isNextPage = true, page = page)
            }
        }
    }

    private fun handleResponse(
        response: Request<CharactersPage>,
        savedFavourites: Set<String>,
        isNextPage: Boolean,
        page: Int
    ) {
        when (response) {
            is Request.Loading -> {
                setState {
                    copy(
                        isLoading = if (isNextPage) isLoading else true,
                        isLoadingNextPage = isNextPage
                    )
                }
            }
            is Request.Success -> {
                val newItems = response.data.characters.map { character ->
                    CharacterUi(
                        id = character.id,
                        name = character.name,
                        status = character.status,
                        species = character.species,
                        imageUrl = character.imageUrl,
                        isFavourite = character.imageUrl != null && character.imageUrl in savedFavourites,
                    )
                }
                setState {
                    copy(
                        characters = if (isNextPage) characters + newItems else newItems,
                        isLoading = false,
                        isLoadingNextPage = false,
                        currentPage = page,
                        totalPages = response.data.totalPages,
                        error = null,
                        isInitialLoading = false,
                    )
                }
            }
            is Request.Error -> {
                val isNoResults = response.apiError?.code == "404"
                val message = response.apiError.toErrorMessage()

                setState {
                    copy(
                        characters = if (!isNextPage && isNoResults) emptyList() else characters,
                        isLoading = false,
                        isLoadingNextPage = false,
                        error = if (isNoResults) null else message,
                        isInitialLoading = false,
                    )
                }

                if (!isNoResults) {
                    setEffect { CharactersEffect.ShowError(message) }
                }
            }
        }
    }

    private fun toggleFavourite(character: CharacterUi) {
        val imageUrl = character.imageUrl ?: return
        viewModelScope.launch {
            toggleFavouriteUseCase(imageUrl, character.isFavourite)
        }
    }
}
