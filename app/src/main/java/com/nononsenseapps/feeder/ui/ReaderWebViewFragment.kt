package com.nononsenseapps.feeder.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

const val ARG_URL = "url"

/**
 * Copy of WebViewFragment in later versions of Android
 */
class ReaderWebViewFragment : Fragment() {
    var url: String = ""

    private var mWebView: WebView? = null
    private var isWebViewAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arguments ->
            url = arguments.getString(ARG_URL)
        }
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mWebView?.destroy()
        mWebView = WebView(context)
        mWebView?.settings?.javaScriptEnabled = true
        mWebView?.settings?.builtInZoomControls = true
        mWebView?.webViewClient = object : WebViewClient() {
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
        super.onDestroy()
    }

    /**
     * Gets the WebView.
     */
    val webView: WebView?
        get() {
            return if (isWebViewAvailable) mWebView else null
        }

}
