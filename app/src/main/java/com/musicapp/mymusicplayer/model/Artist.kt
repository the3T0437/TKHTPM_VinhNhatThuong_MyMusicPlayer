package com.musicapp.mymusicplayer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = Artist.TABLE_NAME,
    indices = [Index(value = [Artist.ARTIST_NAME], unique = true)]
)
class Artist {
    companion object{
        const val TABLE_NAME = "artist"
        const val ID = "id"
        const val ARTIST_NAME= "artist_name"
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Artist.ID)
    var id: Long= 0
    @ColumnInfo(name = Artist.ARTIST_NAME)
    var artistName: String

    constructor(artistName: String){
        this.artistName = artistName
    }
}