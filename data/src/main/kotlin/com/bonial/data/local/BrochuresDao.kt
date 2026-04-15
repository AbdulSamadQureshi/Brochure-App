package com.bonial.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BrochuresDao {
    @Query("SELECT * FROM brochures")
    suspend fun getAll(): List<BrochureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(brochures: List<BrochureEntity>)

    @Query("DELETE FROM brochures")
    suspend fun deleteAll()
}
