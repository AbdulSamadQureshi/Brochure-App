package com.bonial.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [FavouritesDao].
 *
 * These run on a real Android JVM (or emulator) against an in-memory Room
 * database so the generated SQL and Flow emissions are verified end-to-end.
 * Pure JVM unit tests cannot cover DAO behaviour because the Room annotation
 * processor generates Android-specific bytecode.
 */
@RunWith(AndroidJUnit4::class)
class FavouritesDaoTest {
    private lateinit var db: BrochuresDatabase
    private lateinit var dao: FavouritesDao

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    BrochuresDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        dao = db.favouritesDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_makes_item_appear_as_favourite() =
        runTest {
            dao.insert(FavouriteBrochureEntity("https://example.com/cover1.jpg"))

            val isFavourite = dao.isFavouriteFlow("https://example.com/cover1.jpg").first()
            assertThat(isFavourite).isTrue()
        }

    @Test
    fun isFavouriteFlow_returns_false_for_unknown_url() =
        runTest {
            val isFavourite = dao.isFavouriteFlow("https://example.com/nonexistent.jpg").first()
            assertThat(isFavourite).isFalse()
        }

    @Test
    fun delete_removes_the_favourite() =
        runTest {
            val url = "https://example.com/cover2.jpg"
            dao.insert(FavouriteBrochureEntity(url))
            dao.delete(url)

            val isFavourite = dao.isFavouriteFlow(url).first()
            assertThat(isFavourite).isFalse()
        }

    @Test
    fun delete_on_absent_url_does_not_throw() =
        runTest {
            // Should be a no-op — no exception expected.
            dao.delete("https://example.com/not-in-db.jpg")
        }

    @Test
    fun getAllCoverUrls_returns_all_inserted_urls() =
        runTest {
            dao.insert(FavouriteBrochureEntity("https://example.com/a.jpg"))
            dao.insert(FavouriteBrochureEntity("https://example.com/b.jpg"))
            dao.insert(FavouriteBrochureEntity("https://example.com/c.jpg"))

            val urls = dao.getAllCoverUrls().first()
            assertThat(urls).containsExactly(
                "https://example.com/a.jpg",
                "https://example.com/b.jpg",
                "https://example.com/c.jpg",
            )
        }

    @Test
    fun insert_with_same_url_is_ignored_due_to_conflict_strategy() =
        runTest {
            val url = "https://example.com/duplicate.jpg"
            dao.insert(FavouriteBrochureEntity(url))
            dao.insert(FavouriteBrochureEntity(url)) // IGNORE strategy — no crash

            val urls = dao.getAllCoverUrls().first()
            assertThat(urls).hasSize(1)
        }

    @Test
    fun getAllCoverUrls_emits_empty_list_when_no_favourites() =
        runTest {
            val urls = dao.getAllCoverUrls().first()
            assertThat(urls).isEmpty()
        }
}
