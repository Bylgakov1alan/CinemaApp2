package com.example.cinemaapp

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    // Для использования с версиями API < 24
    private val playbackStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED"
                else -> "UNKNOWN_STATE"
            }
            println("Changed state to $stateString")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
    }

    @UnstableApi
    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    @SuppressLint("UseKtx")
    @UnstableApi
    private fun initializePlayer() {
        // 1. Создаём экземпляр ExoPlayer
        player = ExoPlayer.Builder(this)
            .setSeekForwardIncrementMs(10000) // Перемотка вперёд на 10 сек
            .setSeekBackIncrementMs(10000)    // Перемотка назад на 10 сек
            .build()
            .also { exoPlayer ->
                // 2. Настраиваем PlayerView
                val playerView = findViewById<PlayerView>(R.id.playerView)
                playerView.player = exoPlayer

                // 3. Получаем URL видео из интента
                val videoUri = Uri.parse(intent.getStringExtra("VIDEO_URL"))

                // 4. Создаём MediaItem
                val mediaItem = MediaItem.fromUri(videoUri)

                // 5. Подготавливаем плеер
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentWindow = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }
}