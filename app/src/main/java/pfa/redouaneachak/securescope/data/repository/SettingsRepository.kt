package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.model.AppLanguage
import pfa.redouaneachak.securescope.data.model.AppTheme

interface SettingsRepository {
    val language: Flow<AppLanguage>
    val theme: Flow<AppTheme>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setTheme(theme: AppTheme)
}