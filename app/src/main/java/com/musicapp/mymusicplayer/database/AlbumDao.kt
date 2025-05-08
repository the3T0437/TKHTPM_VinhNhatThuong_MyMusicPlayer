package com.musicapp.mymusicplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.musicapp.mymusicplayer.model.Album

@Dao
interface AlbumDao {
    @Query("SELECT * FROM ${Album.TABLE_NAME} ORDER BY ${Album.ALBUM_NAME} ASC")
    fun getAllAlbums(): List<Album>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAlbum(album: Album)
}