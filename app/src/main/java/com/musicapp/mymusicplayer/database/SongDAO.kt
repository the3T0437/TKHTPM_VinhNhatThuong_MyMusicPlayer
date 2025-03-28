package com.musicapp.mymusicplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musicapp.mymusicplayer.model.Song

@Dao
interface SongDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun themSong(song:Song):Long
    @Query("SELECT * FROM ${Song.TABLE_NAME} order by ${Song.ID} ASC")
    suspend fun docSong():List<Song>
    @Query("SELECT * FROM ${Song.TABLE_NAME} WHERE ${Song.ID} = :id")
    suspend fun getSong(id: Long):Song?
    @Update
    suspend fun capNhatSong(song: Song):Int

    @Query("SELECT * FROM song WHERE album = (SELECT album FROM song WHERE id = :songId) AND id != :songId ORDER BY RANDOM() LIMIT 10")
    suspend fun getRelatedSongs(songId: Long): List<Song>
}