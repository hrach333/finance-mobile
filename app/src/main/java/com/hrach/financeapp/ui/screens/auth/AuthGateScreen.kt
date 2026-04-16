package com.hrach.financeapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrach.financeapp.R
import com.hrach.financeapp.viewmodel.SessionViewModel

@Composable
fun AuthGateScreen(sessionViewModel: SessionViewModel) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.Login) }
    var infoMessage by rememberSaveable { mutableStateOf("") }
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
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (mode) {
                        AuthMode.Login -> "Вход в семейный бюджет"
                        AuthMode.Register -> "Регистрация нового пользователя"
                        AuthMode.ForgotPassword -> "Восстановление пароля"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))

                when (mode) {
                    AuthMode.Login -> LoginForm(
                        sessionViewModel = sessionViewModel,
                        loading = loading,
                        onForgotPassword = {
                            sessionViewModel.clearError()
                            infoMessage = ""
                            mode = AuthMode.ForgotPassword
                        }
                    )
                    AuthMode.Register -> RegisterForm(sessionViewModel, loading)
                    AuthMode.ForgotPassword -> ForgotPasswordForm(
                        sessionViewModel = sessionViewModel,
                        loading = loading,
                        onSuccess = {
                            infoMessage = "Код отправлен на ваш email."
                            sessionViewModel.clearError()
                        },
                        onResetComplete = {
                            infoMessage = it
                            sessionViewModel.clearError()
                            mode = AuthMode.Login
                        },
                        onBack = {
                            sessionViewModel.clearError()
                            infoMessage = ""
                            mode = AuthMode.Login
                        }
                    )
                }

                if (!error.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                if (error.isNullOrBlank() && infoMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = infoMessage, color = MaterialTheme.colorScheme.primary)
                }

                if (mode != AuthMode.ForgotPassword) {
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(
                        onClick = {
                            sessionViewModel.clearError()
                            infoMessage = ""
                            mode = if (mode == AuthMode.Login) AuthMode.Register else AuthMode.Login
                        }
                    ) {
                        Text(
                            text = if (mode == AuthMode.Login) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    sessionViewModel: SessionViewModel,
    loading: Boolean,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Пароль") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                )
            }
        }
    )
    Spacer(modifier = Modifier.height(24.dp))
    val isEnabled = !loading && email.isNotBlank() && password.isNotBlank()
    Button(
        onClick = { sessionViewModel.login(email.trim(), password) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.primary else Color.LightGray,
            contentColor = if (isEnabled) Color.White else Color.Gray
        )
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Text("Войти")
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onForgotPassword) {
            Text(
                "Забыли пароль?",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RegisterForm(sessionViewModel: SessionViewModel, loading: Boolean) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Имя") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Пароль") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                )
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = passwordConfirm,
        onValueChange = { passwordConfirm = it },
        label = { Text("Повтори пароль") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
        visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(
                onClick = { passwordConfirmVisible = !passwordConfirmVisible },
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = if (passwordConfirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordConfirmVisible) "Скрыть пароль" else "Показать пароль"
                )
            }
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = acceptedTerms,
            onCheckedChange = { acceptedTerms = it }
        )
        val annotatedString = buildAnnotatedString {
            append("Регистрируясь, вы принимаете ")
            pushStringAnnotation(tag = "privacy", annotation = "privacy_policy")
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("Политику конфиденциальности")
            }
            pop()
            append(" и ")
            pushStringAnnotation(tag = "terms", annotation = "terms_of_use")
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("Условия использования")
            }
            pop()
        }
        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                    .firstOrNull()?.let {
                        LegalDocumentActivity.start(context, "privacy_policy")
                    }
                annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                    .firstOrNull()?.let {
                        LegalDocumentActivity.start(context, "terms_of_use")
                    }
            }
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
    val isEnabled = !loading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && passwordConfirm.isNotBlank() && acceptedTerms
    Button(
        onClick = { sessionViewModel.register(name.trim(), email.trim(), password) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.primary else Color.LightGray,
            contentColor = if (isEnabled) Color.White else Color.Gray
        )
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Text("Зарегистрироваться")
        }
    }
}

@Composable
private fun ForgotPasswordForm(
    sessionViewModel: SessionViewModel,
    loading: Boolean,
    onSuccess: () -> Unit,
    onResetComplete: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var tokenRequested by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirm by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var passwordConfirmVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(Modifier.height(4.dp))
    Button(
        onClick = {
            sessionViewModel.forgotPassword(email.trim()) {
                tokenRequested = true
                onSuccess()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !loading && email.isNotBlank()
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
        } else {
            Text("Получить код")
        }
    }

    if (tokenRequested) {
        Spacer(Modifier.height(10.dp))
        Text(
            "Код отправлен на ваш email. Введите его ниже.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Введите код") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Новый пароль") },
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
            label = { Text("Повторите пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = {
                sessionViewModel.resetPassword(email.trim(), code.trim(), password) {
                    tokenRequested = false
                    code = ""
                    password = ""
                    passwordConfirm = ""
                    onResetComplete("Пароль успешно изменён.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && code.isNotBlank() && password.isNotBlank() && passwordConfirm.isNotBlank()
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
            } else {
                Text("Изменить пароль")
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onBack) {
        Text("Вернуться к входу")
    }
}

enum class AuthMode {
    Login,
    Register,
    ForgotPassword
}
