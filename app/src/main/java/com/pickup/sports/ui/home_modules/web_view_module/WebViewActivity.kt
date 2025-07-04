package com.pickup.sports.ui.home_modules.web_view_module

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import com.pickup.sports.R
import com.pickup.sports.databinding.ActivityWebViewBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {

    private val viewModel : WebViewActivityVm by  viewModels()

    private var stripeUrl : String ? =null

    override fun getLayoutResource(): Int {
        return R.layout.activity_web_view
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initOnClick()
        getData()
    }

    private fun getData() {
        stripeUrl = intent.getStringExtra("url")

        if (stripeUrl != null) {
            val loadingIndicator = binding.loadingIndicator
            val webSettings: WebSettings = binding.webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.allowFileAccess = true
            webSettings.allowFileAccessFromFileURLs = true
            webSettings.allowUniversalAccessFromFileURLs = true
            binding.webView.settings.setSupportZoom(true)
            binding.webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress == 100) {
                        loadingIndicator.visibility = View.GONE
                    } else {
                        loadingIndicator.visibility = View.GONE
                    }
                }
            }

            binding.webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    loadingIndicator.visibility = View.GONE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    loadingIndicator.visibility = View.GONE
                    url?.let {
                        if (it.contains("/account/success")) {
                            showToast("Success")
                            onBackPressedDispatcher.onBackPressed()
                        }
                        else if (it.contains("/account/refresh")){
                            showToast("Failed to connect stripe account ")
                            onBackPressedDispatcher.onBackPressed()
                        }
                        // view?.loadUrl(it)
                        Log.d("UrlData", "check $it")
                    }
                }
            }
            binding.webView.loadUrl(stripeUrl!!)

        }
    }

    private fun initOnClick() {

    }
}