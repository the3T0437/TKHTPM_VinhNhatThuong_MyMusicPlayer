package com.musicapp.mymusicplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musicapp.mymusicplayer.model.FavoriteSong
import com.musicapp.mymusicplayer.model.Song

@Dao
interface SongDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun themSong(song:Song):Long
    @Query("SELECT * FROM ${Song.TABLE_NAME} order by ${Song.ID} ASC")
    fun getAllSongs():List<Song>
    @Query("SELECT * FROM ${Song.TABLE_NAME} WHERE ${Song.ID} = :id")
    fun getSong(id: Long):Song?
    @Update
    fun updateSong(song: Song):Int
    @Query("DELETE FROM ${Song.TABLE_NAME} WHERE ${Song.ID} = :songId")
    fun deleteSong(songId: Long): Int

    @Query("SELECT * FROM ${Song.TABLE_NAME} WHERE ${Song.TITLE} LIKE '%' || :title || '%'")
    fun searchSongs(title: String): List<Song>

    @Query("SELECT * FROM song WHERE album = (SELECT album FROM song WHERE id = :songId) AND id != :songId ORDER BY RANDOM() LIMIT 10")
    fun getRelatedSongs(songId: Long): List<Song>
}