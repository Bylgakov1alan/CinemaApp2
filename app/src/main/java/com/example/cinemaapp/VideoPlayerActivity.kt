package com.example.cinemaapp

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.media3.ui.PlayerView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

import androidx.media3.exoplayer.ExoPlayer


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
            Log.d("PlayerActivity", "Changed state to $stateString")
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            Log.e("PlayerActivity", "Playback error: ${error.message}")
            Toast.makeText(this@PlayerActivity, "Ошибка воспроизведения: ${error.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        // Скрыть Action Bar, если он есть
        supportActionBar?.hide()
        // Скрыть системные панели (статус-бар и навигационная панель)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        // Настройка слушателя видимости контроллеров
        val playerView = findViewById<PlayerView>(R.id.playerView)
        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            findViewById<ImageButton>(R.id.backButton).visibility = visibility
        })
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish() // Закрываем активность, возвращаемся к MovieDetailsFragment
        }

    }

    @UnstableApi
    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    @SuppressLint("UseKtx")
    @UnstableApi
    private fun initializePlayer() {
        val videoUrl = intent.getStringExtra("VIDEO_URL")
        if (videoUrl.isNullOrEmpty()) {
            Log.e("PlayerActivity", "Video URL is missing")
            finish() // Закрываем активность, если URL отсутствует
            return
        }
        val videoUri = Uri.parse(videoUrl)
        val playerView = findViewById<PlayerView>(R.id.playerView)

        player = ExoPlayer.Builder(this)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()
            .apply {
                playerView.player = this
                setMediaItem(MediaItem.fromUri(videoUri))
                playWhenReady = this@PlayerActivity.playWhenReady
                seekTo(playbackPosition)
                addListener(playbackStateListener)
                prepare()
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