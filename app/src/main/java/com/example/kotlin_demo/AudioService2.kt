package com.example.kotlin_demo

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.webkit.WebView
import androidx.media.AudioAttributesCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

private const val SERVICE_TAG = "AudioService"
class AudioService2: MediaBrowserServiceCompat() {
    val PLAYBACK_CHANNEL_ID = "audio_channel"

    lateinit var player: SimpleExoPlayer

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var mediaController: MediaControllerCompat

    private fun initializePlayer(URL: String) {
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let{
            PendingIntent.getActivity(this, 0, it, 0)
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    Log.d(SERVICE_TAG, "onPlay Callback fired")
//            super.onPlay()
                }

                override fun onStop() {
                    super.onStop()
                }
            })
            isActive = true
        }
        mediaController = MediaControllerCompat(this, mediaSession)
        mediaController.registerCallback(mediaControllerCallback)
//        MediaControllerCompat.setMediaController(this, mediaController)
        player = SimpleExoPlayer.Builder(this).build()
        sessionToken = mediaSession.sessionToken
        val media = MediaItem.Builder()
            .setUri(Uri.parse(URL))
            .setMediaId("12345")
            .setTag("podcast")
            .build()
        player.setMediaItem(media)
        player.prepare()
        player.playWhenReady = true
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(player)
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this,
            PLAYBACK_CHANNEL_ID,
            R.string.notification_channel_name,
            R.string.notification_channel_description,
            1,
            object:PlayerNotificationManager.MediaDescriptionAdapter{
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = Intent(this@AudioService2, MainActivity::class.java)
                    return PendingIntent.getActivity(this@AudioService2, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                override fun getCurrentContentText(player: Player): CharSequence {
                    return "test"
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
            },
            object: PlayerNotificationManager.NotificationListener{
                override fun onNotificationStarted(
                    notificationId: Int,
                    notification: Notification
                ) {
                    Log.d(SERVICE_TAG, "notification started")
                    startForeground(notificationId,notification)
                }
                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    stopSelf()
                    Log.d(SERVICE_TAG, "notification cancel")
                    super.onNotificationCancelled(notificationId, dismissedByUser)
                }

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
//                    super.onNotificationPosted(notificationId, notification, ongoing)
                    if(ongoing) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(false)
                    }
//                    MediaSessionConnector.PlaybackPreparer.ACTIONS
                    Log.d(SERVICE_TAG, "notification posted")
                }
            }
        )

        playerNotificationManager.setSmallIcon(R.drawable.ic_launcher_foreground)
        playerNotificationManager.setPlayer(player)
        playerNotificationManager.setMediaSessionToken(sessionToken!!)
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(player)
        mediaSessionConnector.setQueueNavigator(object : TimelineQueueNavigator(mediaSession){
            override fun getMediaDescription(
                player: Player,
                windowIndex: Int
            ): MediaDescriptionCompat {
                return MediaDescriptionCompat.Builder()
                    .setTitle("test")
                    .build()
            }
        })

    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback(){
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            MainActivity.UpdateAudioState(state.toString())
            Log.d(SERVICE_TAG,"Playback State" + state.toString())
            super.onPlaybackStateChanged(state)
        }
    }

    // LIFECYCLE METHODS
    override fun onCreate() {
        Log.d(SERVICE_TAG, "has started running")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("URL")
        initializePlayer(url.toString())
        return START_STICKY
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        playerNotificationManager.setPlayer(null)
        mediaSession.release()
        mediaSessionConnector.setPlayer(null)
        player.release()
        serviceScope.cancel()
        mediaController.unregisterCallback(mediaControllerCallback)
        Log.d(SERVICE_TAG, "has stopped running")
        super.onDestroy()
    }
}