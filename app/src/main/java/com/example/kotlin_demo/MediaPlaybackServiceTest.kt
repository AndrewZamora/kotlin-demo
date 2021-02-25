package com.example.kotlin_demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import java.util.prefs.NodeChangeListener


class MediaPlaybackServiceTest: MediaBrowserServiceCompat(){
    private  var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private var mediaPlayer : MediaPlayer? = null
    private var audioManager : AudioManager? = null
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(mediaPlayer?.isPlaying == true) {
               mediaPlayer?.pause()
            }
        }
    }

    fun sucessfullyGetAudioFocus (){
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

    }


    init {
        Log.d(SERVICE_TAG, "MediaPlaybackServiceTest service is running")
    }


    override fun onCreate() {
        Log.d(SERVICE_TAG, "OnCreate")
        super.onCreate()
        mediaSession = MediaSessionCompat(this@MediaPlaybackServiceTest, "MediaPlaybackServiceTest").apply {
            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())
            // MySessionCallback() has methods that handle callbacks from a media controller

            setCallback(MySessionCallback())
            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SERVICE_TAG, "OnStartCommand")
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
           val URL = "test"
            setDataSource(this@MediaPlaybackServiceTest, Uri.parse(URL))
            prepare()
            start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(SERVICE_TAG, "Stopped Session")
    }

    inner class MySessionCallback() : MediaSessionCompat.Callback(){
        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onPlay () {

            super.onPlay()
        }

        override fun onStop() {
            super.onStop()
        }

        override fun onPause() {
            super.onPause()
        }
    }
    companion object {
        const val SERVICE_TAG = "MediaPlaybackServiceTest"
    }
}


