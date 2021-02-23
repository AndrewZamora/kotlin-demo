package com.example.kotlin_demo
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.webkit.JavascriptInterface

class JSBridge(private val activity: MainActivity ) {
    private var mediaPlayer : MediaPlayer? = null
    @JavascriptInterface
    fun playAudioFromUrl(URL:String) {
        Intent(activity, AudioPlayer::class.java).also {
            activity.startService(it)
        }
        if(mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            )
            setDataSource(activity, Uri.parse(URL))
            prepare()
            start()
            }
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

    private fun stopMediaPlayer() {
        if(mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}