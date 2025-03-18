package com.example.mymusicplayer.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val path: String,
) {
    fun getUri(): Uri{
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, this.id)
    }
}