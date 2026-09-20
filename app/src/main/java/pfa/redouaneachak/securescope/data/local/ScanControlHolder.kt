package pfa.redouaneachak.securescope.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanControlHolder @Inject constructor() {
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    fun pause() { _isPaused.value = true }
    fun resume() { _isPaused.value = false }
    fun togglePause() { _isPaused.value = !_isPaused.value }
    fun reset() { _isPaused.value = false }
}