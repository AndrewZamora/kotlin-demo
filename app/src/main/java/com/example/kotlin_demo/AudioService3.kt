package com.example.kotlin_demo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class AudioService3: MediaBrowserServiceCompat() {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerPreparer: MediaSessionConnector.PlaybackPreparer
    private val nowPlayingChannelId: String = "kotlin demo"
    private val nowPlayingNotificationId: Int = 1
    private lateinit var mediaSession: MediaSessionCompat

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
        return BrowserRoot("root", rootHints)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val url = intent?.getStringExtra("URL")
        if(url != null) {
            val media = MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMediaId("12345")
                .setTag("podcast")
                .build()
            exoPlayer = SimpleExoPlayer.Builder(applicationContext).build().apply {
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setContentType(C.CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA).build(), true
                )
                setHandleAudioBecomingNoisy(true)
            }

            exoPlayer.setMediaItem(media)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 1, sessionIntent, 0)
            }

            mediaSession = MediaSessionCompat(this, "Audio Service").apply {
                setSessionActivity(sessionActivityPendingIntent)
                isActive = true
            }

            sessionToken = mediaSession.sessionToken

            notificationManager = NotificationManagerCompat.from(this)

            if(shouldCreateNotificationChannel(notificationManager)){
                createNotificationChannel(notificationManager)
            }

            sessionToken?.let {
                val playerNotificationManager = PlayerNotificationManager(
                    this,
                    nowPlayingChannelId,
                    nowPlayingNotificationId,
                    object : PlayerNotificationManager.MediaDescriptionAdapter {
                        val controller = MediaControllerCompat(this@AudioService3, it)

                        override fun getCurrentContentText(player: Player): CharSequence? {
                            return "TEST TEXT"
                        }

                        override fun getCurrentContentTitle(player: Player): CharSequence {
                            return "TEST TITLE"
                        }

                        override fun getCurrentLargeIcon(
                            player: Player,
                            callback: PlayerNotificationManager.BitmapCallback
                        ): Bitmap? {
                            return null
                        }

                        override fun createCurrentContentIntent(player: Player): PendingIntent? {
                            val intent = Intent(this@AudioService3, MainActivity::class.java)
                            return PendingIntent.getActivity(this@AudioService3, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        }

                    },
                    object : PlayerNotificationManager.NotificationListener {
                        override fun onNotificationPosted(
                            notificationId: Int,
                            notification: Notification,
                            ongoing: Boolean
                        ) {
                            if(ongoing) {
                                ContextCompat.startForegroundService(
                                    applicationContext,
                                    Intent(applicationContext, this@AudioService3::class.java)
                                )
                                startForeground(nowPlayingNotificationId,notification)
                            } else {
                                stopForeground(false)
                            }
                        }

                    }
                )
                MediaSessionConnection(this, ComponentName(this,this@AudioService3::class.java))
                playerNotificationManager.setPlayer(exoPlayer)
                playerNotificationManager.setMediaSessionToken(it)
            }
            MediaSessionConnector(mediaSession).also {
                it.setPlayer(exoPlayer)
            }
        }

        return START_STICKY
    }
    private fun notificationChannelExists(notificationManager: NotificationManagerCompat) = notificationManager.getNotificationChannel(nowPlayingChannelId) != null
    private fun shouldCreateNotificationChannel(notificationManager: NotificationManagerCompat) = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !notificationChannelExists(notificationManager)
    private fun createNotificationChannel(notificationManager: NotificationManagerCompat) {
        val notificationChannel = NotificationChannel(
            nowPlayingChannelId,
            "Now playing channel",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Now playing channel"
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }
}