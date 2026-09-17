package pfa.redouaneachak.securescope.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pfa.redouaneachak.securescope.ui.screens.appdetail.AppDetailScreen
import pfa.redouaneachak.securescope.ui.screens.applist.AppListScreen
import pfa.redouaneachak.securescope.ui.screens.common.PlaceholderScreen
import pfa.redouaneachak.securescope.ui.screens.datausage.DataUsageScreen
import pfa.redouaneachak.securescope.ui.screens.hardware.HardwareScreen
import pfa.redouaneachak.securescope.ui.screens.home.HomeScreen
import pfa.redouaneachak.securescope.ui.screens.networkscan.NetworkScanScreen
import pfa.redouaneachak.securescope.ui.screens.onboarding.OnboardingScreen
import pfa.redouaneachak.securescope.ui.screens.recentapps.RecentAppsScreen
import pfa.redouaneachak.securescope.ui.screens.scan.ScanScreen
import pfa.redouaneachak.securescope.ui.screens.serverlist.ServerListScreen
import pfa.redouaneachak.securescope.ui.screens.useragreement.UserAgreementScreen

@Composable
fun SecureScopeNavGraph(
    navController: NavHostController = rememberNavController(),
    onOpenMenu: () -> Unit,
    pendingDestination: String? = null,
    onDestinationConsumed: () -> Unit = {},
    startDestinationViewModel: StartDestinationViewModel = hiltViewModel()
) {
    val startDestination by startDestinationViewModel.startDestination.collectAsStateWithLifecycle()

    LaunchedEffect(pendingDestination) {
        if (pendingDestination == "network_scan") {
            navController.navigate(Screen.NetworkScan.route)
            onDestinationConsumed()
        }
    }
    if (startDestination == null) {
        // brief loading state while checking DataStore, avoids flashing the wrong start screen
        return
    }
    NavHost(navController = navController, startDestination = startDestination!!) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                onNavigateToNetworkScan = { navController.navigate(Screen.NetworkScan.route) },
                onNavigateToDataUsage = { navController.navigate(Screen.DataUsage.route) },
                onNavigateToHardware = { navController.navigate(Screen.Hardware.route) },
                onNavigateToRecentApps = { navController.navigate(Screen.RecentApps.route) },
                onNavigateToAppList = { navController.navigate(Screen.AppList.route) },
                onNavigateToAppDetail = { packageName -> navController.navigate(Screen.AppDetail.createRoute(packageName)) },
                onOpenMenu = onOpenMenu
            )
        }

        composable(Screen.DataUsage.route) {
            DataUsageScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AppList.route) {
            AppListScreen(
                onBack = { navController.popBackStack() },
                onAppClick = { packageName -> navController.navigate(Screen.AppDetail.createRoute(packageName)) }
            )
        }
        composable(Screen.Scan.route) {
            ScanScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAppDetail = { pkg -> navController.navigate(Screen.AppDetail.createRoute(pkg)) }
            )
        }
        composable(Screen.Hardware.route) {
            HardwareScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.RecentApps.route) {
            RecentAppsScreen(
                onBack = { navController.popBackStack() },
                onAppClick = { pkg -> navController.navigate(Screen.AppDetail.createRoute(pkg)) }
            )
        }
        composable(Screen.NetworkScan.route) {
            NetworkScanScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
            )
        }

        composable(Screen.UserAgreement.route) {
            UserAgreementScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            AppDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToServerList = { pkg -> navController.navigate(Screen.ServerList.createRoute(pkg)) }
            )
        }
        composable(
            route = Screen.ServerList.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            ServerListScreen(onBack = { navController.popBackStack() })
        }
    }
}