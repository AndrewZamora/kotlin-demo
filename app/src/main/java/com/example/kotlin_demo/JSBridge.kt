package com.example.kotlin_demo
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView

class JSBridge(private val activity: MainActivity ) {
    @JavascriptInterface
    fun playAudioFromUrl(URL:String) {
//        Intent(activity,AudioIntentService::class.java).also { intent ->
//            intent.putExtra("URL", URL)
//            activity.startService(intent)
//        }
        Intent(activity, AudioService2::class.java).also {
            it.putExtra("URL", URL)
            it.putExtra("ACTION", "play")
            activity.startForegroundService(it)
        }
//        Intent(activity, MediaPlaybackServiceTest::class.java).also {
//            it.putExtra("URL", URL)
//            it.putExtra("ACTION", "play")
//            activity.startService(it)
//        }
//        Intent(activity, AudioPlayer::class.java).also {
//            it.putExtra("URL", URL)
//            it.putExtra("ACTION", "play")
//            activity.startService(it)
//        }
    }

    @JavascriptInterface
    fun pauseAudio() {
        Intent(activity, AudioPlayer::class.java).also {
            it.putExtra("ACTION", "pause")
            activity.startService(it)
        }
    }

    @JavascriptInterface
    fun stopAudio() {
        AudioIntentService.stopService()
//        Intent(activity, MediaPlaybackServiceTest::class.java).also {
//            it.putExtra("ACTION", "stop")
//            activity.stopService(it)
//        }
//        Intent(activity, AudioPlayer::class.java).also {
//            activity.stopService(it)
//        }
    }
}