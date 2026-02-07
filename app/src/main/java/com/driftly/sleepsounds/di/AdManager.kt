package com.driftly.sleepsounds.di

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor() {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    // TODO: Replace with production ad unit IDs before release
    companion object {
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741" // Test
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Test
        const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921" // Test
    }

    fun createBannerAdView(context: Context): AdView {
        return AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BANNER_AD_UNIT_ID
            loadAd(AdRequest.Builder().build())
        }
    }

    fun loadInterstitial(context: Context) {
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitial(activity: Activity): Boolean {
        return interstitialAd?.let {
            it.show(activity)
            interstitialAd = null
            true
        } ?: false
    }

    fun loadRewarded(context: Context) {
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun showRewarded(activity: Activity, onRewarded: () -> Unit): Boolean {
        return rewardedAd?.let { ad ->
            ad.show(activity) { onRewarded() }
            rewardedAd = null
            true
        } ?: false
    }

    fun hasInterstitial(): Boolean = interstitialAd != null
    fun hasRewarded(): Boolean = rewardedAd != null
}
