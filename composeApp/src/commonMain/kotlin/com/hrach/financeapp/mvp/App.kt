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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.financeapp.data.auth.SessionStore
import com.hrach.financeapp.data.repository.AuthRepository
import com.hrach.financeapp.data.repository.DemoFinanceOverviewRepository
import com.hrach.financeapp.data.repository.FinanceOverviewRepository
import com.hrach.financeapp.ui.screens.AppBackgroundGradient
import com.hrach.financeapp.ui.screens.AppBlue
import com.hrach.financeapp.ui.screens.AppCard
import com.hrach.financeapp.ui.screens.AppInk
import com.hrach.financeapp.ui.screens.AppLilac
import com.hrach.financeapp.ui.screens.AppMuted
import com.hrach.financeapp.ui.screens.AppPurple
import com.hrach.financeapp.ui.screens.AccountsOverviewScreen
import com.hrach.financeapp.ui.screens.AnalyticsOverviewScreen
import com.hrach.financeapp.ui.screens.CategoriesOverviewScreen
import com.hrach.financeapp.ui.screens.FinanceIcon
import com.hrach.financeapp.ui.screens.GroupMembersOverviewScreen
import com.hrach.financeapp.ui.screens.GroupsOverviewScreen
import com.hrach.financeapp.ui.screens.HomeOverviewScreen
import com.hrach.financeapp.ui.screens.RoundIconButton
import com.hrach.financeapp.ui.screens.TransactionsOverviewScreen
import com.hrach.financeapp.ui.state.AuthActionResult
import com.hrach.financeapp.ui.state.AuthResult
import com.hrach.financeapp.ui.state.AuthSessionCoordinator
import com.hrach.financeapp.ui.state.DashboardTab
import com.hrach.financeapp.ui.state.FinanceDashboardController
import com.hrach.financeapp.ui.state.FinanceDashboardEvent
import com.hrach.financeapp.ui.state.FinanceDashboardState
import kotlinx.coroutines.launch

private val backgroundGradient = AppBackgroundGradient


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
                        DashboardTab.Home -> HomeOverviewScreen(
                            overview = loadedOverview,
                            onLogout = onLogout,
                            onOpenMembers = {
                                dashboardState = dashboardController.selectTab(DashboardTab.Members)
                            },
                            onOpenCategories = {
                                dashboardState = dashboardController.selectTab(DashboardTab.Categories)
                            },
                            onSelectGroup = { group ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewSelectGroup(group)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.selectGroup(group, rollbackState))
                                }
                            },
                            onOpenGroups = {
                                dashboardState = dashboardController.selectTab(DashboardTab.Groups)
                            }
                        )
                        DashboardTab.Transactions -> TransactionsOverviewScreen(
                            overview = loadedOverview,
                            onCreateTransaction = { type, amount, accountId, categoryId, date, comment ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewCreateTransaction(type, amount, accountId, categoryId, date, comment)
                                coroutineScope.launch {
                                    applyDashboardEvent(
                                        dashboardController.createTransaction(type, amount, accountId, categoryId, date, comment, rollbackState)
                                    )
                                }
                            },
                            onUpdateTransaction = { transaction, type, amount, accountId, categoryId, date, comment ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewUpdateTransaction(transaction, type, amount, accountId, categoryId, date, comment)
                                coroutineScope.launch {
                                    applyDashboardEvent(
                                        dashboardController.updateTransaction(transaction, type, amount, accountId, categoryId, date, comment, rollbackState)
                                    )
                                }
                            },
                            onDeleteTransaction = { transaction ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewDeleteTransaction(transaction)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.deleteTransaction(transaction, rollbackState))
                                }
                            }
                        )
                        DashboardTab.Accounts -> AccountsOverviewScreen(
                            overview = loadedOverview,
                            onCreateAccount = { name, type, balance ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewCreateAccount(name, type, balance)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.createAccount(name, type, balance, rollbackState))
                                }
                            },
                            onUpdateAccount = { account, name, type, balance ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewUpdateAccount(account, name, type, balance)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.updateAccount(account, name, type, balance, rollbackState))
                                }
                            },
                            onDeleteAccount = { account ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewDeleteAccount(account)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.deleteAccount(account, rollbackState))
                                }
                            }
                        )
                        DashboardTab.Categories -> CategoriesOverviewScreen(
                            overview = loadedOverview,
                            onCreateCategory = { name, type, iconKey ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewCreateCategory(name, type, iconKey)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.createCategory(name, type, iconKey, rollbackState))
                                }
                            },
                            onUpdateCategory = { category, name, type, iconKey ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewUpdateCategory(category, name, type, iconKey)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.updateCategory(category, name, type, iconKey, rollbackState))
                                }
                            },
                            onDeleteCategory = { category ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewDeleteCategory(category)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.deleteCategory(category, rollbackState))
                                }
                            }
                        )
                        DashboardTab.Groups -> GroupsOverviewScreen(
                            overview = loadedOverview,
                            onBack = {
                                dashboardState = dashboardController.selectTab(DashboardTab.Home)
                            },
                            onCreateGroup = { name, currency ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewCreateGroup(name, currency)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.createGroup(name, currency, rollbackState))
                                }
                            },
                            onUpdateGroup = { group, name, currency ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewUpdateGroup(group, name, currency)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.updateGroup(group, name, currency, rollbackState))
                                }
                            },
                            onSelectGroup = { group ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewSelectGroup(group)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.selectGroup(group, rollbackState))
                                }
                            }
                        )
                        DashboardTab.Members -> GroupMembersOverviewScreen(
                            overview = loadedOverview,
                            onAddMember = { email, role ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewAddGroupMember(email, role)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.addGroupMember(email, role, rollbackState))
                                }
                            },
                            onUpdateMemberRole = { member, role ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewUpdateGroupMemberRole(member, role)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.updateGroupMemberRole(member, role, rollbackState))
                                }
                            },
                            onDeleteMember = { member ->
                                val rollbackState = dashboardController.state
                                dashboardState = dashboardController.previewDeleteGroupMember(member)
                                coroutineScope.launch {
                                    applyDashboardEvent(dashboardController.deleteGroupMember(member, rollbackState))
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
    var authMode by remember { mutableStateOf(AuthMode.Login) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var resetCode by remember { mutableStateOf("") }
    var codeRequested by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    val isRegisterMode = authMode == AuthMode.Register
    val isResetMode = authMode == AuthMode.ForgotPassword

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
                        text = when (authMode) {
                            AuthMode.Login -> "Вход"
                            AuthMode.Register -> "Регистрация"
                            AuthMode.ForgotPassword -> "Восстановление пароля"
                        },
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

                    if (isResetMode && codeRequested) {
                        TextField(
                            value = resetCode,
                            onValueChange = { resetCode = it },
                            label = { Text("Код из письма") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (!isResetMode || codeRequested) {
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (isResetMode) "Новый пароль" else "Пароль") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (isResetMode && codeRequested) {
                        TextField(
                            value = passwordConfirm,
                            onValueChange = { passwordConfirm = it },
                            label = { Text("Повторите пароль") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    error?.let {
                        Text(text = it, color = Color(0xFFE85B6A), style = MaterialTheme.typography.body2)
                    }
                    infoMessage?.let {
                        Text(text = it, color = Color(0xFF16A34A), style = MaterialTheme.typography.body2)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                error = null
                                infoMessage = null
                                if (isResetMode) {
                                    if (!codeRequested) {
                                        when (val result = authSession.forgotPassword(email)) {
                                            is AuthActionResult.Failure -> error = result.message
                                            is AuthActionResult.Success -> {
                                                codeRequested = true
                                                infoMessage = result.message
                                            }
                                        }
                                    } else {
                                        val passwordValidationError = validatePassword(password)
                                        when {
                                            passwordValidationError != null -> error = passwordValidationError
                                            password != passwordConfirm -> error = "Пароли не совпадают"
                                            else -> when (val result = authSession.resetPassword(email, resetCode, password)) {
                                                is AuthActionResult.Failure -> error = result.message
                                                is AuthActionResult.Success -> {
                                                    authMode = AuthMode.Login
                                                    password = ""
                                                    passwordConfirm = ""
                                                    resetCode = ""
                                                    codeRequested = false
                                                    infoMessage = result.message
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val passwordValidationError = if (isRegisterMode) validatePassword(password) else null
                                    if (passwordValidationError != null) {
                                        error = passwordValidationError
                                    } else {
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
                                    }
                                }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() &&
                            when (authMode) {
                                AuthMode.Login -> password.isNotBlank()
                                AuthMode.Register -> name.isNotBlank() && password.isNotBlank()
                                AuthMode.ForgotPassword -> !codeRequested ||
                                    (resetCode.isNotBlank() && password.isNotBlank() && passwordConfirm.isNotBlank())
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5E4B8B), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isLoading) {
                                "Подождите..."
                            } else {
                                when (authMode) {
                                    AuthMode.Login -> "Войти"
                                    AuthMode.Register -> "Создать аккаунт"
                                    AuthMode.ForgotPassword -> if (codeRequested) "Изменить пароль" else "Получить код"
                                }
                            }
                        )
                    }

                    if (!isResetMode) {
                        Button(
                            onClick = {
                                authMode = if (isRegisterMode) AuthMode.Login else AuthMode.Register
                                error = null
                                infoMessage = null
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF1E7FB), contentColor = Color(0xFF5E4B8B)),
                            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isRegisterMode) "У меня уже есть аккаунт" else "Создать новый аккаунт")
                        }
                    }

                    TextButton(
                        onClick = {
                            authMode = if (isResetMode) AuthMode.Login else AuthMode.ForgotPassword
                            error = null
                            infoMessage = null
                            codeRequested = false
                            resetCode = ""
                            password = ""
                            passwordConfirm = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isResetMode) "Вернуться к входу" else "Забыли пароль?")
                    }
                }
            }
        }
    }
}

private enum class AuthMode {
    Login,
    Register,
    ForgotPassword
}

private fun validatePassword(password: String): String? {
    return when {
        password.length < 8 -> "Пароль должен быть не менее 8 символов"
        password.none { it.isUpperCase() } -> "Пароль должен содержать хотя бы одну заглавную букву"
        password.none { it.isLowerCase() } -> "Пароль должен содержать хотя бы одну строчную букву"
        password.none { it.isDigit() } -> "Пароль должен содержать хотя бы одну цифру"
        password.none { !it.isLetterOrDigit() } -> "Пароль должен содержать хотя бы один специальный символ"
        else -> null
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
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density.density,
                    fontScale = density.fontScale * 1.12f
                )
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    DesktopRail(selectedTab = selectedTab, onTabSelected = onTabSelected)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 36.dp, vertical = 30.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1180.dp)) {
                            content()
                        }
                    }
                }
            }
        } else {
            Scaffold(
                backgroundColor = Color.Transparent,
                bottomBar = {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        BottomNavigation(
                            backgroundColor = AppCard.copy(alpha = 0.94f),
                            elevation = 16.dp,
                            modifier = Modifier.clip(RoundedCornerShape(28.dp))
                        ) {
                            DashboardTab.entries.filter { it.showInNavigation }.forEach { tab ->
                                BottomNavigationItem(
                                    selected = selectedTab == tab,
                                    onClick = { onTabSelected(tab) },
                                    icon = { NavigationGlyph(tab, selectedTab == tab, size = 38.dp) },
                                    label = { Text(tab.title, fontSize = 10.sp, maxLines = 1) },
                                    selectedContentColor = AppPurple,
                                    unselectedContentColor = AppMuted
                                )
                            }
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
            .width(216.dp)
            .fillMaxHeight()
            .background(AppCard.copy(alpha = 0.72f))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "SmartBudget",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = AppInk
        )
        Text(
            text = "KMP preview",
            color = AppMuted,
            style = MaterialTheme.typography.body2
        )
        Spacer(modifier = Modifier.height(12.dp))
        DashboardTab.entries.filter { it.showInNavigation }.forEach { tab ->
            RailItem(tab = tab, selected = selectedTab == tab, onClick = { onTabSelected(tab) })
        }
    }
}

@Composable
private fun RailItem(tab: DashboardTab, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) Color.White.copy(alpha = 0.78f) else Color.Transparent
    val textColor = if (selected) AppPurple else Color(0xFF4B4760)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavigationGlyph(tab = tab, selected = selected, size = 42.dp)
        Text(tab.title, color = textColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun NavigationGlyph(tab: DashboardTab, selected: Boolean, size: androidx.compose.ui.unit.Dp = 34.dp) {
    val background = if (selected) AppPurple else AppLilac
    val content = if (selected) Color.White else AppMuted

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        FinanceIcon(tab.icon(), contentDescription = tab.title, tint = content)
    }
}

private fun DashboardTab.icon(): FinanceIcon = when (this) {
    DashboardTab.Home -> FinanceIcon.Home
    DashboardTab.Transactions -> FinanceIcon.List
    DashboardTab.Accounts -> FinanceIcon.Wallet
    DashboardTab.Groups -> FinanceIcon.Settings
    DashboardTab.Categories -> FinanceIcon.Tag
    DashboardTab.Members -> FinanceIcon.Group
    DashboardTab.Analytics -> FinanceIcon.Pie
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
