package com.musicapp.mymusicplayer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = FavoriteSong.TABLE_NAME,
    foreignKeys = [
        ForeignKey(entity = Song::class, parentColumns = [Song.ID], childColumns = [FavoriteSong.songID])
    ],
    )
class FavoriteSong {
    companion object{
        const val TABLE_NAME = "favorite"
        const val ID = "id"
        const val songID = "song_id"
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = FavoriteSong.ID)
    var id: Int = 0
    @ColumnInfo(name = FavoriteSong.songID)
    var songId: Long

    constructor(songId: Long){
        this.songId = songId
    }
}