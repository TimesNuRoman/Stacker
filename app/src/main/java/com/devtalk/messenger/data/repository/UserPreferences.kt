package com.devtalk.messenger.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "devtalk_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_UID = stringPreferencesKey("user_uid")
        private val KEY_USERNAME = stringPreferencesKey("user_username")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("user_display_name")
    }

    val uid: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_UID]
    }

    val username: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USERNAME]
    }

    suspend fun saveUser(uid: String, username: String, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_UID] = uid
            prefs[KEY_USERNAME] = username
            prefs[KEY_DISPLAY_NAME] = displayName
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun getUidSync(): String? {
        var result: String? = null
        context.dataStore.edit { prefs ->
            result = prefs[KEY_UID]
        }
        return result
    }
}
