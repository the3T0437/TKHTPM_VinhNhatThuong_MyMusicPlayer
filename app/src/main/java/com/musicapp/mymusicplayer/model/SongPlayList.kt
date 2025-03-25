package com.musicapp.mymusicplayer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = SongPlayList.TABLE_NAME,
    foreignKeys = [
        ForeignKey(entity = PlayList::class, parentColumns = [PlayList.ID], childColumns = [SongPlayList.PLAY_LIST_ID]),
        ForeignKey(entity = Song::class, parentColumns = [Song.ID], childColumns = [SongPlayList.SONG_ID])
    ],
    primaryKeys = ["play_list_id", "song_id"]
)
class SongPlayList {
    companion object {
        const val TABLE_NAME = "song_play_list"
        const val PLAY_LIST_ID = "play_list_id"
        const val SONG_ID = "song_id"
    }

    @ColumnInfo(name = PLAY_LIST_ID)
    var playListId: Int = 0

    @ColumnInfo(name = SONG_ID)
    var songId: Int = 0

    // Constructor
    constructor(playListId: Int, songId: Int) {
        this.playListId = playListId
        this.songId = songId
    }

    constructor()
}
