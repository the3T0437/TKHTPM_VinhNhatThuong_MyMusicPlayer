package com.musicapp.mymusicplayer.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.model.SongPlayList

@Database(
    entities = [Song::class, PlayList::class, SongPlayList::class],
    version = MyRoomDatabaseSongPlayList.DB_VERSION
)
abstract class MyRoomDatabaseSongPlayList : RoomDatabase() {

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "my_database_song_playlist"

        @Volatile
        private var DB_INSTANCE: MyRoomDatabaseSongPlayList? = null

        fun getDatabase(context: Context): MyRoomDatabaseSongPlayList {
            return DB_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyRoomDatabaseSongPlayList::class.java,
                    DB_NAME
                ).build()
                DB_INSTANCE = instance
                instance
            }
        }
    }

    abstract fun songPlayListDao(): SongPlayListDAO
    abstract fun songDao(): SongDAO
    abstract fun playListDao(): PlayListDAO
}