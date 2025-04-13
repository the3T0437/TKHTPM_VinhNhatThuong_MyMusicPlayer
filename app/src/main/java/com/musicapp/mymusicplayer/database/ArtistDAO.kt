package com.musicapp.mymusicplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musicapp.mymusicplayer.model.Artist

@Dao
interface ArtistDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertArtist(artist: Artist):Long

    @Query("SELECT * FROM ${Artist.TABLE_NAME} ORDER BY ${Artist.ARTIST_NAME} ASC")
    fun getAllArtists():List<Artist>

    @Query("SELECT * FROM ${Artist.TABLE_NAME} WHERE ${Artist.ID} = :id")
    suspend fun getArtist(id: Long):Artist?

    @Query("SELECT * FROM ${Artist.TABLE_NAME} WHERE ${Artist.ARTIST_NAME} = :name")
    fun getArtist(name: String):Artist?

    @Update
    suspend fun updateArtist(artist: Artist):Int

    @Query("DELETE FROM ${Artist.TABLE_NAME} WHERE ${Artist.ID} = :artistID")
    suspend fun deleteSong(artistID: Long): Int
}