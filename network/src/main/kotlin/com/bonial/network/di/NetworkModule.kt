package com.bonial.network.di

import com.bonial.core.preferences.UserPreferencesDataStore
import com.bonial.network.BuildConfig
import com.bonial.network.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(userPreferencesDataStore: UserPreferencesDataStore): Retrofit =
        RetrofitClient(
            baseUrl = BuildConfig.BASE_URL,
            enableLogging = BuildConfig.DEBUG,
            userPreferencesDataStore = userPreferencesDataStore,
        ).retrofit
}
