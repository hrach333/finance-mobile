package com.hrach.financeapp.mvp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.financeapp.data.auth.SessionStore
import com.hrach.financeapp.data.repository.AuthRepository
import com.hrach.financeapp.data.repository.DemoFinanceOverviewRepository
import com.hrach.financeapp.data.repository.FinanceOverviewRepository
import com.hrach.financeapp.ui.screens.AccountsOverviewScreen
import com.hrach.financeapp.ui.screens.AnalyticsOverviewScreen
import com.hrach.financeapp.ui.screens.CategoriesOverviewScreen
import com.hrach.financeapp.ui.screens.HomeOverviewScreen
import com.hrach.financeapp.ui.screens.TransactionsOverviewScreen
import com.hrach.financeapp.ui.state.AuthResult
import com.hrach.financeapp.ui.state.AuthSessionCoordinator
import com.hrach.financeapp.ui.state.DashboardTab
import com.hrach.financeapp.ui.state.FinanceDashboardController
import com.hrach.financeapp.ui.state.FinanceDashboardEvent
import com.hrach.financeapp.ui.state.FinanceDashboardState
import kotlinx.coroutines.launch

private val backgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFCCCFDF), Color(0xFFEFD6EF), Color(0xFFABA7CE))
)


@Composable
fun App(
    repository: FinanceOverviewRepository = DemoFinanceOverviewRepository(),
    authRepository: AuthRepository? = null,
    sessionStore: SessionStore? = null,
    repositoryFactory: (((() -> String?) -> FinanceOverviewRepository))? = null
) {
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF5E4B8B),
            primaryVariant = Color(0xFF4C5E8B),
            secondary = Color(0xFF16A34A),
            background = Color(0xFFEFD6EF),
            surface = Color(0xFFF9F6FC),
            error = Color(0xFFE85B6A)
        )
    ) {
        if (authRepository != null && sessionStore != null && repositoryFactory != null) {
            AuthenticatedApp(
                authRepository = authRepository,
                sessionStore = sessionStore,
                repositoryFactory = repositoryFactory
            )
            return@MaterialTheme
        }

        FinanceOverviewApp(repository = repository, onLogout = null)
    }
}

@Composable
private fun AuthenticatedApp(
    authRepository: AuthRepository,
    sessionStore: SessionStore,
    repositoryFactory: (() -> String?) -> FinanceOverviewRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val authSession = remember(authRepository, sessionStore, repositoryFactory) {
        AuthSessionCoordinator(
            authRepository = authRepository,
            sessionStore = sessionStore,
            repositoryFactory = repositoryFactory
        )
    }
    var sessionLoaded by remember(sessionStore) { mutableStateOf(false) }
    var token by remember(sessionStore) { mutableStateOf<String?>(null) }

    LaunchedEffect(authSession) {
        token = authSession.restoreToken()
        sessionLoaded = true
    }

    if (!sessionLoaded) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Box(modifier = Modifier.fillMaxSize().background(backgroundGradient).padding(24.dp)) {
                LoadingDashboard(state = FinanceDashboardState())
            }
        }
        return
    }

    val activeToken = token
    if (activeToken.isNullOrBlank()) {
        AuthScreen(
            authSession = authSession,
            onAuthenticated = { authToken ->
                token = authToken
            }
        )
        return
    }

    val overviewRepository = remember(activeToken, authSession) {
        authSession.createOverviewRepository { token }
    }
    FinanceOverviewApp(
        repository = overviewRepository,
        onLogout = {
            coroutineScope.launch {
                authSession.logout(token)
                token = null
            }
        },
        onAuthExpired = {
            coroutineScope.launch {
                authSession.clearToken()
                token = null
            }
        }
    )
}

@Composable
private fun FinanceOverviewApp(
    repository: FinanceOverviewRepository,
    onLogout: (() -> Unit)?,
    onAuthExpired: (() -> Unit)? = null
) {
        val dashboardController = remember(repository) { FinanceDashboardController(repository) }
        val coroutineScope = rememberCoroutineScope()
        var dashboardState by remember(dashboardController) {
            mutableStateOf(dashboardController.state)
        }
        fun applyDashboardEvent(event: FinanceDashboardEvent) {
            dashboardState = dashboardController.state
            if (event == FinanceDashboardEvent.AuthExpired) {
                onAuthExpired?.invoke()
            }
        }

        LaunchedEffect(repository) {
            dashboardState = dashboardController.markLoading()
            applyDashboardEvent(dashboardController.refresh())
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            ResponsiveShell(
                selectedTab = dashboardState.selectedTab,
                onTabSelected = { tab ->
                    dashboardState = dashboardController.selectTab(tab)
                }
            ) {
                val loadedOverview = dashboardState.overview
                if (loadedOverview == null) {
                    LoadingDashboard(state = dashboardState)
                } else {
                    when (dashboardState.selectedTab) {
                        DashboardTab.Home -> HomeOverviewScreen(loadedOverview, onLogout)
                        DashboardTab.Transactions -> TransactionsOverviewScreen(
                            overview = loadedOverview,
                            onCreateTransaction = { type, amount, accountId, categoryId, date, comment ->
                                coroutineScope.launch {
                                    applyDashboardEvent(
                                        dashboardController.createTransaction(type, amount, accountId, categoryId, date, comment)
                                    )
                                }
                            },
                            onUpdateTransaction = { transaction, type, amount, accountId, categoryId, date, comment ->
                                coroutineScope.launch {
                                    applyDashboardEvent(
                                        dashboardController.updateTransaction(transaction, type, amount, accountId, categoryId, date, comment)
                                    )
                                }
                            },
                            onDeleteTransaction = { transaction ->
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.deleteTransaction(transaction))
                                }
                            }
                        )
                        DashboardTab.Accounts -> AccountsOverviewScreen(
                            overview = loadedOverview,
                            onCreateAccount = { name, type, balance ->
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.createAccount(name, type, balance))
                                }
                            },
                            onUpdateAccount = { account, name, type, balance ->
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.updateAccount(account, name, type, balance))
                                }
                            },
                            onDeleteAccount = { account ->
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.deleteAccount(account))
                                }
                            }
                        )
                        DashboardTab.Categories -> CategoriesOverviewScreen(
                            overview = loadedOverview,
                            onCreateCategory = { name, type, iconKey ->
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.createCategory(name, type, iconKey))
                                }
                            },
                            onUpdateCategory = { category, name, type, iconKey ->
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.updateCategory(category, name, type, iconKey))
                                }
                            },
                            onDeleteCategory = { category ->
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.deleteCategory(category))
                                }
                            }
                        )
                        DashboardTab.Analytics -> AnalyticsOverviewScreen(loadedOverview)
                    }
                }
            }
        }
}

@Composable
private fun AuthScreen(
    authSession: AuthSessionCoordinator,
    onAuthenticated: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
            val widthModifier = if (maxWidth >= 760.dp) {
                Modifier.width(430.dp)
            } else {
                Modifier.fillMaxWidth()
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color(0xFFF9F6FC),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                elevation = 6.dp,
                modifier = widthModifier
                    .align(Alignment.Center)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Регистрация" else "Вход",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF23212B)
                    )
                    Text(
                        text = "SmartBudget",
                        style = MaterialTheme.typography.body2,
                        color = Color(0xFF6B6579)
                    )

                    if (isRegisterMode) {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Имя") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    error?.let {
                        Text(text = it, color = Color(0xFFE85B6A), style = MaterialTheme.typography.body2)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                error = null
                                when (val result =
                                    if (isRegisterMode) {
                                        authSession.register(name = name, email = email, password = password)
                                    } else {
                                        authSession.login(email = email, password = password)
                                    }
                                ) {
                                    is AuthResult.Failure -> {
                                        error = result.message
                                    }
                                    is AuthResult.Success -> {
                                        onAuthenticated(result.token)
                                    }
                                }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && (!isRegisterMode || name.isNotBlank()),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5E4B8B), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isLoading) "Подождите..." else if (isRegisterMode) "Создать аккаунт" else "Войти")
                    }

                    Button(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            error = null
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF1E7FB), contentColor = Color(0xFF5E4B8B)),
                        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRegisterMode) "У меня уже есть аккаунт" else "Создать новый аккаунт")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingDashboard(state: FinanceDashboardState) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HeaderBlock(
            title = "SmartBudget",
            subtitle = if (state.isLoading) "Загрузка данных" else "Данные недоступны"
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xFFF9F6FC),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.errorMessage ?: "Подключаем финансовые данные...",
                modifier = Modifier.padding(18.dp),
                color = if (state.errorMessage == null) Color(0xFF2F2B3A) else Color(0xFFE85B6A)
            )
        }
    }
}

@Composable
private fun ResponsiveShell(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        val useRail = maxWidth >= 760.dp

        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                DesktopRail(selectedTab = selectedTab, onTabSelected = onTabSelected)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(0.72f)) {
                        content()
                    }
                }
            }
        } else {
            Scaffold(
                backgroundColor = Color.Transparent,
                bottomBar = {
                    BottomNavigation(backgroundColor = Color(0xFFF9F6FC), elevation = 10.dp) {
                        DashboardTab.entries.forEach { tab ->
                            BottomNavigationItem(
                                selected = selectedTab == tab,
                                onClick = { onTabSelected(tab) },
                                icon = { NavigationGlyph(tab, selectedTab == tab) },
                                label = { Text(tab.title, fontSize = 11.sp) },
                                selectedContentColor = Color(0xFF5E4B8B),
                                unselectedContentColor = Color(0xFF6B6579)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DesktopRail(selectedTab: DashboardTab, onTabSelected: (DashboardTab) -> Unit) {
    Column(
        modifier = Modifier
            .width(206.dp)
            .fillMaxHeight()
            .background(Color(0xFFF9F6FC).copy(alpha = 0.78f))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "SmartBudget",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF23212B)
        )
        Text(
            text = "KMP preview",
            color = Color(0xFF6B6579),
            style = MaterialTheme.typography.body2
        )
        Spacer(modifier = Modifier.height(12.dp))
        DashboardTab.entries.forEach { tab ->
            RailItem(tab = tab, selected = selectedTab == tab, onClick = { onTabSelected(tab) })
        }
    }
}

@Composable
private fun RailItem(tab: DashboardTab, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) Color(0xFFF1E7FB) else Color.Transparent
    val textColor = if (selected) Color(0xFF5E4B8B) else Color(0xFF4B4760)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavigationGlyph(tab = tab, selected = selected)
        Text(tab.title, color = textColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun NavigationGlyph(tab: DashboardTab, selected: Boolean) {
    val background = if (selected) Color(0xFF5E4B8B) else Color(0xFFE8E1F0)
    val content = if (selected) Color.White else Color(0xFF6B6579)

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(tab.glyph, color = content, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun HeaderBlock(title: String, subtitle: String) {
    HeaderBlock(title = title, subtitle = subtitle, onLogout = null)
}

@Composable
private fun HeaderBlock(title: String, subtitle: String, onLogout: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF23212B)
            )
            Text(text = subtitle, color = Color(0xFF4B4760), style = MaterialTheme.typography.body2)
        }
        Button(
            onClick = { onLogout?.invoke() },
            enabled = onLogout != null,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF9F6FC), contentColor = Color(0xFF5E4B8B)),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Text("Выйти")
        }
    }
}
