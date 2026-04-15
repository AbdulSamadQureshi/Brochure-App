package com.bonial.data.repository

import app.cash.turbine.test
import com.bonial.data.local.FavouriteBrochureEntity
import com.bonial.data.local.FavouritesDao
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FavouritesRepositoryImplTest {
    private val dao: FavouritesDao = mock()
    private lateinit var repository: FavouritesRepositoryImpl

    @Before
    fun setUp() {
        repository = FavouritesRepositoryImpl(dao)
    }

    @Test
    fun `addFavourite inserts entity into DAO`() =
        runBlocking {
            repository.addFavourite("https://example.com/cover.jpg")
            verify(dao).insert(FavouriteBrochureEntity("https://example.com/cover.jpg"))
        }

    @Test
    fun `removeFavourite deletes from DAO`() =
        runBlocking {
            repository.removeFavourite("https://example.com/cover.jpg")
            verify(dao).delete("https://example.com/cover.jpg")
        }

    @Test
    fun `isFavouriteFlow emits true when DAO returns true`() =
        runBlocking {
            whenever(dao.isFavouriteFlow("url")).thenReturn(flowOf(true))

            repository.isFavouriteFlow("url").test {
                assertThat(awaitItem()).isTrue()
                awaitComplete()
            }
        }

    @Test
    fun `getFavouriteCoverUrls emits set of URLs from DAO`() =
        runBlocking {
            whenever(dao.getAllCoverUrls()).thenReturn(flowOf(listOf("url1", "url2")))

            repository.getFavouriteCoverUrls().test {
                assertThat(awaitItem()).containsExactly("url1", "url2")
                awaitComplete()
            }
        }
}
