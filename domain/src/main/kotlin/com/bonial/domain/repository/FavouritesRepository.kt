package com.bonial.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavouritesRepository {
    suspend fun addFavourite(coverUrl: String)

    suspend fun removeFavourite(coverUrl: String)

    fun isFavouriteFlow(coverUrl: String): Flow<Boolean>

    fun getFavouriteCoverUrls(): Flow<Set<String>>
}
