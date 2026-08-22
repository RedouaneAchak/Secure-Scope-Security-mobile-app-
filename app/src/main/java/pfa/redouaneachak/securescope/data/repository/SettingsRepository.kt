package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.model.AppTheme

interface SettingsRepository {
    val theme: Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
}