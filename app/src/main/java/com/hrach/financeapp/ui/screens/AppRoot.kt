package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hrach.financeapp.R
import com.hrach.financeapp.navigation.Screen
import com.hrach.financeapp.ui.screens.auth.AuthGateScreen
import com.hrach.financeapp.viewmodel.AuthState
import com.hrach.financeapp.viewmodel.HomeViewModel
import com.hrach.financeapp.viewmodel.SessionViewModel

@Composable
fun AppRoot(
    sessionViewModel: SessionViewModel,
    homeViewModel: HomeViewModel
) {
    val authState by sessionViewModel.authState.collectAsStateWithLifecycle()
    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()
    val sessionExpired by homeViewModel.sessionExpired.collectAsStateWithLifecycle()

    LaunchedEffect(authState, currentUser?.id) {
        if (authState is AuthState.Authenticated && currentUser != null) {
            homeViewModel.onAuthenticated(currentUser!!.id)
        }
        if (authState is AuthState.Offline) {
            homeViewModel.onOfflineMode()
        }
        if (authState is AuthState.Unauthenticated) {
            homeViewModel.onLoggedOut()
        }
    }

    LaunchedEffect(sessionExpired) {
        if (sessionExpired) {
            homeViewModel.clearSessionExpired()
            sessionViewModel.logout()
        }
    }

    when (authState) {
        AuthState.Checking -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        AuthState.Unauthenticated -> {
            AuthGateScreen(sessionViewModel)
        }

        AuthState.Authenticated, AuthState.Offline -> {
            MainAppScaffold(
                sessionViewModel = sessionViewModel,
                homeViewModel = homeViewModel
            )
        }
    }
}

@Composable
private fun MainAppScaffold(
    sessionViewModel: SessionViewModel,
    homeViewModel: HomeViewModel
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Transactions,
        Screen.Analytics,
        Screen.Accounts,
        Screen.Members
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedGroupId by homeViewModel.selectedGroupId.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(selectedGroupId) {
        if (selectedGroupId != null) {
            homeViewModel.refreshAll()
            homeViewModel.startPolling()
        } else {
            homeViewModel.stopPolling()
        }
    }

    DisposableEffect(lifecycleOwner, selectedGroupId) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (selectedGroupId != null) {
                        homeViewModel.refreshAll()
                        homeViewModel.startPolling()
                    }
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    homeViewModel.stopPolling()
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            homeViewModel.stopPolling()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    val icon = when (screen) {
                        Screen.Home -> Icons.Filled.Home
                        Screen.Transactions -> Icons.AutoMirrored.Filled.List
                        Screen.Analytics -> Icons.Filled.PieChart
                        Screen.Accounts -> Icons.Filled.AccountBalanceWallet
                        Screen.Members -> Icons.Filled.Group
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
                        icon = { Icon(imageVector = icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute != Screen.AddTransaction.route && selectedGroupId != null) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clickable { navController.navigate(Screen.AddTransaction.route) }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_add_bg),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    sessionViewModel = sessionViewModel,
                    paddingValues = paddingValues,
                    onOpenCategories = { navController.navigate(Screen.Categories.route) },
                    onOpenMembers = { navController.navigate(Screen.Members.route) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(homeViewModel, paddingValues)
            }
            composable(Screen.Analytics.route) {
                GraphAnalysisScreen(homeViewModel, paddingValues)
            }
            composable(Screen.Accounts.route) {
                AccountsScreen(homeViewModel, paddingValues)
            }
            composable(Screen.Members.route) {
                GroupMembersScreen(homeViewModel, paddingValues) {
                    navController.popBackStack()
                }
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    viewModel = homeViewModel,
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(homeViewModel, paddingValues, onSaved = {
                    navController.popBackStack()
                }, onBack = {
                    navController.popBackStack()
                })
            }
        }
    }
}
