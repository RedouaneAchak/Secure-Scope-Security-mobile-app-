package pfa.redouaneachak.securescope.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AppList : Screen("app_list")
    object Scan : Screen("scan")
    object NetworkScan : Screen("network_scan")
    object ServerList : Screen("server_list/{packageName}") {
        fun createRoute(packageName: String) = "server_list/$packageName"
    }
    object DataUsage : Screen("data_usage")
    object Hardware : Screen("hardware")
    object RecentApps : Screen("recent_apps")
    object Onboarding : Screen("onboarding")
    object Guide : Screen("guide")
    object UserAgreement : Screen("user_agreement")

    object AppDetail : Screen("app_detail/{packageName}") {
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
}