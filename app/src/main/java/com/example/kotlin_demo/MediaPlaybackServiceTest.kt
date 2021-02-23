package com.example.kotlin_demo

import android.app.Activity
import android.graphics.ColorSpace
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat


class MediaPlaybackServiceTest: MediaBrowserServiceCompat() {
    private  var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private var mediaPlayer : MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(baseContext, "Audio Service").apply {
            // Enable callbacks from MediaButtons and TransportControls
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

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
    
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
       return BrowserRoot("empty_root", null)
    }

    override fun onDestroy() {
        mediaSession?.release()
        super.onDestroy()
    }
    private fun stopMediaPlayer() {
        if(mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    private inner class MySessionCallback: MediaSessionCompat.Callback() {
         fun onPlay(activity: Activity, URL: String) {
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

        override fun onSkipToQueueItem(queueId: Long) {}

        override fun onSeekTo(position: Long) {}

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {}

        override fun onPause() {}

        override fun onStop() {}

        override fun onSkipToNext() {}

        override fun onSkipToPrevious() {}

        override fun onCustomAction(action: String?, extras: Bundle?) {}

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {}
    }
}


