package pfa.redouaneachak.securescope.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.ui.navigation.SecureScopeNavGraph
import pfa.redouaneachak.securescope.ui.screens.settings.SettingsScreen

@Composable
fun SecureScopeRoot() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.8f)) {
                SettingsScreen(
                    onNavigateToGuide = { scope.launch { drawerState.close() } },
                    onNavigateToUserAgreement = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        SecureScopeNavGraph(
            onOpenMenu = { scope.launch { drawerState.open() } }
        )
    }
}