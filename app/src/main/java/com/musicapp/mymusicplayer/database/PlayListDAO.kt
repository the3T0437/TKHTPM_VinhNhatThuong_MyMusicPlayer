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
    suspend fun themPlayList(playList: PlayList):Long
    @Query("SELECT * FROM ${PlayList.TABLE_NAME} order by ${PlayList.ID} ASC")
    suspend fun docPlayList():List<PlayList>
    @Update
    suspend fun capNhatPlayList(playList: PlayList):Int
}