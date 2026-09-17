package pfa.redouaneachak.securescope.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.local.OnboardingStateHolder
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingStateHolder: OnboardingStateHolder
) : ViewModel() {

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            onboardingStateHolder.setOnboardingCompleted()
            onDone()
        }
    }
}