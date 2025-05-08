package com.musicapp.mymusicplayer.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.musicapp.mymusicplayer.database.MyRoomDatabase
import com.musicapp.mymusicplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface GetDataListener{
    suspend fun onGetData(arr: ArrayList<Song>)
    suspend fun onDone(arr: ArrayList<Song>)
}

object songGetter {
    private const val ID = MediaStore.Audio.Media._ID
    private const val TITLE = MediaStore.Audio.Media.TITLE
    private const val ARTIST = MediaStore.Audio.Media.ARTIST
    private const val PATH = MediaStore.Audio.Media.RELATIVE_PATH
    private const val ALBUM = MediaStore.Audio.Media.ALBUM
    private const val RELEASE_DATE = MediaStore.Audio.Media.YEAR
    val project = arrayOf(
        ID,
        TITLE,
        ARTIST,
        ALBUM,
        RELEASE_DATE
    )

    fun getAllSongs(context: Context, arr: ArrayList<Song>, callback: GetDataListener){
        CoroutineScope(Dispatchers.IO + Job()).launch {
            arr.clear()

            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val sort = "$TITLE ASC"
            val cursor: Cursor? = context.contentResolver.query(uri, project, null, null, sort)
            if (cursor == null){
                return@launch
            }

            val idColumn = cursor.getColumnIndexOrThrow(ID)
            val titleColumn = cursor.getColumnIndexOrThrow(TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(ALBUM)
            val releaseColumn = cursor.getColumnIndexOrThrow(RELEASE_DATE)

            cursor.use{
                while(it.moveToNext()){
                    val id = it.getLong(idColumn)
                    val title= it.getString(titleColumn)
                    val artist = it.getStringOrNull(artistColumn)
                    val album = it.getStringOrNull(albumColumn)
                    val releaseDate = it.getIntOrNull(releaseColumn)

                    val song = Song(id, title, artist, album, "", releaseDate, 0)
                    arr.add(song)
                }

                callback.onGetData(arr)
            }

            callback.onGetData(arr)

            cursor.close()
        }
    }


    suspend fun getAllSongs(context: Context): ArrayList<Song> = runBlocking(Dispatchers.IO + Job()){
            val arr = arrayListOf<Song>()

            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val sort = "$TITLE ASC"
            val cursor: Cursor? = context.contentResolver.query(uri, project, null, null, sort)
            if (cursor == null){
                return@runBlocking arr
            }

            val idColumn = cursor.getColumnIndexOrThrow(ID)
            val titleColumn = cursor.getColumnIndexOrThrow(TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(ALBUM)
            val releaseColumn = cursor.getColumnIndexOrThrow(RELEASE_DATE)

            cursor.use{
                while(it.moveToNext()){
                    val id = it.getLong(idColumn)
                    val title= it.getString(titleColumn)
                    val artist = it.getStringOrNull(artistColumn)
                    val album = it.getStringOrNull(albumColumn)
                    val releaseDate = it.getIntOrNull(releaseColumn)

                    val song = Song(id, title, artist, album, "", releaseDate, 0)
                    arr.add(song)
                }
            }

            cursor.close()

        return@runBlocking arr
    }

    fun getSong(context: Context, uri: Uri): Song?{
        val sort = "$TITLE ASC"
        val cursor: Cursor? = context.contentResolver.query(uri, project, null, null, sort)
        if (cursor == null){
            return null
        }

        val idColumn = cursor.getColumnIndexOrThrow(ID)
        val titleColumn = cursor.getColumnIndexOrThrow(TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(ARTIST)
        val albumColumn = cursor.getColumnIndexOrThrow(ALBUM)
        val releaseColumn = cursor.getColumnIndexOrThrow(RELEASE_DATE)

        var song: Song
        cursor.use{
            it.moveToFirst()
            val id = it.getLong(idColumn)
            val title= it.getString(titleColumn)
            val artist = it.getStringOrNull(artistColumn)
            val album = it.getStringOrNull(albumColumn)
            val releaseDate = it.getIntOrNull(releaseColumn)

            song = Song(id, title, artist, album, "", releaseDate, 0)
        }

        cursor.close()
        return song;
    }
}