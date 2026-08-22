package pfa.redouaneachak.securescope.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pfa.redouaneachak.securescope.data.model.AppTheme
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
    }

    val theme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        val stored = prefs[Keys.THEME] ?: AppTheme.LIGHT.name
        runCatching { AppTheme.valueOf(stored) }.getOrDefault(AppTheme.LIGHT)
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME] = theme.name }
    }
}