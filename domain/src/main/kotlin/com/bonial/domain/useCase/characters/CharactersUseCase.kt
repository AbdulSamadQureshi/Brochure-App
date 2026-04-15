package com.bonial.domain.useCase.characters

import com.bonial.domain.model.network.response.Request
import com.bonial.domain.repository.CharactersPage
import com.bonial.domain.repository.CharactersRepository
import com.bonial.domain.useCase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Fetches a page of characters. The repository owns DTO → domain mapping so this
 * use case is a thin pass-through seam — there is no business logic on top of the
 * pagination request today, and keeping the seam preserves an injection point for
 * future behaviour (caching policy, favourites merging, etc.) without ViewModels
 * having to call the repository directly.
 */
data class CharactersParams(
    val page: Int,
    val name: String? = null,
)

class CharactersUseCase
    @Inject
    constructor(
        private val repository: CharactersRepository,
    ) : BaseUseCase<CharactersParams, Flow<Request<CharactersPage>>> {
        override suspend fun invoke(params: CharactersParams): Flow<Request<CharactersPage>> =
            repository.characters(params.page, params.name)
    }
