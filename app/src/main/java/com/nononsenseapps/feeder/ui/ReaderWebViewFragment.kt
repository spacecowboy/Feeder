package com.nononsenseapps.feeder.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.CoroutineScopedFragment
import com.nononsenseapps.feeder.util.openLinkInBrowser

const val ARG_URL = "url"

class ReaderWebViewFragment : CoroutineScopedFragment() {
    private var webView: WebView? = null
    var url: String = ""
    private var enclosureUrl: String? = null
    private var shareActionProvider: ShareActionProvider? = null
    private var isWebViewAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arguments ->
            url = arguments.getString(ARG_URL, null) ?: ""
            enclosureUrl = arguments.getString(ARG_ENCLOSURE, null)
        }

        setHasOptionsMenu(true)
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @SuppressLint("SetJavaScriptEnabled", "Recycle")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        webView?.destroy()
        webView = WebView(context)

        // Important to create webview before setting cookie policy on Android18
        CookieManager.getInstance().setAcceptCookie(false)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.builtInZoomControls = true
        webView?.webViewClient = WebViewClientHandler

        if (url.isNotBlank()) {
            isWebViewAvailable = true
            webView?.loadUrl(url)
        }

        return webView
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    override fun onResume() {
        webView?.onResume()
        super.onResume()
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    override fun onDestroyView() {
        isWebViewAvailable = false
        super.onDestroyView()
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    override fun onDestroy() {
        webView?.destroy()
        webView = null
        WebViewClientHandler.onPageStartedListener = null
        super.onDestroy()
    }

    /**
     * Gets the WebView.
     */
    /*val webView: WebView?
        get() {
            return if (isWebViewAvailable) webView else null*/
    //}

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.webview, menu)

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

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_open_in_browser -> {
                // Use the currently visible page as the url
                val link = webView?.url ?: url
                context?.let { context ->
                    openLinkInBrowser(context, link)
                }

                return true
            }
            R.id.action_open_enclosure -> {
                enclosureUrl?.let { link ->
                    context?.let { context ->
                        openLinkInBrowser(context, link)
                    }
                }

                return true
            }
            else -> return super.onOptionsItemSelected(menuItem)
        }
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

fun View.setPadding(left: Int? = null,
                    top: Int? = null,
                    right: Int? = null,
                    bottom: Int? = null) {
    this.setPadding(left ?: this.paddingLeft,
            top ?: this.paddingTop,
            right ?: this.paddingRight,
            bottom ?: this.paddingBottom)
}
