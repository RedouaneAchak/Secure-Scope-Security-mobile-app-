package pfa.redouaneachak.securescope.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.local.OnboardingStateHolder
import javax.inject.Inject

@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    onboardingStateHolder: OnboardingStateHolder
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val completed = onboardingStateHolder.hasCompletedOnboarding.first()
            _startDestination.value = if (completed) Screen.Home.route else Screen.Onboarding.route
        }
    }
}