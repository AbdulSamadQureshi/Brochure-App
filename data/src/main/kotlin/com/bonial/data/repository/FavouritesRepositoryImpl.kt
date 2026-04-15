package com.bonial.data.repository

import com.bonial.data.local.FavouriteBrochureEntity
import com.bonial.data.local.FavouritesDao
import com.bonial.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesRepositoryImpl
    @Inject
    constructor(
        private val favouritesDao: FavouritesDao,
    ) : FavouritesRepository {
        override suspend fun addFavourite(coverUrl: String) {
            favouritesDao.insert(FavouriteBrochureEntity(coverUrl))
        }

        override suspend fun removeFavourite(coverUrl: String) {
            favouritesDao.delete(coverUrl)
        }

        override fun isFavouriteFlow(coverUrl: String): Flow<Boolean> = favouritesDao.isFavouriteFlow(coverUrl)

        override fun getFavouriteCoverUrls(): Flow<Set<String>> = favouritesDao.getAllCoverUrls().map { it.toSet() }
    }
