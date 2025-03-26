package com.musicapp.mymusicplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.musicapp.mymusicplayer.model.FavoriteSong

@Dao
interface FavoriteSongDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavoriteSong(favoriteSong: FavoriteSong): Long
    @Query("SELECT * FROM ${FavoriteSong.TABLE_NAME} order by ${FavoriteSong.ID} ASC")
    suspend fun readAllFavoriteSongs():List<FavoriteSong>
    @Query("SELECT * FROM ${FavoriteSong.TABLE_NAME} WHERE ${FavoriteSong.songID} = :songID")
    fun readFavoriteSongFromId(songID: Long):FavoriteSong?
    @Query("DELETE FROM ${FavoriteSong.TABLE_NAME} WHERE ${FavoriteSong.ID} = :favoriteSongID")
    suspend fun deleteByUserId(favoriteSongID : Long): Int
}