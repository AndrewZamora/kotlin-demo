package com.example.kotlin_demo
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.webkit.JavascriptInterface

class JSBridge(private val activity: MainActivity ) {
    private var mediaPlayer : MediaPlayer? = null
    @JavascriptInterface
    fun playAudioFromUrl(URL:String) {
        if(mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(activity, Uri.parse(URL))
            mediaPlayer!!.setOnCompletionListener {
                stopMediaPlayer()
            }
        }
        mediaPlayer?.start()
    }
    @JavascriptInterface
    fun pauseAudio() {
        mediaPlayer?.pause()
    }
    @JavascriptInterface
    fun stopAudio() {
        stopMediaPlayer()
    }
    @JavascriptInterface
    fun stopMediaPlayer() {
        if(mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}