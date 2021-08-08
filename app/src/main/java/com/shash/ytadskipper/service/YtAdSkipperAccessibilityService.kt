package com.shash.ytadskipper.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.preference.PreferenceManager
import com.shash.ytadskipper.utils.SETTINGS_ENABLE_SERVICE
import com.shash.ytadskipper.utils.SETTINGS_MUTE_AUDIO


class YtAdSkipperAccessibilityService : AccessibilityService() {

    private val TAG = "AdSkipperServiceT"
    private val AD_LEARN_MORE_BUTTON_ID = "com.google.android.youtube:id/player_learn_more_button"
    private val SKIP_AD_BUTTON_ID = "com.google.android.youtube:id/skip_ad_button"
    private val AD_PROGRESS_TEXT = "com.google.android.youtube:id/ad_progress_text"
    private val APP_PROMO_AD_CTA_OVERLAY = "com.google.android.youtube:id/app_promo_ad_cta_overlay"
    private val AD_COUNTDOWN = "com.google.android.youtube:id/ad_countdown"

    private var isMuted = false

    private var isRunning = false

    private val TELEGRAM_PACKAGE_NAME  ="org.telegram.messenger"

    override fun onInterrupt() {
        Log.v(TAG, "onInterrupt fired")
        isRunning = false
    }

    private fun isServiceEnabled(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getBoolean(SETTINGS_ENABLE_SERVICE, true)
    }

    private fun isMuteAdEnabled(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getBoolean(SETTINGS_MUTE_AUDIO, true)
    }

    private fun muteMedia() {
        if (isMuted) {
            return
        }

        if (!isMuteAdEnabled()) {
            return
        }

        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        }else {
            @Suppress("DEPRECATION")
            am.setStreamMute(AudioManager.STREAM_MUSIC, true)
        }

        Log.i(TAG, "STREAM_MUSIC muted.")
        isMuted = true
    }

    private fun unmuteMedia() {
        if (!isMuted) {
            return
        }

        if (!isMuteAdEnabled()) {
            return
        }

        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // am.setStreamMute(AudioManager.STREAM_MUSIC, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
        }else{
            @Suppress("DEPRECATION")
            am.setStreamMute(AudioManager.STREAM_MUSIC, false)
        }

        Log.i(TAG, "STREAM_MUSIC unmuted.")
        isMuted = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        //optional resource ids
        // com.google.android.youtube:id/ad_progress_text --> ad duration

        // required resource ids:
        // [visitar anunciante text]
        // com.google.android.youtube:id/player_learn_more_button --> class android.widget.TextView

        // [skip ad button]
        // com.google.android.youtube:id/skip_ad_button --> class android.widget.FrameLayout


        try {

            if (!isServiceEnabled()) {
                Log.i(TAG, "Service is not supposed to be enabled. Disabling it..")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Log.i(TAG, "Disabling it..")
                    // TODO: decide if it is a good idea do entirely disable service. It would require the user to always enable the accessibility service and might generate friction
                    //disableSelf()
                }
                return
            }

            Log.d(TAG,"packageName:${event?.packageName}")
            handleTelegram(event)
            /*
            val adLearnMoreElement = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(AD_LEARN_MORE_BUTTON_ID)?.getOrNull(0)
            val skipAdButton = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(SKIP_AD_BUTTON_ID)?.getOrNull(0)
            val adProgressText = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(AD_PROGRESS_TEXT)?.getOrNull(0)
            val appPromoAdCTAOverlay = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(APP_PROMO_AD_CTA_OVERLAY)?.getOrNull(0)
            val adCountdown = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(AD_COUNTDOWN)?.getOrNull(0)

            if (adLearnMoreElement == null && skipAdButton == null && adProgressText == null && appPromoAdCTAOverlay == null && adCountdown == null) {
                unmuteMedia()
                Log.v(TAG, "No ads yet...")
                return
            }
            Log.i(TAG, "player_learn_more_button or skipAdButton or adProgressText are visible. Trying to skip ad...")

            muteMedia()

            if (skipAdButton == null) {
                Log.v(TAG, "skipAdButton is null... returning...")
                return
            }

            if (skipAdButton.isClickable) {
                Log.v(TAG, "skipAdButton is clickable! Trying to click it...")
                skipAdButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.i(TAG, "Clicked skipAdButton!")
            }

             */

        } catch (error: Exception) {

            Log.e(TAG, "Something went wrong...")
            Log.e(TAG, error.message.toString())
            error.printStackTrace()
        }
    }

    private fun handleTelegram(event: AccessibilityEvent?) {
           // Log.d(TAG, "handleTelegram:${event?.source}")
        try {
            event?.let {
                if (event.packageName.equals(TELEGRAM_PACKAGE_NAME)) {
                    val currentNode = rootInActiveWindow
                    currentNode?.let {cNode->
                        Log.d(TAG, "handleTelegram:[CurrentNode]${cNode.className}")
                        if (cNode.className == "android.widget.FrameLayout") {

                            val mainNodeInfo = currentNode.getChild(0)

                            val bottomNodeInfo = mainNodeInfo.getChild(2)

                            val editTextNodeInfo = bottomNodeInfo.getChild(1)

                            editTextNodeInfo.apply {
                                performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                                val arguments = Bundle()
                                arguments.putString(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
                                performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, arguments)
                                performAction(AccessibilityNodeInfo.ACTION_PASTE)
                            }
//
//                            for (i in 0 until bottomNodeInfo.childCount)
//                            {
//                                Log.d(TAG,"[bottomNodeInfo$i]${bottomNodeInfo.getChild(i).className},Description:${bottomNodeInfo.getChild(i).contentDescription}")
//                            }

//                            bottomNodeInfo?.let {
//                                Log.d(TAG, "handleTelegram:[bottomNodeInfo]${it.className}")
//                                val editTextNode = it.getChild(1)
//                                Log.d(TAG, "handleTelegram:[editTextNode]${editTextNode.className}")
//                                val sendBtn = it.getChild(2)
//                                Log.d(TAG, "handleTelegram:[sendBtn]${sendBtn.className}")
//                                if (editTextNode != null && editTextNode.className == "android.widget.EditText") {
//
//                                    editTextNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
//                                }
//
//                                if (sendBtn != null && sendBtn.contentDescription == "Send") {
//                                    sendBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                                }
//
//                            }
                        }
                    }
                }
            }
        }catch (e:java.lang.Exception)
        {
            Log.d(TAG, "handleTelegram:${e.message}")
        }
    }

    override fun onServiceConnected() {
        Log.v(TAG, "accessibility onServiceConnected(). Ad skipping service connected.")
        isRunning = true
    }

}