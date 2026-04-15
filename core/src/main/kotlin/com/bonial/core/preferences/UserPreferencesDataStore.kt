package com.bonial.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bonial_preferences")

/**
 * Jetpack DataStore wrapper for user preferences.
 * Provides type-safe, coroutine-based async preference access — a modern replacement for SharedPreferences.
 */
@Singleton
class UserPreferencesDataStore
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.dataStore

        val accessTokenFlow: Flow<String?> = dataStore.data.map { it[Keys.ACCESS_TOKEN] }
        val refreshTokenFlow: Flow<String?> = dataStore.data.map { it[Keys.REFRESH_TOKEN] }

        suspend fun getAccessToken(): String? = dataStore.data.first()[Keys.ACCESS_TOKEN]

        suspend fun setAccessToken(token: String) {
            dataStore.edit { it[Keys.ACCESS_TOKEN] = token }
        }

        suspend fun setRefreshToken(token: String) {
            dataStore.edit { it[Keys.REFRESH_TOKEN] = token }
        }

        suspend fun clearAll() {
            dataStore.edit { it.clear() }
        }

        private object Keys {
            val ACCESS_TOKEN = stringPreferencesKey(PreferenceKeys.KEY_ACCESS_TOKEN)
            val REFRESH_TOKEN = stringPreferencesKey(PreferenceKeys.KEY_REFRESH_TOKEN)
        }
    }
