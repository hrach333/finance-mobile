package com.hrach.financeapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrach.financeapp.R
import com.hrach.financeapp.viewmodel.SessionViewModel

@Composable
fun AuthGateScreen(sessionViewModel: SessionViewModel) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.Login) }
    val loading by sessionViewModel.loading.collectAsStateWithLifecycle()
    val error by sessionViewModel.error.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFCCCFDF), Color(0xFFEFD6EF), Color(0xFFABA7CE))
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (mode == AuthMode.Login) "Вход в семейный бюджет" else "Регистрация нового пользователя",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (mode == AuthMode.Login) {
                    LoginForm(sessionViewModel, loading)
                } else {
                    RegisterForm(sessionViewModel, loading)
                }

                if (!error.isNullOrBlank()) {
                    Text(text = error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }

                TextButton(
                    onClick = {
                        sessionViewModel.clearError()
                        mode = if (mode == AuthMode.Login) AuthMode.Register else AuthMode.Login
                    }
                ) {
                    Text(if (mode == AuthMode.Login) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти")
                }
            }
        }
    }
}

@Composable
private fun LoginForm(sessionViewModel: SessionViewModel, loading: Boolean) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Пароль") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                )
            }
        }
    )
    Spacer(Modifier.height(4.dp))
    Button(
        onClick = { sessionViewModel.login(email.trim(), password) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !loading && email.isNotBlank() && password.isNotBlank()
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
        } else {
            Text("Войти")
        }
    }
}

@Composable
private fun RegisterForm(sessionViewModel: SessionViewModel, loading: Boolean) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    val passwordError = passwordConfirm.isNotBlank() && password != passwordConfirm

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Имя") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Пароль") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                )
            }
        }
    )
    OutlinedTextField(
        value = passwordConfirm,
        onValueChange = { passwordConfirm = it },
        label = { Text("Повтори пароль") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = passwordError,
        visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordConfirmVisible = !passwordConfirmVisible }) {
                Icon(
                    imageVector = if (passwordConfirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordConfirmVisible) "Скрыть пароль" else "Показать пароль"
                )
            }
        }
    )
    if (passwordError) {
        Text("Пароли не совпадают", color = MaterialTheme.colorScheme.error)
    }
    Spacer(Modifier.height(4.dp))
    Button(
        onClick = { sessionViewModel.register(name.trim(), email.trim(), password) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !loading && name.isNotBlank() && email.isNotBlank() && password.length >= 8 && !passwordError
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
        } else {
            Text("Зарегистрироваться")
        }
    }
}

enum class AuthMode {
    Login,
    Register
}
