package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.YumasteViewModel

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Accedi")
    object Register : Screen("register", "Registrati")
    object Catalog : Screen("catalog", "Menù")
    object Detail : Screen("detail/{boxId}", "Dettagli") {
        fun createRoute(boxId: Int) = "detail/$boxId"
    }
    object Cart : Screen("cart", "Carrello")
    object Orders : Screen("orders", "Ordini")
    object Profile : Screen("profile", "Profilo")
}

data class BottomNavItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YumasteNavigation(viewModel: YumasteViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Catalog, Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu, "nav_catalog"),
        BottomNavItem(
            Screen.Cart,
            Icons.Filled.ShoppingCart,
            Icons.Outlined.ShoppingCart,
            "nav_cart"
        ),
        BottomNavItem(Screen.Orders, Icons.Filled.History, Icons.Outlined.History, "nav_orders"),
        BottomNavItem(Screen.Profile, Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
    )

    // Screens that shouldn't display bottom navigation (like login/register)
    val showBottomBar = currentRoute in listOf(
        Screen.Catalog.route,
        Screen.Detail.route,
        Screen.Cart.route,
        Screen.Orders.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_bar"),
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.screen.route || 
                                (item.screen == Screen.Catalog && currentRoute?.startsWith("detail") == true)
                        
                        NavigationBarItem(
                            modifier = Modifier.testTag(item.tag),
                            selected = isSelected,
                            onClick = {
                                if (item.screen == Screen.Catalog || isLoggedIn) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    // Guest user clicked profile/cart/orders -> go to Login
                                    navController.navigate(Screen.Login.route) {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.screen == Screen.Cart && cartItems.isNotEmpty()) {
                                            val itemCount = cartItems.sumOf { it.quantita }
                                            Badge {
                                                Text(itemCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.screen.title
                                    )
                                }
                            },
                            label = { Text(item.screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Catalog.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegistrazioneScreen(
                    viewModel = viewModel,
                    onRegistrationSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Catalog.route) {
                CatalogScreen(
                    viewModel = viewModel,
                    onNavigateToBoxDetail = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("boxId") { type = NavType.IntType })
            ) { backStackEntry ->
                val boxId = backStackEntry.arguments?.getInt("boxId") ?: 0
                DettaglioBoxScreen(
                    boxId = boxId,
                    viewModel = viewModel,
                    onBack = { navController.navigateUp() },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Cart.route) {
                CarrelloScreen(
                    viewModel = viewModel,
                    onNavigateToCatalog = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    }
                )
            }

            composable(Screen.Orders.route) {
                OrdiniScreen(
                    viewModel = viewModel,
                    onBack = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfiloScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}
