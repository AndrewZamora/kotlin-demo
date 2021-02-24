package com.example.kotlin_demo

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log

class AudioPlayer: Service() {
    private val serviceTag = "AudioPlayer"

    init {
        Log.d(serviceTag, "AudioPlayer service is running")
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("ACTION")
        val url = intent?.getStringExtra("URL")
        action?.let {
            Log.d(serviceTag, "Current Action: $action")
        }
        if(action == "play" && url is String) {
            Player.play(this@AudioPlayer, url)
        }
        if(action == "pause") {
            Player.pause()
        }
        return START_STICKY
    }

    object Player {
        private var mediaPlayer : MediaPlayer? = null
         fun stop() {
            if(mediaPlayer != null) {
                mediaPlayer?.release()
                mediaPlayer = null
            }

        }
        fun play(context: Context, URL: String) {
            if(mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )

                    setDataSource(context, Uri.parse(URL))
                    prepare()
                    start()
                }
                mediaPlayer!!.setOnCompletionListener {
                    stop()
                }
            }
            mediaPlayer?.start()
        }
        fun pause() {
            mediaPlayer?.pause()
        }
        fun seek(seekAmount:Int) {
            val currentPosition = mediaPlayer?.currentPosition
            val duration = mediaPlayer?.duration
            val newPosition = currentPosition?.plus(seekAmount)
            if(mediaPlayer?.isPlaying == true && duration != currentPosition && newPosition != null){
                mediaPlayer?.seekTo(newPosition)
            }
        }
    }

    override fun onDestroy() {
        Player.stop()
        Log.d(serviceTag, "service is stopped")
        super.onDestroy()
    }
}