package com.bonial.domain.useCase.characters

import com.bonial.domain.model.CharacterDetail
import com.bonial.domain.model.network.response.Request
import com.bonial.domain.repository.CharactersRepository
import com.bonial.domain.useCase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Fetches a single character by id. Repository now returns domain models directly, so
 * this use case only guards against an invalid id — it emits a descriptive Error rather
 * than silently substituting defaults or returning an empty flow.
 */
class CharacterDetailUseCase
    @Inject
    constructor(
        private val repository: CharactersRepository,
    ) : BaseUseCase<Int, Flow<Request<CharacterDetail>>> {
        override suspend fun invoke(params: Int): Flow<Request<CharacterDetail>> = repository.character(params)
    }
