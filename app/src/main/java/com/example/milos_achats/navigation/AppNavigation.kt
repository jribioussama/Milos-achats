package com.example.milos_achats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.milos_achats.ui.screens.BarProductsScreen
import com.example.milos_achats.ui.screens.CatalogAdminScreen
import com.example.milos_achats.ui.screens.KitchenProductsScreen
import com.example.milos_achats.ui.screens.MainScreen
import com.example.milos_achats.ui.screens.ManagerScreen
import com.example.milos_achats.ui.screens.ServerProductsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onBarClick     = { navController.navigate("bar_products") },
                onKitchenClick = { navController.navigate("kitchen_products") },
                onServerClick  = { navController.navigate("server_products") },
                onManagerClick = { navController.navigate("manager") },
            )
        }
        composable("bar_products") {
            BarProductsScreen(onBack = { navController.popBackStack() })
        }
        composable("manager") {
            ManagerScreen(
                onBack              = { navController.popBackStack() },
                onAdminBarClick     = { navController.navigate("admin_bar") },
                onAdminKitchenClick = { navController.navigate("admin_cuisine") },
                onAdminServerClick  = { navController.navigate("admin_server") },
            )
        }
        composable("kitchen_products") {
            KitchenProductsScreen(onBack = { navController.popBackStack() })
        }
        composable("server_products") {
            ServerProductsScreen(onBack = { navController.popBackStack() })
        }
        composable("admin_bar") {
            CatalogAdminScreen(category = "bar", onBack = { navController.popBackStack() })
        }
        composable("admin_cuisine") {
            CatalogAdminScreen(category = "cuisine", onBack = { navController.popBackStack() })
        }
        composable("admin_server") {
            CatalogAdminScreen(category = "server", onBack = { navController.popBackStack() })
        }
    }
}
