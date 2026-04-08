package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hrach.financeapp.R
import com.hrach.financeapp.navigation.Screen
import com.hrach.financeapp.viewmodel.HomeViewModel

@Composable
fun AppRoot(viewModel: HomeViewModel) {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.Transactions,
        Screen.Accounts
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    val icon = when (screen) {
                        Screen.Home -> Icons.Filled.Home
                        Screen.Transactions -> Icons.AutoMirrored.Filled.List
                        Screen.Accounts -> Icons.Filled.AccountBalanceWallet
                        else -> Icons.Filled.Home
                    }

                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(screen.title)
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute != Screen.AddTransaction.route) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clickable { navController.navigate(Screen.AddTransaction.route) }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_add_bg),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    paddingValues = paddingValues,
                    onOpenCategories = { navController.navigate(Screen.Categories.route) }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(viewModel, paddingValues)
            }

            composable(Screen.Accounts.route) {
                AccountsScreen(viewModel, paddingValues)
            }

            composable(Screen.Categories.route) {
                CategoriesScreen(
                    viewModel = viewModel,
                    paddingValues = paddingValues,
                    onBack = { navController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(viewModel, paddingValues) {
                    navController.popBackStack()
                }
            }
        }
    }
}