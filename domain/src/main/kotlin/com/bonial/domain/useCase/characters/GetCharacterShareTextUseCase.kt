package com.bonial.domain.useCase.characters

import com.bonial.domain.model.CharacterDetail
import com.bonial.domain.useCase.BaseUseCase
import javax.inject.Inject

class GetCharacterShareTextUseCase @Inject constructor() : BaseUseCase<CharacterDetail, String> {

    override suspend fun invoke(params: CharacterDetail): String {
        return buildString {
            append(params.name ?: "")
            params.species?.let { species ->
                append(" · $species")
            }
            params.status?.let { status ->
                append(" · $status")
            }
            params.imageUrl?.let { imageUrl ->
                append("\n$imageUrl")
            }
        }
    }
}
