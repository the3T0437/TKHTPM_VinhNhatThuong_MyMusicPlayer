package com.musicapp.mymusicplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musicapp.mymusicplayer.model.PlayList

@Dao
interface PlayListDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayList(playList: PlayList):Long
    @Query("SELECT * FROM ${PlayList.TABLE_NAME} order by ${PlayList.ID} ASC")
    suspend fun getAllPlaylists():List<PlayList>
    @Update
    suspend fun updatePlayList(playList: PlayList):Int

    @Query("DELETE FROM ${PlayList.TABLE_NAME} WHERE ${PlayList.ID} = :id")
    suspend fun deletePlaylist(id: Long): Int

}