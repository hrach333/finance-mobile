package com.hrach.financeapp.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import com.hrach.financeapp.ui.theme.FinanceAppTheme

class LegalDocumentActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_DOCUMENT_TYPE = "document_type"

        fun start(context: Context, documentType: String) {
            val intent = Intent(context, LegalDocumentActivity::class.java).apply {
                putExtra(EXTRA_DOCUMENT_TYPE, documentType)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val documentType = intent.getStringExtra(EXTRA_DOCUMENT_TYPE) ?: "terms_of_use"

        setContent {
            FinanceAppTheme {
                LegalDocumentScreen(documentType, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentScreen(documentType: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val title = when (documentType) {
        "privacy_policy" -> "Политика конфиденциальности"
        "terms_of_use" -> "Условия использования"
        else -> "Документ"
    }

    val htmlContent = loadHtmlFromAssets(context, documentType)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = false
                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            update = { webView ->
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
        )
    }
}

private fun loadHtmlFromAssets(context: Context, documentType: String): String {
    val fileName = "legal/$documentType.html"
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        "Ошибка загрузки документа: ${e.message}"
    }
}