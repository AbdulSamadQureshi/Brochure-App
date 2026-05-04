package com.bonial.brochure.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.bonial.feature.characters.CharactersScreen
import com.bonial.feature.characters.CharactersViewModel
import com.bonial.feature.detail.CharacterDetailKey
import com.bonial.feature.detail.CharacterDetailScreen
import com.bonial.feature.detail.CharacterDetailViewModel

@Composable
fun CharacterNavGraph() {
    val backStack = rememberNavBackStack(CharacterListKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<CharacterListKey> {
                    val viewModel: CharactersViewModel = hiltViewModel()
                    CharactersScreen(
                        viewModel = viewModel,
                        onCharacterClick = { characterId ->
                            backStack.add(CharacterDetailKey(id = characterId))
                        },
                    )
                }

                entry<CharacterDetailKey> { key ->
                    val viewModel =
                        hiltViewModel<CharacterDetailViewModel, CharacterDetailViewModel.Factory>(
                            creationCallback = { factory -> factory.create(key) },
                        )
                    CharacterDetailScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
    )
}
