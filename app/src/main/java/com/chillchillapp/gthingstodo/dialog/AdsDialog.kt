package com.chillchillapp.gthingstodo.dialog

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import com.chillchillapp.gthingstodo.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.android.synthetic.main.dialog_ads1.*

class AdsDialog(private var activity: Activity): Dialog(activity) {

    private var l: MyEvent? = null
    interface MyEvent{
        fun onMyEventListener(status: String)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_ads1)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)

        setAds()

        closeIV.setOnClickListener {
            dismiss()
        }

        if(negativeTV != null){
            negativeTV.setOnClickListener {
                dismiss()
            }
        }
    }

    fun setMyEvent(l: MyEvent){
        this.l = l
    }

    private fun setAds(){
        var adLoader: AdLoader? = null
        adLoader = AdLoader.Builder(activity, activity.getString(R.string.Ads_Native_Static_UnitId))
            .forNativeAd { nativeAd : NativeAd ->
                // Show the ad.

                try {
                    if (activity.isDestroyed) {
                        nativeAd.destroy()

                        return@forNativeAd
                    }

                    nativeAd(nativeAd)

                    if (!adLoader!!.isLoading) {
                        nativeAdView.visibility = View.VISIBLE
                        l?.onMyEventListener("showAds")
                        Log.d("sadasdasd", "showAds.")
                    }

                }catch (e: Exception){
                    nativeAd.destroy()
                    Log.d("sadasdasd", "e: $e")
                }

            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                    Log.d("sadasdasd", "LoadAdError: "+ adError.message)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                // Methods in the NativeAdOptions.Builder class can be
                // used here to specify individual options settings.
                .build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun nativeAd(nativeAd : NativeAd){
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        val headline = nativeAd.headline
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val starRating = nativeAd.starRating
        val icon = nativeAd.icon

        mediaView.setMediaContent(nativeAd.mediaContent)
        mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)

        val secondaryText: String

        nativeAdView.callToActionView = callToActionView
        nativeAdView.headlineView = primaryView
        nativeAdView.mediaView = mediaView
        secondaryView.visibility = View.VISIBLE
        secondaryText = if (adHasOnlyStore(nativeAd)) {
            nativeAdView.storeView = secondaryView
            store
        } else if (!TextUtils.isEmpty(advertiser)) {
            nativeAdView.advertiserView = secondaryView
            advertiser
        } else {
            ""
        }

        primaryView.text = headline
        callToActionView.text = cta

        //  Set the secondary view to be the star rating if available.
        /*if (starRating != null && starRating > 0) {
            secondaryView.visibility = View.GONE;

        } else {
            secondaryView.text = secondaryText
            secondaryView.visibility = View.VISIBLE
        }*/

        secondaryView.text = secondaryText
        secondaryView.visibility = View.VISIBLE

        if (icon != null) {
            iconView.visibility = View.VISIBLE;
            iconView.setImageDrawable(icon.drawable)
        } else {
            iconView.visibility = View.GONE
        }

        if (tertiaryView != null) {
            tertiaryView.text = body
            nativeAdView.bodyView = tertiaryView
        }

        nativeAdView.setNativeAd(nativeAd);
    }
    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }
}