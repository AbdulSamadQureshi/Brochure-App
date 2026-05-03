package com.bonial.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that caches a single character page result.
 *
 * [cachedAt] is a Unix-epoch millisecond timestamp set by [CharactersRepositoryImpl]
 * at insertion time. The repository uses it to decide whether the cache is still
 * fresh before hitting the network (see [com.bonial.data.local.CachePolicy]).
 *
 * [page] stores which API page this character belongs to so the full list can be
 * reconstructed page-by-page from the local database.
 */
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String?,
    val status: String?,
    val species: String?,
    val imageUrl: String?,
    val page: Int,
    val totalPages: Int,
    val cachedAt: Long,
)
