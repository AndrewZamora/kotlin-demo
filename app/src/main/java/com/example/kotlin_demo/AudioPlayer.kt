package com.example.kotlin_demo

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class AudioPlayer: Service() {
    val serviceTag = "AudioPlayer"
    private var mediaPlayer : MediaPlayer? = null

    init {
        Log.d(serviceTag, "AudioPlayer service is running")
    }
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}