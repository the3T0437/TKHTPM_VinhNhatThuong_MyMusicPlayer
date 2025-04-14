package com.musicapp.mymusicplayer.utils

import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import com.musicapp.mymusicplayer.model.Song

object store {
    var mediaBrowser: MediaBrowser? = null
    val songs: ArrayList<Song> = arrayListOf()
}