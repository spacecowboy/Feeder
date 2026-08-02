package com.nononsenseapps.feeder.model

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.Immutable
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PodcastPlayerStateHolder(
    private val application: Application,
    private val coroutineScope: CoroutineScope,
) {
    private val _playerState = MutableStateFlow(PodcastPlayerState())
    val playerState: StateFlow<PodcastPlayerState> = _playerState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    fun playLink(link: String) {
        if (link.isBlank()) {
            return
        }

        releaseCurrentPlayer()
        _playerState.value =
            PodcastPlayerState(
                audioUrl = link,
                isVisible = true,
                isLoading = true,
            )

        initializePlayer(link)
    }

    fun play() {
        val player = mediaPlayer ?: return
        player.start()
        startProgressUpdates()
        _playerState.update { it.copy(isPlaying = true) }
    }

    fun pause() {
        mediaPlayer?.pause()
        stopProgressUpdates()
        _playerState.update { it.copy(isPlaying = false) }
    }

    fun stop() {
        releaseCurrentPlayer()
        _playerState.value = PodcastPlayerState()
    }

    fun seekBy(deltaMillis: Int) {
        val player = mediaPlayer ?: return
        val currentState = _playerState.value
        if (!currentState.canSeek) {
            return
        }

        val targetPosition = (player.currentPosition + deltaMillis).coerceIn(0, player.duration.coerceAtLeast(0))
        player.seekTo(targetPosition)
        _playerState.update { it.copy(positionMillis = targetPosition) }
    }

    fun seekTo(positionMillis: Int) {
        val player = mediaPlayer ?: return
        val currentState = _playerState.value
        if (!currentState.canSeek) {
            return
        }

        val targetPosition = positionMillis.coerceIn(0, player.duration.coerceAtLeast(0))
        player.seekTo(targetPosition)
        _playerState.update { it.copy(positionMillis = targetPosition) }
    }

    fun shutdown() {
        releaseCurrentPlayer()
    }

    private fun initializePlayer(link: String) {
        coroutineScope.launch {
            runCatching {
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build(),
                    )
                    setOnPreparedListener { player ->
                        player.start()
                        _playerState.update {
                            it.copy(
                                isLoading = false,
                                isBuffering = false,
                                isPlaying = true,
                                canSeek = player.duration > 0,
                                durationMillis = player.duration.coerceAtLeast(0),
                                errorMessage = null,
                            )
                        }
                        startProgressUpdates()
                    }
                    setOnCompletionListener { player ->
                        stopProgressUpdates()
                        _playerState.update {
                            it.copy(
                                isPlaying = false,
                                isBuffering = false,
                                positionMillis = player.duration.coerceAtLeast(0),
                            )
                        }
                    }
                    setOnInfoListener { _, what, _ ->
                        when (what) {
                            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                                _playerState.update { it.copy(isBuffering = true) }
                                true
                            }

                            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                                _playerState.update { it.copy(isBuffering = false) }
                                true
                            }

                            else -> false
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        logDebug(LOG_TAG, "Podcast playback failed: $what / $extra")
                        stopProgressUpdates()
                        _playerState.update {
                            it.copy(
                                isLoading = false,
                                isBuffering = false,
                                isPlaying = false,
                                errorMessage = application.getString(R.string.audio_playback_failed),
                            )
                        }
                        true
                    }

                    setDataSource(application, Uri.parse(link))
                    prepareAsync()
                }
            }.onSuccess { player ->
                mediaPlayer = player
            }.onFailure { error ->
                logDebug(LOG_TAG, "Failed to initialize podcast player: ${error.message}")
                _playerState.update {
                    it.copy(
                        isLoading = false,
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = application.getString(R.string.audio_playback_failed),
                    )
                }
            }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob =
            coroutineScope.launch {
                while (isActive) {
                    val player = mediaPlayer ?: break
                    _playerState.update {
                        it.copy(
                            positionMillis = player.currentPosition.coerceAtLeast(0),
                            durationMillis = player.duration.coerceAtLeast(0),
                        )
                    }
                    delay(500)
                }
            }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releaseCurrentPlayer() {
        stopProgressUpdates()
        mediaPlayer?.runCatching {
            stop()
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val LOG_TAG = "FEEDER_PODCAST"
    }
}

@Immutable
data class PodcastPlayerState(
    val audioUrl: String = "",
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val canSeek: Boolean = false,
    val durationMillis: Int = 0,
    val positionMillis: Int = 0,
    val errorMessage: String? = null,
)
