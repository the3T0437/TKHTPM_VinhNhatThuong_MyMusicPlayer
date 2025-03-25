package com.musicapp.mymusicplayer.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.musicapp.mymusicplayer.database.Converters
import com.musicapp.mymusicplayer.model.Song

@Database(entities = [Song::class], version = MyRoomDatabaseSong.DB_VERSION)
abstract class MyRoomDatabaseSong : RoomDatabase(){
    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "my_database_song"

        @Volatile
        private var DB_INSTANCE:MyRoomDatabaseSong? = null

        // Chỉ một luồng có thể gọi lệnh tạo mới CSDL (Tạo 1 lần duy nhất)
        fun getDatabase(context: Context):MyRoomDatabaseSong {
            return DB_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, MyRoomDatabaseSong::class.java, DB_NAME).build()
                DB_INSTANCE = instance
                instance
            }
        }
    }
    // 2. Định nghĩa đối tượng DAO truy xuất CSDL
    abstract fun songDao(): SongDAO
}