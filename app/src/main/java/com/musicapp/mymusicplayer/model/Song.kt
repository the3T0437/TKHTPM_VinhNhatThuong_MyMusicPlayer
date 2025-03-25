package com.musicapp.mymusicplayer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = Song.TABLE_NAME)
class Song {
    // Thuộc tính tĩnh của lớp sẽ không được lưu và database không cần phải @Ignore cho chúng
    companion object {
        const val TABLE_NAME = "song"
        const val ID = "id"
        const val URL = "url"
        const val TITLE = "title"
        const val ARTIST = "artist"
        const val ALBUM = "album"
        const val LYRICS = "lyrics"
        const val RELEASE_DATE = "release_date"
        const val PLAYED_TIME = "played_time"
        const val FAVORITE = "favorite"
    }


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id:Int = 0
    @ColumnInfo(name = URL)
    var url:String =""
    @ColumnInfo(name = TITLE)
    var title: String = ""
    @ColumnInfo(name = ARTIST)
    var artist: Int = 0
    @ColumnInfo(name = ALBUM)
    var album: Int = 0
    @ColumnInfo(name = LYRICS)
    var lyrics: String = ""
    @ColumnInfo(name = RELEASE_DATE)
    var release_date: String= ""
    @ColumnInfo(name = PLAYED_TIME)
    var played_time: Int = 0
    @ColumnInfo(name = FAVORITE)
    var favorite: Int = 0



    // Constructor
    constructor(id:Int, url:String, title: String, artist:Int, album :Int, lyrics: String, release_date:String, played_time:Int, favorite:Int) {
        this.id = id
        this.url = url
        this.title = title
        this.artist = artist
        this.album = album
        this.lyrics = lyrics
        this.release_date = release_date
        this.played_time = played_time
        this.favorite = favorite
    }
    constructor()
}