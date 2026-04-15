package com.bonial.domain.useCase.favourites

import com.bonial.domain.repository.FavouritesRepository
import javax.inject.Inject

class ToggleFavouriteUseCase
    @Inject
    constructor(
        private val repository: FavouritesRepository,
    ) {
        suspend operator fun invoke(
            coverUrl: String,
            isFavourite: Boolean,
        ) {
            if (isFavourite) {
                repository.removeFavourite(coverUrl)
            } else {
                repository.addFavourite(coverUrl)
            }
        }
    }
