package com.bonial.domain.useCase.characters

import com.bonial.domain.model.CharacterWithFavourite
import com.bonial.domain.model.CharactersWithFavouritePage
import com.bonial.domain.model.network.response.Request
import com.bonial.domain.repository.CharactersRepository
import com.bonial.domain.repository.FavouritesRepository
import com.bonial.domain.useCase.BaseUseCase
import com.bonial.domain.utils.mapSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetEnrichedCharactersUseCase @Inject constructor(
    private val charactersRepository: CharactersRepository,
    private val favouritesRepository: FavouritesRepository,
) : BaseUseCase<CharactersParams, Flow<Request<CharactersWithFavouritePage>>> {

    override suspend fun invoke(params: CharactersParams): Flow<Request<CharactersWithFavouritePage>> {
        val sanitizedParams = params.copy(name = params.name?.ifBlank { null })
        
        return combine(
            charactersRepository.characters(sanitizedParams.page, sanitizedParams.name),
            favouritesRepository.getFavouriteCoverUrls()
        ) { charactersRequest, favouriteUrls ->
            charactersRequest.mapSuccess { page ->
                CharactersWithFavouritePage(
                    characters = page.characters.map { character ->
                        CharacterWithFavourite(
                            id = character.id,
                            name = character.name,
                            status = character.status,
                            species = character.species,
                            imageUrl = character.imageUrl,
                            isFavourite = character.imageUrl != null && character.imageUrl in favouriteUrls
                        )
                    },
                    totalPages = page.totalPages
                )
            }
        }
    }
}
