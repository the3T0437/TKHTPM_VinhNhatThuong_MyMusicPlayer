package com.example.mymusicplayer.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import com.example.mymusicplayer.model.Song

object songGetter {
    private const val ID = MediaStore.Audio.Media._ID
    private const val TITLE = MediaStore.Audio.Media.TITLE
    private const val ARTIST = MediaStore.Audio.Media.ARTIST
    private const val PATH = MediaStore.Audio.Media.RELATIVE_PATH
    private const val ALBUM = MediaStore.Audio.Media.ALBUM

    fun getAllSongs(context: Context, arr: ArrayList<Song>){
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val project = arrayOf(
            ID,
            TITLE,
            ARTIST,
            PATH,
            ALBUM
        )

        val sort = "${TITLE} ASC"
        val cursor: Cursor? = context.contentResolver.query(uri, project, null, null, sort)
        if (cursor == null){
            return
        }

        val idColumn = cursor.getColumnIndexOrThrow(ID)
        val titleColumn = cursor.getColumnIndexOrThrow(TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(ARTIST)
        val pathColumn = cursor.getColumnIndexOrThrow(PATH)
        val albumColumn = cursor.getColumnIndexOrThrow(ALBUM)

        cursor.use{
            while(it.moveToNext()){
                val id = it.getLong(idColumn)
                val title= it.getString(titleColumn)
                val artist = it.getStringOrNull(artistColumn)
                val path = it.getString(pathColumn)
                val album = it.getStringOrNull(albumColumn)

                val song = Song(id, title, artist, album, path)
                arr.add(song)
            }
        }

        cursor.close()
    }
}