package com.bonial.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharactersDao {
    /**
     * Returns a [PagingSource] over all cached characters ordered by id.
     * Room regenerates the source automatically whenever the table changes,
     * which is what keeps the Paging 3 UI in sync after [CharactersRemoteMediator]
     * writes new data.
     */
    @Query("SELECT * FROM characters ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, CharacterEntity>

    /** Returns all cached characters for [page], ordered by their original list position. */
    @Query("SELECT * FROM characters WHERE page = :page ORDER BY id ASC")
    suspend fun getByPage(page: Int): List<CharacterEntity>

    /**
     * Returns the [CharacterEntity.cachedAt] timestamp of the first row for [page],
     * or null when no cache entry exists.  Used by the repository to check freshness
     * before deciding whether to hit the network.
     */
    @Query("SELECT cachedAt FROM characters WHERE page = :page LIMIT 1")
    suspend fun getCachedAt(page: Int): Long?

    /** Returns the [CharacterEntity.totalPages] value stored with the given page. */
    @Query("SELECT totalPages FROM characters WHERE page = :page LIMIT 1")
    suspend fun getTotalPages(page: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    /** Clears all cached characters — called when the user explicitly refreshes. */
    @Query("DELETE FROM characters")
    suspend fun deleteAll()

    /** Clears cached characters for a single page — useful after a failed partial write. */
    @Query("DELETE FROM characters WHERE page = :page")
    suspend fun deleteByPage(page: Int)
}
