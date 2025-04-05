package com.raafat.revise


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles all WebView related operations
 */
class WebViewManager(private val context: Context) {


    private var isWebViewInitialized = false

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun setupWebView(webView: WebView) = withContext(Dispatchers.Main) {

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // Add the JavaScript interface
        webView.addJavascriptInterface(WebViewJavaScriptInterface(context as MainActivity), "Android")


        if (!isWebViewInitialized) {
            webView.apply {
                loadUrl("file:///android_asset/index.html")
                settings.javaScriptEnabled = true
                settings.textZoom = 100
                setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
                setBackgroundColor(Color.parseColor("#191919"))
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        isWebViewInitialized = true
                    }
                }
            }
        }
    }

    suspend fun displayAya(webView: WebView, aya: Aya) = withContext(Dispatchers.Main) {
        if (!isWebViewInitialized) {
            setupAndDisplayAya(webView, aya, false)
        } else {
            webView.evaluateJavascript("javascript:receiveAya(\'${aya.ayaText}\')", null)
        }
    }

    suspend fun displayAyaPrev(webView: WebView, aya: Aya) = withContext(Dispatchers.Main) {
        if (!isWebViewInitialized) {
            setupAndDisplayAya(webView, aya, true)
        } else {
            webView.evaluateJavascript("javascript:receiveAyaPrev(\'${aya.ayaText}\')", null)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun setupAndDisplayAya(webView: WebView, aya: Aya, isPrev: Boolean) = withContext(Dispatchers.Main) {
        webView.apply {
            loadUrl("file:///android_asset/index.html")
            settings.javaScriptEnabled = true
            settings.textZoom = 100
            setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    view?.setBackgroundColor(Color.parseColor("#191919"))
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    isWebViewInitialized = true
                    if (isPrev) {
                        webView.evaluateJavascript("javascript:receiveAyaPrev(\'${aya.ayaText}\')", null)
                    } else {
                        webView.evaluateJavascript("javascript:receiveAya(\'${aya.ayaText}\')", null)
                    }
                }
            }
        }
    }

    suspend fun toggleVisibility(webView: WebView) = withContext(Dispatchers.Main) {
        webView.evaluateJavascript("javascript:toggleHideShow();", null)
    }

    suspend fun showNextWord(webView: WebView): Boolean = withContext(Dispatchers.Main) {
        var result = false
        webView.evaluateJavascript("javascript:showNext()") { hasNext ->
            result = hasNext.toBoolean()
        }
        // Add a small delay to ensure the JS result is processed
        kotlinx.coroutines.delay(50)
        return@withContext result
    }

    suspend fun showPrevWord(webView: WebView, ayaNo: Int): Boolean = withContext(Dispatchers.Main) {
        var result = false
        webView.evaluateJavascript("javascript:showPrev($ayaNo)") { hasPrev ->
            result = hasPrev.toBoolean()
        }
        // Add a small delay to ensure the JS result is processed
        kotlinx.coroutines.delay(50)
        return@withContext result
    }

    suspend fun hasScrollOverflow(webView: WebView): Boolean = withContext(Dispatchers.Main) {
        var result = false
        webView.evaluateJavascript(
            """
            (function() {
                const ayaTextDiv = document.getElementById('ayaText');
                const hasOverflow = ayaTextDiv.scrollHeight > ayaTextDiv.clientHeight;
                const isNotScrolled = ayaTextDiv.scrollTop === 0;
            
                return hasOverflow && !isNotScrolled;
            })();
            """.trimIndent()
        ) { hasOverflow ->
            result = hasOverflow.toBoolean()
        }
        // Add a small delay to ensure the JS result is processed
        kotlinx.coroutines.delay(50)
        return@withContext result
    }

    suspend fun scrollToTop(webView: WebView) = withContext(Dispatchers.Main) {
        webView.evaluateJavascript(
            """
            (function() {
                const ayaTextDiv = document.getElementById('ayaText');
                ayaTextDiv.scroll({
                    top: 0,
                    behavior: 'smooth'
                });
            })();
            """.trimIndent(), null
        )
    }

    suspend fun scrollToNextPage(webView: WebView) = withContext(Dispatchers.Main) {
        webView.evaluateJavascript(
            """
            (function() {
                let y;
                if (window.matchMedia("(min-width: 600px)").matches) {
                    y = 6
                } else {
                    y = 4
                }
                const ayaTextDiv = document.getElementById('ayaText');
                const scrollAmount = parseFloat(getComputedStyle(document.documentElement).fontSize) * y; 
                const numLines = Math.floor(window.innerHeight / scrollAmount);

                ayaTextDiv.scroll({
                    top: ayaTextDiv.scrollTop + scrollAmount * (numLines - 1),
                    behavior: 'smooth'
                });
            })();
            """.trimIndent(), null
        )
    }

    suspend fun isScrolledToBottom(webView: WebView): Boolean = withContext(Dispatchers.Main) {
        var result = false
        webView.evaluateJavascript(
            """
            (function() {
                const ayaTextDiv = document.getElementById('ayaText');
                const hasOverflow = ayaTextDiv.scrollHeight > ayaTextDiv.clientHeight;
                const isScrolledToBottom = ayaTextDiv.scrollTop + ayaTextDiv.clientHeight >= ayaTextDiv.scrollHeight - 2;
           
                return hasOverflow && !isScrolledToBottom;
            })();
            """.trimIndent()
        ) { hasOverflow ->
            result = hasOverflow.toBoolean()
        }
        // Add a small delay to ensure the JS result is processed
        kotlinx.coroutines.delay(50)
        return@withContext result
    }

    private inner class WebViewJavaScriptInterface(private val activity: MainActivity) {
        @android.webkit.JavascriptInterface
        fun moveToNextAya() {
            activity.runOnUiThread {
                activity.moveToNextAya()
            }
        }

        @android.webkit.JavascriptInterface
        fun moveToPrevAya() {
            activity.runOnUiThread {
                activity.moveToPrevAya()
            }
        }
    }
}

