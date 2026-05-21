package com.nononsenseapps.feeder.localtranslation

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class BergamotWebTranslator(
    override val di: DI,
) : DIAware {
    private val application: Application by instance()
    private val json =
        Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        }
    private val bridge = Bridge()
    private val requestId = AtomicLong(0)
    private val initMutex = Mutex()
    private var webView: WebView? = null
    private var ready = CompletableDeferred<Unit>()
    private var initializedRegistryJson: String = ""

    suspend fun translate(
        content: List<String>,
        sourceLanguage: String,
        targetLanguage: String,
        preserveHtml: Boolean,
        modelRegistry: List<BergamotModelRegistryEntry>,
    ): BergamotWebTranslationResult =
        // WebView must be created and called on the UI thread. Translation work runs inside
        // the Bergamot Web Worker loaded by this page, so this should only schedule JS calls.
        withContext(Dispatchers.Main.immediate) {
            initialize(modelRegistry)
            translateBatch(
                content = content,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                preserveHtml = preserveHtml,
            )
        }

    private suspend fun initialize(modelRegistry: List<BergamotModelRegistryEntry>) {
        val registryJson = json.encodeToString(modelRegistry)
        initMutex.withLock {
            ensureWebView()
            try {
                withTimeout(WEBVIEW_INIT_TIMEOUT_MS) { ready.await() }
            } catch (e: Exception) {
                destroyWebView()
                throw e
            }
            if (initializedRegistryJson != registryJson) {
                evaluate("window.FeederBergamot.initialize($registryJson);")
                initializedRegistryJson = registryJson
            }
        }
    }

    private fun destroyWebView() {
        webView?.destroy()
        webView = null
        initializedRegistryJson = ""
    }

    private suspend fun translateBatch(
        content: List<String>,
        sourceLanguage: String,
        targetLanguage: String,
        preserveHtml: Boolean,
    ): BergamotWebTranslationResult {
        val id = requestId.incrementAndGet()
        val response = CompletableDeferred<BergamotWebTranslationResult>()
        bridge.pending[id] = response

        evaluate(
            "window.FeederBergamot.translateBatch(" +
                "$id," +
                "${json.encodeToString(sourceLanguage)}," +
                "${json.encodeToString(targetLanguage)}," +
                "${json.encodeToString(content)}," +
                "$preserveHtml" +
                ");",
        )

        return withTimeoutOrNull(WEBVIEW_TRANSLATION_TIMEOUT_MS) { response.await() }
            ?: run {
                bridge.pending.remove(id)
                BergamotWebTranslationResult.Error("Bergamot WebView translation timed out.")
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun ensureWebView() {
        if (webView != null) {
            return
        }

        ready = CompletableDeferred()
        webView =
            WebView(application).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = false
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.allowFileAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = false
                webChromeClient =
                    object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage): Boolean {
                            android.util.Log.w(
                                LOG_TAG,
                                "WebView console [$consoleMessage]: ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()} ${consoleMessage.message()}",
                            )
                            return true
                        }
                    }
                webViewClient =
                    object : WebViewClient() {
                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError,
                        ) {
                            logDebug(
                                LOG_TAG,
                                "WebView error ${error.errorCode}: ${error.description} for ${request.url}",
                            )
                        }
                    }
                addJavascriptInterface(bridge, "AndroidBergamot")
                loadUrl(BERGAMOT_PAGE_URL)
            }
    }

    private fun evaluate(script: String) {
        webView?.evaluateJavascript(script, null)
            ?: throw IllegalStateException("Bergamot WebView is not initialized")
    }

    private inner class Bridge {
        val pending = ConcurrentHashMap<Long, CompletableDeferred<BergamotWebTranslationResult>>()

        @JavascriptInterface
        fun onReady() {
            Log.w(LOG_TAG, "BRIDGE onReady() called from JS!")
            ready.complete(Unit)
        }

        @JavascriptInterface
        fun onTranslationSuccess(
            id: Long,
            translatedText: String,
        ) {
            pending.remove(id)?.complete(BergamotWebTranslationResult.Success(listOf(translatedText)))
        }

        @JavascriptInterface
        fun onTranslationBatchSuccess(
            id: Long,
            translatedTextJson: String,
        ) {
            Log.w(LOG_TAG, "BRIDGE onTranslationBatchSuccess id=$id jsonLen=${translatedTextJson.length}")
            val translatedTexts =
                runCatching {
                    json.decodeFromString<List<String>>(translatedTextJson)
                }.getOrElse {
                    pending.remove(id)?.complete(BergamotWebTranslationResult.Error(it.message ?: "Could not parse Bergamot response."))
                    return
                }
            pending.remove(id)?.complete(BergamotWebTranslationResult.Success(translatedTexts))
        }

        @JavascriptInterface
        fun onTranslationError(
            id: Long,
            message: String,
        ) {
            Log.w(LOG_TAG, "BRIDGE onTranslationError id=$id message=$message")
            pending.remove(id)?.complete(BergamotWebTranslationResult.Error(message))
        }

        @JavascriptInterface
        fun onLog(message: String) {
            Log.w(LOG_TAG, "BRIDGE onLog from JS: $message")
            logDebug(LOG_TAG, message)
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_BERGAMOT"
        private const val BERGAMOT_PAGE_URL = "file:///android_asset/bergamot/index.html"
        private const val WEBVIEW_INIT_TIMEOUT_MS = 60_000L
        private const val WEBVIEW_TRANSLATION_TIMEOUT_MS = 5 * 60_000L
    }
}

sealed interface BergamotWebTranslationResult {
    data class Success(
        val values: List<String>,
    ) : BergamotWebTranslationResult

    data class Error(
        val message: String,
    ) : BergamotWebTranslationResult
}
