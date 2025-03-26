package com.musicapp.mymusicplayer.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.musicapp.mymusicplayer.model.FavoriteSong

import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.model.SongPlayList

@Database(
    entities = [Song::class, PlayList::class, SongPlayList::class, FavoriteSong::class],
    version = MyRoomDatabase.DB_VERSION
)
abstract class MyRoomDatabase : RoomDatabase() {

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "my_database_song_playlist"

        @Volatile
        private var DB_INSTANCE: MyRoomDatabase? = null

        fun getDatabase(context: Context): MyRoomDatabase {
            return DB_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyRoomDatabase::class.java,
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
    abstract fun favroiteSongDAO(): FavoriteSongDAO
}