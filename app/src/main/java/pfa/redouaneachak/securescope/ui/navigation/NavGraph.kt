package pfa.redouaneachak.securescope.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pfa.redouaneachak.securescope.ui.screens.common.PlaceholderScreen
import pfa.redouaneachak.securescope.ui.screens.datausage.DataUsageScreen
import pfa.redouaneachak.securescope.ui.screens.home.HomeScreen

@Composable
fun SecureScopeNavGraph(
    navController: NavHostController = rememberNavController(),
    onOpenMenu: () -> Unit
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                onNavigateToNetworkScan = { navController.navigate(Screen.NetworkScan.route) },
                onNavigateToDataUsage = { navController.navigate(Screen.DataUsage.route) },
                onNavigateToHardware = { navController.navigate(Screen.Hardware.route) },
                onNavigateToRecentApps = { navController.navigate(Screen.RecentApps.route) },
                onNavigateToAppList = { navController.navigate(Screen.AppList.route) },
                onOpenMenu = onOpenMenu
            )
        }

        composable(Screen.DataUsage.route) {
            DataUsageScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AppList.route) { PlaceholderScreen("Installed Apps") { navController.popBackStack() } }
        composable(Screen.Scan.route) { PlaceholderScreen("Scan Apps") { navController.popBackStack() } }
        composable(Screen.NetworkScan.route) { PlaceholderScreen("Scan Network") { navController.popBackStack() } }
        composable(Screen.Hardware.route) { PlaceholderScreen("Hardware Monitoring") { navController.popBackStack() } }
        composable(Screen.RecentApps.route) { PlaceholderScreen("Recent Apps") { navController.popBackStack() } }
        composable(Screen.Guide.route) { PlaceholderScreen("App Guide") { navController.popBackStack() } }
        composable(Screen.UserAgreement.route) { PlaceholderScreen("User Agreement") { navController.popBackStack() } }

        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            PlaceholderScreen("App Detail: $packageName") { navController.popBackStack() }
        }
    }
}