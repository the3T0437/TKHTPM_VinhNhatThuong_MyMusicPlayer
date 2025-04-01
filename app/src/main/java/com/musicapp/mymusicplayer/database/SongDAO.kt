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
    suspend fun themSong(song:Song):Long
    @Query("SELECT * FROM ${Song.TABLE_NAME} order by ${Song.ID} ASC")
    suspend fun getAllSongs():List<Song>
    @Query("SELECT * FROM ${Song.TABLE_NAME} WHERE ${Song.ID} = :id")
    suspend fun getSong(id: Long):Song?
    @Update
    suspend fun updateSong(song: Song):Int
    @Query("DELETE FROM ${Song.TABLE_NAME} WHERE ${Song.ID} = :songId")
    suspend fun deleteSong(songId: Long): Int
}