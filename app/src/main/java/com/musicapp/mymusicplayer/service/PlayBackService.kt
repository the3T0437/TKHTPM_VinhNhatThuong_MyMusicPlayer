package com.musicapp.mymusicplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import androidx.media3.session.SessionCommand
import com.google.common.collect.ImmutableList
import com.musicapp.mymusicplayer.R

class PlayBackService: MediaSessionService() {
    companion object{
        private val SAVE_TO_FAVORITES = "saveToFavorites"
    }
    private var mediaSession: MediaSession? = null
    /**
     * This method is called when the service is being created.
     * It initializes the ExoPlayer and MediaSession instances.
     */
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate() // Call the superclass method

        // Create an ExoPlayer instance
        val player = ExoPlayer.Builder(this).build()

        val likeButton = CommandButton.Builder()
            .setDisplayName("Like")
            .setIconResId(R.drawable.baseline_favorite_24)
            .setSessionCommand(SessionCommand("like", Bundle()))
            .setEnabled(true)
            .build()
        val favoriteButton = CommandButton.Builder()
            .setDisplayName("Save to favorites")
            .setIconResId(R.drawable.baseline_favorite_24)
            .setSessionCommand(SessionCommand(SAVE_TO_FAVORITES, Bundle()))
            .build()

        // Create a MediaSession instance
        mediaSession = MediaSession.Builder(this, player)
            .setCustomLayout(arrayListOf(likeButton, favoriteButton, likeButton, favoriteButton))
            .build()

        mediaSession?.setCustomLayout(mutableListOf(likeButton, favoriteButton))
    }

    /**
     * This method is called when the system determines that the service is no longer used and is being removed.
     * It checks the player's state and if the player is not ready to play or there are no items in the media queue, it stops the service.
     *
     * @param rootIntent The original root Intent that was used to launch the task that is being removed.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        // Get the player from the media session
        val player = mediaSession!!.player

        // Check if the player is not ready to play or there are no items in the media queue
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Stop the service
            stopSelf()
        }
    }

    /**
     * This method is called when a MediaSession.ControllerInfo requests the MediaSession.
     * It returns the current MediaSession instance.
     *
     * @param controllerInfo The MediaSession.ControllerInfo that is requesting the MediaSession.
     * @return The current MediaSession instance.
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    /**
     * This method is called when the service is being destroyed.
     * It releases the player and the MediaSession instances.
     */
    override fun onDestroy() {
        // If _mediaSession is not null, run the following block
        mediaSession?.run {
            // Release the player
            player.release()
            // Release the MediaSession instance
            release()
            // Set _mediaSession to null
            mediaSession = null
        }
        // Call the superclass method
        super.onDestroy()
    }

}