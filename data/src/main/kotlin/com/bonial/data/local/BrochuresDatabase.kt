package com.bonial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BrochureEntity::class, FavouriteBrochureEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class BrochuresDatabase : RoomDatabase() {
    abstract fun brochuresDao(): BrochuresDao

    abstract fun favouritesDao(): FavouritesDao

    companion object {
        /**
         * Version 1 → 2: adds the `favourite_brochures` table.
         */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `favourite_brochures` (
                            `coverUrl` TEXT NOT NULL PRIMARY KEY
                        )
                        """.trimIndent(),
                    )
                }
            }
    }
}
