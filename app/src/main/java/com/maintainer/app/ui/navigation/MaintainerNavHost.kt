package com.maintainer.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.maintainer.app.ui.vehicles.VehicleListScreen
import com.maintainer.app.ui.vehicles.AddEditVehicleScreen
import com.maintainer.app.ui.vehicles.VehicleProfileScreen
import com.maintainer.app.ui.maintenance.MaintenanceListScreen
import com.maintainer.app.ui.maintenance.AddEditMaintenanceScreen
import com.maintainer.app.ui.garage.GarageScreen
import com.maintainer.app.ui.settings.SettingsScreen
import com.maintainer.app.ui.analytics.AnalyticsScreen

@Composable
fun MaintainerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = "garage"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("garage") {
            GarageScreen(
                onNavigateToVehicles = { navController.navigate("vehicles") },
                onNavigateToMaintenance = { vehicleId ->
                    navController.navigate("maintenance/$vehicleId")
                },
                onNavigateToEditVehicle = { vehicleId ->
                    navController.navigate("vehicles/edit/$vehicleId")
                },
                onNavigateToVehicleProfile = { vehicleId ->
                    navController.navigate("vehicles/profile/$vehicleId")
                },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToAnalytics = { navController.navigate("analytics") }
            )
        }

        composable("vehicles") {
            VehicleListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddVehicle = { navController.navigate("vehicles/add") },
                onNavigateToEditVehicle = { vehicleId ->
                    navController.navigate("vehicles/edit/$vehicleId")
                },
                onNavigateToMaintenance = { vehicleId ->
                    navController.navigate("maintenance/$vehicleId")
                }
            )
        }

        composable("vehicles/add") {
            AddEditVehicleScreen(
                onNavigateBack = { navController.popBackStack() },
                onVehicleSaved = { navController.popBackStack() }
            )
        }

        composable(
            "vehicles/edit/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            AddEditVehicleScreen(
                vehicleId = vehicleId,
                onNavigateBack = { navController.popBackStack() },
                onVehicleSaved = { navController.popBackStack() }
            )
        }

        composable(
            "vehicles/profile/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            VehicleProfileScreen(
                vehicleId = vehicleId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate("vehicles/edit/$vehicleId") },
                onNavigateToMaintenance = { navController.navigate("maintenance/$vehicleId") }
            )
        }

        composable(
            "maintenance/{vehicleId}",
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            MaintenanceListScreen(
                vehicleId = vehicleId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddMaintenance = {
                    navController.navigate("maintenance/$vehicleId/add")
                },
                onNavigateToEditMaintenance = { recordId ->
                    navController.navigate("maintenance/$vehicleId/edit/$recordId")
                }
            )
        }

        composable(
            "maintenance/{vehicleId}/add",
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            AddEditMaintenanceScreen(
                vehicleId = vehicleId,
                onNavigateBack = { navController.popBackStack() },
                onMaintenanceSaved = { navController.popBackStack() }
            )
        }

        composable(
            "maintenance/{vehicleId}/edit/{recordId}",
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("recordId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
            val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
            AddEditMaintenanceScreen(
                vehicleId = vehicleId,
                recordId = recordId,
                onNavigateBack = { navController.popBackStack() },
                onMaintenanceSaved = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("analytics") {
            AnalyticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}