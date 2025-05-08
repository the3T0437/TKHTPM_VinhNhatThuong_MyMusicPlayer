package com.musicapp.mymusicplayer.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Query


@Entity(
    tableName = Song.TABLE_NAME,
    foreignKeys = [
        ForeignKey(entity = Artist::class, parentColumns = [Artist.ID], childColumns = [Song.ARTIST])
    ]
)
class Song {
    // Thuộc tính tĩnh của lớp sẽ không được lưu và database không cần phải @Ignore cho chúng
    companion object {
        const val TABLE_NAME = "song"
        const val ID = "id"
        const val TITLE = "title"
        const val ARTIST = "artist_id"
        const val ALBUM = "album"
        const val LYRICS = "lyrics"
        const val RELEASE_DATE = "release_date"
        const val PLAYED_TIME = "played_time"
    }

    @PrimaryKey
    @ColumnInfo(name = ID)
    var id:Long = 0
    @ColumnInfo(name = TITLE)
    var title: String = ""
    lateinit var artist: String
    @ColumnInfo(name = ARTIST)
    var artistId: Long? = null
    @ColumnInfo(name = ALBUM)
    var album: String? = null
    @ColumnInfo(name = LYRICS)
    var lyrics: String = ""
    @ColumnInfo(name = RELEASE_DATE)
    var release_year : Int? = null
    @ColumnInfo(name = PLAYED_TIME)
    var played_time: Int = 0

    fun getUri(): Uri {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, this.id)
    }

    // Constructor
    constructor(id:Long, title: String, artist:String?, album: String?, lyrics: String, release_year :Int?, played_time:Int) {
        this.id = id
        this.title = title
        this.artist = artist ?: "<unknown>"
        this.album = album ?: "<unknown>"
        this.lyrics = lyrics
        this.release_year = release_year
        this.played_time = played_time
    }

    override fun toString(): String {
        return "$id $title ArtistId: $artistId"
    }
    constructor()
}
