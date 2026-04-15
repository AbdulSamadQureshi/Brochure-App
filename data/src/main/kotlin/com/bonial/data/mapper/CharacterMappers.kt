package com.bonial.data.mapper

import com.bonial.data.remote.model.CharacterDto
import com.bonial.data.remote.model.CharacterResponseDto
import com.bonial.domain.model.Character
import com.bonial.domain.model.CharacterDetail
import com.bonial.domain.repository.CharactersPage

internal fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    imageUrl = image,
)

internal fun CharacterDto.toDomainDetail(): CharacterDetail = CharacterDetail(
    id = id,
    name = name,
    status = status,
    species = species,
    gender = gender,
    origin = origin?.name,
    location = location?.name,
    imageUrl = image,
)

internal fun CharacterResponseDto.toDomainPage(): CharactersPage = CharactersPage(
    characters = results.orEmpty().map { it.toDomain() },
    totalPages = info?.pages ?: 1,
)
