package com.bonial.data.repository

import com.bonial.data.local.CachePolicy
import com.bonial.data.local.CharactersDao
import com.bonial.data.mapper.toDomain
import com.bonial.data.mapper.toDomainDetail
import com.bonial.data.mapper.toDomainPage
import com.bonial.data.mapper.toEntity
import com.bonial.data.remote.service.CharactersApiService
import com.bonial.domain.model.CharacterDetail
import com.bonial.domain.model.network.response.Request
import com.bonial.domain.repository.CharactersPage
import com.bonial.domain.repository.CharactersRepository
import com.bonial.domain.utils.mapSuccess
import com.bonial.utils.manageThrowable
import com.bonial.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first implementation of [CharactersRepository].
 *
 * **Cache strategy for [characters]:**
 * 1. Emit [Request.Loading].
 * 2. Check Room for a cached page.
 * 3. If the cache is fresh (within [CachePolicy.CHARACTER_TTL_MS]) emit it as
 *    [Request.Success] and return — no network call is made.
 * 4. If the cache is stale or absent, fetch from the network, persist the
 *    result to Room, then emit [Request.Success] with the fresh data.
 * 5. Network errors bubble up as [Request.Error]; the caller still has the
 *    previously cached data on screen if it was shown before the error.
 *
 * **Search queries** always bypass the cache and go straight to the network
 * because filtered result sets should never be cached alongside the unfiltered
 * character list.
 *
 * **[character]** (detail endpoint) is always fetched live — detail pages are
 * accessed infrequently and caching them would complicate the entity schema
 * without meaningful benefit.
 */
@Singleton
class CharactersRepositoryImpl
    @Inject
    constructor(
        private val apiService: CharactersApiService,
        private val charactersDao: CharactersDao,
    ) : CharactersRepository {
        override fun characters(
            page: Int,
            name: String?,
        ): Flow<Request<CharactersPage>> {
            // Search queries always go straight to the network.
            if (!name.isNullOrBlank()) {
                return safeApiCall { apiService.characters(page, name) }.map { request ->
                    request.mapSuccess { it.toDomainPage() }
                }
            }

            return flow {
                emit(Request.Loading)

                val cachedAt = charactersDao.getCachedAt(page)
                if (cachedAt != null && CachePolicy.isFresh(cachedAt)) {
                    // Cache hit — serve from Room without touching the network.
                    val entities = charactersDao.getByPage(page)
                    val totalPages = charactersDao.getTotalPages(page) ?: 1
                    emit(
                        Request.Success(
                            CharactersPage(
                                characters = entities.map { it.toDomain() },
                                totalPages = totalPages,
                            ),
                        ),
                    )
                    return@flow
                }

                // Cache miss or stale — fetch from network and persist.
                try {
                    val response = apiService.characters(page, name)
                    val now = System.currentTimeMillis()
                    val totalPages = response.info?.pages ?: 1
                    val entities =
                        (response.results ?: emptyList()).map { dto ->
                            dto.toEntity(page = page, totalPages = totalPages, cachedAt = now)
                        }
                    charactersDao.insertAll(entities)

                    emit(
                        Request.Success(
                            CharactersPage(
                                characters = entities.map { it.toDomain() },
                                totalPages = totalPages,
                            ),
                        ),
                    )
                } catch (e: Exception) {
                    emit(Request.Error(apiError = manageThrowable(e)))
                }
            }.flowOn(Dispatchers.IO)
        }

        override fun character(id: Int): Flow<Request<CharacterDetail>> =
            safeApiCall { apiService.character(id) }.map { request ->
                request.mapSuccess { it.toDomainDetail() }
            }
    }
