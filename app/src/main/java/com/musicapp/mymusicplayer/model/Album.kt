package com.musicapp.mymusicplayer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = Album.TABLE_NAME,
    indices = [Index(value = [Album.ALBUM_NAME], unique = true)]
)
class Album {
    companion object{
        const val TABLE_NAME = "album"
        const val ID = "id"
        const val ALBUM_NAME = "album_name"
        const val RELEASE_DATE = "release_date"
    }
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Album.ID)
    var id: Long= 0
    @ColumnInfo(name = Album.ALBUM_NAME)
    var albumName: String
    @ColumnInfo(name = Album.RELEASE_DATE)
    var releaseDate: String

    constructor(albumName: String){
        this.albumName = albumName
        this.releaseDate = ""
    }
}