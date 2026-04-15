package com.bonial.data.repository

import com.bonial.core.preferences.UserPreferencesDataStore
import com.bonial.domain.repository.LocalStorageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageRepositoryImpl
    @Inject
    constructor(
        private val userPreferencesDataStore: UserPreferencesDataStore,
    ) : LocalStorageRepository {
        override suspend fun clearData() {
            userPreferencesDataStore.clearAll()
        }
    }
