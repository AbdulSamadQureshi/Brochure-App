package com.bonial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BrochureEntity::class, FavouriteBrochureEntity::class, CharacterEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class BrochuresDatabase : RoomDatabase() {
    abstract fun brochuresDao(): BrochuresDao

    abstract fun favouritesDao(): FavouritesDao

    abstract fun charactersDao(): CharactersDao

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

        /**
         * Version 2 → 3: adds the `characters` table for offline-first caching
         * with a TTL strategy (see [CachePolicy]).
         */
        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `characters` (
                            `id` INTEGER NOT NULL PRIMARY KEY,
                            `name` TEXT,
                            `status` TEXT,
                            `species` TEXT,
                            `imageUrl` TEXT,
                            `page` INTEGER NOT NULL,
                            `totalPages` INTEGER NOT NULL,
                            `cachedAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )
                }
            }
    }
}
