package com.bonial.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavouriteBrochureEntity)

    @Query("DELETE FROM favourite_brochures WHERE coverUrl = :coverUrl")
    suspend fun delete(coverUrl: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_brochures WHERE coverUrl = :coverUrl)")
    fun isFavouriteFlow(coverUrl: String): Flow<Boolean>

    @Query("SELECT coverUrl FROM favourite_brochures")
    fun getAllCoverUrls(): Flow<List<String>>
}
