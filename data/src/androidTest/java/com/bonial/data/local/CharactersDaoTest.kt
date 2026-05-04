package com.bonial.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CharactersDao].
 *
 * Exercises the SQL queries, REPLACE conflict strategy, and TTL-related
 * [CharactersDao.getCachedAt] / [CharactersDao.getTotalPages] helpers that
 * [com.bonial.data.repository.CharactersRepositoryImpl] relies on for its
 * offline-first cache strategy.
 */
@RunWith(AndroidJUnit4::class)
class CharactersDaoTest {
    private lateinit var db: BrochuresDatabase
    private lateinit var dao: CharactersDao

    private val now = System.currentTimeMillis()

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    BrochuresDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        dao = db.charactersDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── getByPage ─────────────────────────────────────────────────────────────

    @Test
    fun getByPage_returns_only_characters_for_requested_page() =
        runTest {
            dao.insertAll(makeEntities(page = 1, count = 3))
            dao.insertAll(makeEntities(page = 2, count = 2, idOffset = 100))

            val page1 = dao.getByPage(1)
            assertThat(page1).hasSize(3)
            page1.forEach { assertThat(it.page).isEqualTo(1) }
        }

    @Test
    fun getByPage_returns_empty_when_no_data_exists() =
        runTest {
            assertThat(dao.getByPage(1)).isEmpty()
        }

    // ── getCachedAt ───────────────────────────────────────────────────────────

    @Test
    fun getCachedAt_returns_null_before_any_insert() =
        runTest {
            assertThat(dao.getCachedAt(1)).isNull()
        }

    @Test
    fun getCachedAt_returns_the_timestamp_set_at_insertion() =
        runTest {
            val cachedAt = now - 5_000L
            dao.insertAll(makeEntities(page = 1, count = 2, cachedAt = cachedAt))

            assertThat(dao.getCachedAt(1)).isEqualTo(cachedAt)
        }

    // ── getTotalPages ─────────────────────────────────────────────────────────

    @Test
    fun getTotalPages_returns_null_before_any_insert() =
        runTest {
            assertThat(dao.getTotalPages(1)).isNull()
        }

    @Test
    fun getTotalPages_returns_the_value_stored_with_the_page() =
        runTest {
            dao.insertAll(makeEntities(page = 1, count = 1, totalPages = 42))

            assertThat(dao.getTotalPages(1)).isEqualTo(42)
        }

    // ── insertAll / REPLACE ───────────────────────────────────────────────────

    @Test
    fun insertAll_replaces_existing_rows_with_same_primary_key() =
        runTest {
            val staleEntities = makeEntities(page = 1, count = 2, cachedAt = now - 100_000L)
            dao.insertAll(staleEntities)

            val freshEntities = makeEntities(page = 1, count = 2, cachedAt = now)
            dao.insertAll(freshEntities)

            // Row count stays the same — IDs 1 and 2 were replaced, not duplicated.
            assertThat(dao.getByPage(1)).hasSize(2)
            assertThat(dao.getCachedAt(1)).isEqualTo(now)
        }

    // ── deleteAll ─────────────────────────────────────────────────────────────

    @Test
    fun deleteAll_clears_every_cached_character() =
        runTest {
            dao.insertAll(makeEntities(page = 1, count = 3))
            dao.insertAll(makeEntities(page = 2, count = 3, idOffset = 100))

            dao.deleteAll()

            assertThat(dao.getByPage(1)).isEmpty()
            assertThat(dao.getByPage(2)).isEmpty()
        }

    // ── deleteByPage ──────────────────────────────────────────────────────────

    @Test
    fun deleteByPage_removes_only_the_specified_page() =
        runTest {
            dao.insertAll(makeEntities(page = 1, count = 2))
            dao.insertAll(makeEntities(page = 2, count = 2, idOffset = 100))

            dao.deleteByPage(1)

            assertThat(dao.getByPage(1)).isEmpty()
            assertThat(dao.getByPage(2)).hasSize(2)
        }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeEntities(
        page: Int,
        count: Int,
        idOffset: Int = 0,
        totalPages: Int = 10,
        cachedAt: Long = now,
    ): List<CharacterEntity> =
        (1..count).map { i ->
            CharacterEntity(
                id = idOffset + i,
                name = "Character ${idOffset + i}",
                status = "Alive",
                species = "Human",
                imageUrl = "https://example.com/${idOffset + i}.jpg",
                page = page,
                totalPages = totalPages,
                cachedAt = cachedAt,
            )
        }
}
