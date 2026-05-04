package com.example.milos_achats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.milos_achats.ui.screens.BarProductsScreen
import com.example.milos_achats.ui.screens.MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(onBarClick = { navController.navigate("bar_products") })
        }
        composable("bar_products") {
            BarProductsScreen(onBack = { navController.popBackStack() })
        }
    }
}
