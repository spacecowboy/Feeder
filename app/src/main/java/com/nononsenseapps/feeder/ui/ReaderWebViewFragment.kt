package com.nononsenseapps.feeder.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.nononsenseapps.feeder.R

const val ARG_URL = "url"

/**
 * Copy of WebViewFragment in later versions of Android, plus menu
 */
class ReaderWebViewFragment : Fragment() {
    var url: String = ""
    private var enclosureUrl: String? = null
    private var shareActionProvider: ShareActionProvider? = null

    private var mWebView: WebView? = null
    private var isWebViewAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arguments ->
            url = arguments.getString(ARG_URL)
            enclosureUrl = arguments.getString(ARG_ENCLOSURE, null)
        }

        setHasOptionsMenu(true)
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mWebView?.destroy()
        CookieManager.getInstance().setAcceptCookie(false)
        mWebView = WebView(context)
        mWebView?.settings?.javaScriptEnabled = true
        mWebView?.settings?.builtInZoomControls = true
        mWebView?.webViewClient = WebViewClientHandler

        if (url.isNotBlank()) {
            isWebViewAvailable = true
            mWebView?.loadUrl(url)
        }

        return mWebView
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    override fun onPause() {
        super.onPause()
        mWebView?.onPause()
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    override fun onResume() {
        mWebView?.onResume()
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
        mWebView?.destroy()
        mWebView = null
        WebViewClientHandler.onPageStartedListener = null
        super.onDestroy()
    }

    /**
     * Gets the WebView.
     */
    val webView: WebView?
        get() {
            return if (isWebViewAvailable) mWebView else null
        }

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
                val uri = Uri.parse(webView?.url ?: url)
                // Open in browser
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(activity, R.string.no_activity_for_link, Toast.LENGTH_SHORT).show()
                    Log.d("ReaderFragment", "No such activity: $e")
                }

                return true
            }
            R.id.action_open_enclosure -> {
                enclosureUrl?.let { enclosureUrl ->
                    val uri = Uri.parse(enclosureUrl)
                    // Open enclosure link
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(activity, R.string.no_activity_for_link, Toast.LENGTH_SHORT).show()
                        Log.d("ReaderFragment", "No such activity: $e")
                    }
                }

                return true
            }
            else -> return super.onOptionsItemSelected(menuItem)
        }
    }
}

private object WebViewClientHandler: WebViewClient() {
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
