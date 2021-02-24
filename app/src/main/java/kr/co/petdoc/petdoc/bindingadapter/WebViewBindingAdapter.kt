package kr.co.petdoc.petdoc.bindingadapter

import androidx.databinding.BindingAdapter
import kr.co.petdoc.petdoc.nicepay.NicePayWebView

@BindingAdapter("webViewListener")
fun setOnPurchaseCompleteListener(
    webView: NicePayWebView,
    listener: NicePayWebView.NicePayWebViewListener)
{
    webView.setPurchaseWebViewListener(listener)
}