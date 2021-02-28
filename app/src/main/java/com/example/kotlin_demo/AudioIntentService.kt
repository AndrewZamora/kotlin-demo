package com.example.kotlin_demo

import android.app.IntentService
import android.content.Intent
import android.util.Log

private val SERVICE_TAG = "AudioIntentService"
class AudioIntentService: IntentService("AudioIntentService") {
    init {
        instance = this
    }

    companion object {
        private lateinit var instance: AudioIntentService
        var isRunning = false
        fun stopService() {
            Log.d(SERVICE_TAG, "Is stopping")
            isRunning = false
            instance.stopSelf()
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        try {
            isRunning = true
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}