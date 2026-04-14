package com.bonial.network

import com.bonial.core.preferences.UserPreferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(
    private val baseUrl: String,
    private val enableLogging: Boolean,
    userPreferencesDataStore: UserPreferencesDataStore,
) {
    // Mirror the current access token in memory so the OkHttp interceptor can read it
    // synchronously without blocking. The DataStore flow remains the source of truth —
    // this cache updates whenever the stored token changes. This avoids runBlocking in
    // the interceptor chain, which risks deadlock if the interceptor is ever invoked
    // on a dispatcher that owns the DataStore actor (e.g. tests, mock web servers).
    @Volatile
    private var cachedAccessToken: String? = null

    private val tokenScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        userPreferencesDataStore.accessTokenFlow
            .onEach { cachedAccessToken = it }
            .launchIn(tokenScope)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (enableLogging) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Platform", "Android")

                cachedAccessToken?.takeIf { it.isNotEmpty() }?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }
}
