package com.bonial.domain.useCase.favourites

import com.bonial.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsFavouriteFlowUseCase
    @Inject
    constructor(
        private val repository: FavouritesRepository,
    ) {
        operator fun invoke(coverUrl: String): Flow<Boolean> = repository.isFavouriteFlow(coverUrl)
    }
