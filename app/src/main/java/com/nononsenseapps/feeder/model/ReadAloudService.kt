package com.nononsenseapps.feeder.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.MainActivity
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import org.kodein.di.direct
import org.kodein.di.instance

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class ReadAloudService : MediaSessionService() {
    private var session: MediaSession? = null
    private val holder: TTSStateHolder by lazy { (application as FeederApplication).di.direct.instance() }

    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider
                .Builder(this)
                .setNotificationId(NOTIFICATION_ID)
                .setChannelId(CHANNEL_ID)
                .setChannelName(R.string.read_article)
                .build()
                .apply { setSmallIcon(R.drawable.ic_stat_f) },
        )
        val player =
            ExoPlayer.Builder(this).build().apply {
                setSeekBackIncrementMs(10_000)
                setSeekForwardIncrementMs(10_000)
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                        .build(),
                    true,
                )
                setHandleAudioBecomingNoisy(true)
                setWakeMode(C.WAKE_MODE_LOCAL)
            }
        val activity = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        session =
            MediaSession
                .Builder(this, player)
                .setSessionActivity(activity)
                .setMediaButtonPreferences(
                    listOf(
                        CommandButton
                            .Builder(CommandButton.ICON_SKIP_BACK_10)
                            .setPlayerCommand(Player.COMMAND_SEEK_BACK)
                            .setDisplayName(getString(R.string.skip_back_10))
                            .setSlots(CommandButton.SLOT_BACK)
                            .build(),
                        CommandButton
                            .Builder(CommandButton.ICON_SKIP_FORWARD_10)
                            .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
                            .setDisplayName(getString(R.string.skip_forward_10))
                            .setSlots(CommandButton.SLOT_FORWARD)
                            .build(),
                    ),
                ).setCallback(
                    object : MediaSession.Callback {
                        override fun onConnect(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo,
                        ): MediaSession.ConnectionResult {
                            val defaults = super.onConnect(session, controller)
                            // External controls can operate this article, not replace its local audio.
                            val commands =
                                defaults.availablePlayerCommands
                                    .buildUpon()
                                    .remove(Player.COMMAND_SET_MEDIA_ITEM)
                                    .remove(Player.COMMAND_CHANGE_MEDIA_ITEMS)
                                    .build()
                            return MediaSession.ConnectionResult
                                .AcceptedResultBuilder(session)
                                .setAvailablePlayerCommands(commands)
                                .build()
                        }
                    },
                ).build()
        holder.attach(this, player)
        // The Compose UI uses the application facade, not a bound MediaController.
        addSession(checkNotNull(session))
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_PREPARE) {
            showPreparationNotification()
            holder.prepare()
        } else if (intent?.action == ACTION_STOP) {
            holder.stop()
        }
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onUpdateNotification(
        session: MediaSession,
        startInForegroundRequired: Boolean,
    ) {
        // Clearing the previous playlist must not demote an active synthesis job.
        if (holder.progress.value.preparationProgress != null) {
            showPreparationNotification()
        } else {
            super.onUpdateNotification(session, startInForegroundRequired)
        }
    }

    private fun showPreparationNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, getString(R.string.read_article), NotificationManager.IMPORTANCE_LOW))
        val stop = PendingIntent.getService(this, 1, Intent(this, ReadAloudService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
        startForeground(
            NOTIFICATION_ID,
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_f)
                .setContentTitle(getString(R.string.read_article))
                .setContentText(getString(R.string.read_aloud_preparing))
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause, getString(R.string.stop_reading), stop)
                .build(),
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    internal fun setArticle(articleId: Long) {
        val intent =
            Intent(this, MainActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData("$DEEP_LINK_BASE_URI/article/$articleId".toUri())
        session?.setSessionActivity(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
    }

    override fun onDestroy() {
        holder.detach()
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_PREPARE = "com.nononsenseapps.feeder.READ_ALOUD_PREPARE"
        private const val ACTION_STOP = "com.nononsenseapps.feeder.READ_ALOUD_STOP"
        private const val CHANNEL_ID = "read-aloud-preparation"
        private const val NOTIFICATION_ID = 2401
    }
}
