package com.musicapp.mymusicplayer.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class PlayBackService: MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null
    private val callback = object: MediaLibrarySession.Callback{}

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }

        super.onDestroy()
    }
}