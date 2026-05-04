package com.bonial.feature.characters

data class CharacterUi(
    val id: Int,
    val name: String?,
    val status: String?,
    val species: String?,
    val imageUrl: String?,
    val isFavourite: Boolean = false,
)
