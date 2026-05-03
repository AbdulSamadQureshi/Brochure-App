package com.bonial.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.bonial.data.local.CachePolicy
import com.bonial.data.local.CharacterEntity
import com.bonial.data.local.CharactersDao
import com.bonial.data.mapper.toEntity
import com.bonial.data.remote.service.CharactersApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * [RemoteMediator] for the paginated characters list.
 *
 * Drives the three-layer offline-first pattern:
 *  - **Room** is the single source of truth for the UI.
 *  - **[CharactersRemoteMediator]** fills Room from the network when the cache
 *    is absent, stale, or the user triggers a refresh.
 *  - The UI observes a [androidx.paging.PagingSource] backed by Room and never
 *    talks to the network directly.
 *
 * **TTL strategy:**
 * On [LoadType.REFRESH], if the first page is still fresh (within
 * [CachePolicy.CHARACTER_TTL_MS]), the mediator returns [MediatorResult.Success]
 * with `endOfPaginationReached = false` without hitting the network. This keeps
 * the app usable offline and avoids redundant API calls on config changes.
 *
 * **Page mapping:**
 * The Rick & Morty API uses 1-based page numbers. Page 1 is loaded for
 * [LoadType.REFRESH] and [LoadType.PREPEND]. [LoadType.APPEND] advances the
 * page counter by reading the current item count from [PagingState].
 *
 * **Usage (wire up in repository):**
 * ```kotlin
 * Pager(
 *     config = PagingConfig(pageSize = PAGE_SIZE),
 *     remoteMediator = CharactersRemoteMediator(apiService, charactersDao),
 *     pagingSourceFactory = { charactersDao.pagingSource() },
 * ).flow
 * ```
 */
@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediator(
    private val apiService: CharactersApiService,
    private val charactersDao: CharactersDao,
) : RemoteMediator<Int, CharacterEntity>() {
    override suspend fun initialize(): InitializeAction {
        val cachedAt = charactersDao.getCachedAt(page = FIRST_PAGE) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        return if (CachePolicy.isFresh(cachedAt)) {
            // Cache is fresh — skip the initial network refresh and let Room serve the UI.
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>,
    ): MediatorResult {
        val page =
            when (loadType) {
                LoadType.REFRESH -> FIRST_PAGE
                // Prepend is not supported — the API is append-only.
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    // Derive the next page from the number of items already loaded.
                    val loadedCount = state.pages.sumOf { it.data.size }
                    if (loadedCount == 0) return MediatorResult.Success(endOfPaginationReached = false)
                    (loadedCount / state.config.pageSize) + 1
                }
            }

        return try {
            val response = apiService.characters(page = page)
            val totalPages = response.info?.pages ?: 1
            val now = System.currentTimeMillis()
            val entities =
                (response.results ?: emptyList()).map { dto ->
                    dto.toEntity(page = page, totalPages = totalPages, cachedAt = now)
                }

            if (loadType == LoadType.REFRESH) {
                charactersDao.deleteAll()
            }
            charactersDao.insertAll(entities)

            MediatorResult.Success(endOfPaginationReached = page >= totalPages)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    companion object {
        private const val FIRST_PAGE = 1
    }
}
