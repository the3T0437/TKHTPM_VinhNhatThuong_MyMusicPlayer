package com.musicapp.mymusicplayer.utils

import androidx.media3.session.MediaController
import com.musicapp.mymusicplayer.model.Song

object store {
    var mediaController: MediaController? = null
    var playingSongs: ArrayList<Song>? = arrayListOf()
}