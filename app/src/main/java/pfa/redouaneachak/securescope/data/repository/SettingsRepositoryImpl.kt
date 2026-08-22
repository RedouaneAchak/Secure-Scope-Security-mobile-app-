package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.local.datastore.UserPreferencesDataStore
import pfa.redouaneachak.securescope.data.model.AppTheme
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : SettingsRepository {

    override val theme: Flow<AppTheme> = dataStore.theme

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.setTheme(theme)
    }
}