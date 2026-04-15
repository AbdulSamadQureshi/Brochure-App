package com.bonial.domain.model

data class CharacterWithFavourite(
    val id: Int,
    val name: String?,
    val status: String?,
    val species: String?,
    val imageUrl: String?,
    val isFavourite: Boolean,
)

data class CharactersWithFavouritePage(
    val characters: List<CharacterWithFavourite>,
    val totalPages: Int,
)
