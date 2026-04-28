package com.hrach.financeapp.auth

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64
import java.util.Properties
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class DesktopYandexOAuthTokenProvider(
    private val clientId: String = resolveClientId()
) {
    suspend fun requestToken(): String = withContext(Dispatchers.IO) {
        require(clientId.isNotBlank()) {
            "YANDEX_CLIENT_ID не задан для desktop-сборки"
        }

        val state = randomToken()
        val codeVerifier = randomToken(byteCount = 48)
        val codeChallenge = codeVerifier.toCodeChallenge()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", CALLBACK_PORT), 0)
        val redirectUri = "http://127.0.0.1:$CALLBACK_PORT$CALLBACK_PATH"
        val codeResult = CompletableDeferred<String>()

        server.createContext(CALLBACK_PATH) { exchange ->
            val params = exchange.requestURI.rawQuery.orEmpty().toQueryMap()
            val responseHtml = when {
                params["state"] != state -> {
                    codeResult.completeExceptionally(IllegalStateException("Яндекс вернул неверный state"))
                    failurePage("Не удалось подтвердить запрос. Вернитесь в приложение.")
                }
                params["error"] != null -> {
                    val message = params["error_description"] ?: params["error"].orEmpty()
                    codeResult.completeExceptionally(IllegalStateException(message.ifBlank { "Яндекс отклонил вход" }))
                    failurePage("Вход через Яндекс не завершен. Вернитесь в приложение.")
                }
                params["code"].isNullOrBlank() -> {
                    codeResult.completeExceptionally(IllegalStateException("Яндекс не вернул код авторизации"))
                    failurePage("Код авторизации не получен. Вернитесь в приложение.")
                }
                else -> {
                    codeResult.complete(params.getValue("code"))
                    successPage()
                }
            }
            exchange.sendHtml(responseHtml)
        }

        server.executor = null
        server.start()

        try {
            openBrowser(
                buildAuthorizeUrl(
                    clientId = clientId,
                    redirectUri = redirectUri,
                    state = state,
                    codeChallenge = codeChallenge
                )
            )
            val code = withTimeout(Duration.ofMinutes(3).toMillis()) { codeResult.await() }
            exchangeCodeForToken(clientId = clientId, code = code, codeVerifier = codeVerifier)
        } finally {
            server.stop(0)
        }
    }

    private fun buildAuthorizeUrl(
        clientId: String,
        redirectUri: String,
        state: String,
        codeChallenge: String
    ): URI {
        val params = formEncode(
            "response_type" to "code",
            "client_id" to clientId,
            "redirect_uri" to redirectUri,
            "state" to state,
            "code_challenge" to codeChallenge,
            "code_challenge_method" to "S256"
        )
        return URI.create("https://oauth.yandex.com/authorize?$params")
    }

    private fun openBrowser(uri: URI) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri)
            return
        }

        val command = when {
            System.getProperty("os.name").startsWith("Mac", ignoreCase = true) -> listOf("open", uri.toString())
            System.getProperty("os.name").startsWith("Windows", ignoreCase = true) ->
                listOf("rundll32", "url.dll,FileProtocolHandler", uri.toString())
            else -> listOf("xdg-open", uri.toString())
        }
        ProcessBuilder(command).start()
    }

    private fun exchangeCodeForToken(
        clientId: String,
        code: String,
        codeVerifier: String
    ): String {
        val requestBody = formEncode(
            "grant_type" to "authorization_code",
            "code" to code,
            "client_id" to clientId,
            "code_verifier" to codeVerifier
        )
        val request = HttpRequest.newBuilder(URI.create("https://oauth.yandex.com/token"))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        val root = Json.parseToJsonElement(response.body()).jsonObject

        if (response.statusCode() !in 200..299) {
            throw IllegalStateException(root.errorMessage() ?: "Яндекс не выдал OAuth token")
        }

        return root.stringValue("access_token")
            ?: throw IllegalStateException(root.errorMessage() ?: "В ответе Яндекса нет access_token")
    }

    private fun successPage(): String = """
        <!doctype html>
        <html lang="ru">
        <head><meta charset="utf-8"><title>Smart Budget</title></head>
        <body style="font-family: sans-serif; padding: 32px;">
            <h2>Вход выполнен</h2>
            <p>Можно вернуться в приложение.</p>
        </body>
        </html>
    """.trimIndent()

    private fun failurePage(message: String): String = """
        <!doctype html>
        <html lang="ru">
        <head><meta charset="utf-8"><title>Smart Budget</title></head>
        <body style="font-family: sans-serif; padding: 32px;">
            <h2>Вход не завершен</h2>
            <p>${message.escapeHtml()}</p>
        </body>
        </html>
    """.trimIndent()

    private fun HttpExchange.sendHtml(html: String) {
        val bytes = html.toByteArray(StandardCharsets.UTF_8)
        responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        sendResponseHeaders(200, bytes.size.toLong())
        responseBody.use { it.write(bytes) }
    }

    private fun JsonObject.stringValue(key: String): String? =
        (this[key] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }

    private fun JsonObject.errorMessage(): String? =
        stringValue("error_description") ?: stringValue("error")

    private fun String.toCodeChallenge(): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray(StandardCharsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun String.escapeHtml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    private fun String.toQueryMap(): Map<String, String> {
        if (isBlank()) return emptyMap()
        return split("&")
            .mapNotNull { part ->
                val index = part.indexOf("=")
                if (index <= 0) return@mapNotNull null
                part.substring(0, index).urlDecode() to part.substring(index + 1).urlDecode()
            }
            .toMap()
    }

    private fun String.urlDecode(): String =
        URLDecoder.decode(this, StandardCharsets.UTF_8)

    private fun randomToken(byteCount: Int = 32): String {
        val bytes = ByteArray(byteCount)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun formEncode(vararg params: Pair<String, String>): String =
        params.joinToString("&") { (key, value) -> "${key.urlEncode()}=${value.urlEncode()}" }

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8)

    companion object {
        private const val CALLBACK_PORT = 34319
        private const val CALLBACK_PATH = "/yandex-oauth"

        fun resolveClientId(): String {
            System.getProperty("YANDEX_CLIENT_ID")?.takeIf { it.isNotBlank() }?.let { return it }
            System.getenv("YANDEX_CLIENT_ID")?.takeIf { it.isNotBlank() }?.let { return it }

            val candidates = listOf(
                Path.of("gradle.properties"),
                Path.of("../gradle.properties")
            )
            for (path in candidates) {
                val properties = runCatching {
                    Properties().apply {
                        path.toFile().takeIf { it.isFile }?.inputStream()?.use(::load)
                    }
                }.getOrNull()
                properties?.getProperty("YANDEX_CLIENT_ID")?.takeIf { it.isNotBlank() }?.let { return it }
            }

            return ""
        }

        fun desktopDeviceId(): String =
            UUID.nameUUIDFromBytes(System.getProperty("user.name").toByteArray()).toString()
    }
}
