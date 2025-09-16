package com.maintainer.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.maintainer.app.ui.vehicles.VehicleListScreen
import com.maintainer.app.ui.vehicles.AddEditVehicleScreen
import com.maintainer.app.ui.maintenance.MaintenanceListScreen
import com.maintainer.app.ui.maintenance.AddEditMaintenanceScreen
import com.maintainer.app.ui.home.HomeScreen
import com.maintainer.app.ui.settings.SettingsScreen

@Composable
fun MaintainerNavHost(
    navController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToVehicles = { navController.navigate("vehicles") },
                onNavigateToMaintenance = { vehicleId ->
                    navController.navigate("maintenance/$vehicleId")
                },
                onNavigateToEditVehicle = { vehicleId ->
                    navController.navigate("vehicles/edit/$vehicleId")
                },
                onNavigateToSettings = { navController.navigate("settings") }
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
    }
}