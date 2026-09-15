package pfa.redouaneachak.securescope.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import pfa.redouaneachak.securescope.data.model.BlockedDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockStatsHolder @Inject constructor() {
    private val _blockedDomains = MutableStateFlow<List<BlockedDomain>>(emptyList())
    val blockedDomains: StateFlow<List<BlockedDomain>> = _blockedDomains.asStateFlow()

    val blockedCount: kotlinx.coroutines.flow.Flow<Int> = _blockedDomains.map { it.size }

    fun recordBlocked(domain: String, category: String) {
        _blockedDomains.update { current -> listOf(BlockedDomain(domain, category, System.currentTimeMillis())) + current }
    }
}