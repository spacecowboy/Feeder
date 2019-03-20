package com.nononsenseapps.feeder.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.openLinkInBrowser


const val ARG_URL = "url"

class ReaderWebViewActivity : BaseActivity() {
    private var webView: WebView? = null
    var url: String = ""
    private var enclosureUrl: String? = null
    private var shareActionProvider: ShareActionProvider? = null
    private var isWebViewAvailable: Boolean = false

    @SuppressLint("SetJavaScriptEnabled")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_webview)
        initializeActionBar()
        val ab = supportActionBar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true)
            ab.setHomeButtonEnabled(true)
            ab.setDisplayShowTitleEnabled(false)
        }

        webView = findViewById(R.id.webview)


        CookieManager.getInstance().setAcceptCookie(false)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.builtInZoomControls = true
        webView?.webViewClient = WebViewClientHandler

        // Arguments are set by activity after fragment has been created
        intent.extras?.let { extras ->
            url = extras.getString(ARG_URL, null) ?: ""
            enclosureUrl = extras.getString(ARG_ENCLOSURE, null)
        }

        if (url.isNotBlank()) {
            isWebViewAvailable = true
            webView?.loadUrl(url)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.webview, menu)

        if (menu != null) {
            // Locate MenuItem with ShareActionProvider
            val shareItem = menu.findItem(R.id.action_share)

            // Fetch and store ShareActionProvider
            shareActionProvider = MenuItemCompat.getActionProvider(shareItem) as ShareActionProvider

            // Update share intent everytime a page is loaded
            WebViewClientHandler.onPageStartedListener = { url: String? ->
                if (url != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, url)
                    shareActionProvider?.setShareIntent(shareIntent)
                }
            }
            // Invoke it immediately with current url
            WebViewClientHandler.onPageStartedListener?.invoke(url)

            // Show/Hide enclosure
            menu.findItem(R.id.action_open_enclosure).isVisible = enclosureUrl != null
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.action_open_in_browser -> {
                    // Use the currently visible page as the url
                    val link = webView?.url ?: url
                    openLinkInBrowser(this, link)

                    true
                }
                R.id.action_open_enclosure -> {
                    enclosureUrl?.let { link ->
                        openLinkInBrowser(this, link)
                    }

                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onResume() {
        webView?.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        webView?.destroy()
        super.onDestroy()
    }
}

private object WebViewClientHandler : WebViewClient() {
    var onPageStartedListener: ((String?) -> Unit)? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        onPageStartedListener?.invoke(url)
    }

    @Suppress("OverridingDeprecatedMember")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        // prevent links from loading in external web browser
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        // prevent links from loading in external web browser
        return false
    }
}
