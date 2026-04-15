package com.bonial.data.repository

import app.cash.turbine.test
import com.bonial.data.remote.model.CharacterDto
import com.bonial.data.remote.model.CharacterResponseDto
import com.bonial.data.remote.model.PageInfoDto
import com.bonial.data.remote.service.CharactersApiService
import com.bonial.domain.model.network.response.Request
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Covers the behaviour that makes the repository more than a type alias:
 *  - DTO → domain mapping is applied on Success
 *  - Loading/Error states survive the mapping step (via `mapSuccess`)
 *  - Transport failures are surfaced as `Request.Error` rather than thrown
 *  - The `name` filter param is forwarded to the API service
 */
class CharactersRepositoryImplTest {
    private val apiService: CharactersApiService = mock()
    private lateinit var repository: CharactersRepositoryImpl

    @Before
    fun setUp() {
        repository = CharactersRepositoryImpl(apiService)
    }

    @Test
    fun `characters maps DTO response to domain CharactersPage on success`() =
        runBlocking {
            val response =
                CharacterResponseDto(
                    info = PageInfoDto(pages = 3),
                    results =
                        listOf(
                            CharacterDto(id = 1, name = "Rick"),
                            CharacterDto(id = 2, name = "Morty"),
                        ),
                )
            whenever(apiService.characters(1)).thenReturn(response)

            repository.characters(page = 1).test {
                assertThat(awaitItem()).isInstanceOf(Request.Loading::class.java)
                val success = awaitItem() as Request.Success
                assertThat(success.data.totalPages).isEqualTo(3)
                assertThat(success.data.characters.map { it.name }).containsExactly("Rick", "Morty")
                awaitComplete()
            }
        }

    @Test
    fun `characters forwards name filter to api service`() =
        runBlocking {
            val response =
                CharacterResponseDto(
                    info = PageInfoDto(pages = 1),
                    results = listOf(CharacterDto(id = 1, name = "Rick Sanchez")),
                )
            whenever(apiService.characters(page = 1, name = "Rick")).thenReturn(response)

            repository.characters(page = 1, name = "Rick").test {
                assertThat(awaitItem()).isInstanceOf(Request.Loading::class.java)
                val success = awaitItem() as Request.Success
                assertThat(
                    success.data.characters
                        .single()
                        .name,
                ).isEqualTo("Rick Sanchez")
                awaitComplete()
            }
        }

    @Test
    fun `characters with null name returns unfiltered results`() =
        runBlocking {
            val response =
                CharacterResponseDto(
                    info = PageInfoDto(pages = 2),
                    results =
                        listOf(
                            CharacterDto(id = 1, name = "Rick"),
                            CharacterDto(id = 2, name = "Morty"),
                        ),
                )
            whenever(apiService.characters(page = 1, name = null)).thenReturn(response)

            repository.characters(page = 1, name = null).test {
                assertThat(awaitItem()).isInstanceOf(Request.Loading::class.java)
                val success = awaitItem() as Request.Success
                assertThat(success.data.characters).hasSize(2)
                awaitComplete()
            }
        }

    @Test
    fun `characters emits Error when api throws IOException`() =
        runBlocking {
            apiService.stub { on { characters(1) } doAnswer { throw IOException("offline") } }

            repository.characters(page = 1).test {
                assertThat(awaitItem()).isInstanceOf(Request.Loading::class.java)
                val error = awaitItem() as Request.Error
                assertThat(error.apiError?.code).isEqualTo("NetworkError")
                awaitComplete()
            }
        }

    @Test
    fun `character maps DTO to domain CharacterDetail on success`() =
        runBlocking {
            whenever(apiService.character(7)).thenReturn(
                CharacterDto(id = 7, name = "Summer", species = "Human"),
            )

            repository.character(id = 7).test {
                assertThat(awaitItem()).isInstanceOf(Request.Loading::class.java)
                val success = awaitItem() as Request.Success
                assertThat(success.data.id).isEqualTo(7)
                assertThat(success.data.name).isEqualTo("Summer")
                assertThat(success.data.species).isEqualTo("Human")
                awaitComplete()
            }
        }

    @Test
    fun `character emits Error with http code when api throws HttpException`() =
        runBlocking {
            val httpResponse = Response.error<CharacterDto>(404, "".toResponseBody())
            apiService.stub { on { character(99) } doAnswer { throw HttpException(httpResponse) } }

            repository.character(id = 99).test {
                assertThat(awaitItem()).isInstanceOf(Request.Loading::class.java)
                val error = awaitItem() as Request.Error
                assertThat(error.apiError?.code).isEqualTo("404")
                awaitComplete()
            }
        }
}
