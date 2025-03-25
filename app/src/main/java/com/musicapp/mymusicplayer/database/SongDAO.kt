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
    @Update
    suspend fun capNhatSong(song: Song):Int
}