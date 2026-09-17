package pfa.redouaneachak.securescope.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_prefs")

class OnboardingStateHolder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_COMPLETED = booleanPreferencesKey("onboarding_completed")

    val hasCompletedOnboarding: Flow<Boolean> = context.onboardingDataStore.data.map { it[KEY_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted() {
        context.onboardingDataStore.edit { it[KEY_COMPLETED] = true }
    }
}