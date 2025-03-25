package com.musicapp.mymusicplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.musicapp.mymusicplayer.model.PlayList

@Database(entities = [PlayList::class], version = MyRoomDatabasePlayList.DB_VERSION)

abstract class MyRoomDatabasePlayList : RoomDatabase(){
    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "my_database_playlist"

        @Volatile
        private var DB_INSTANCE:MyRoomDatabasePlayList? = null

        // Chỉ một luồng có thể gọi lệnh tạo mới CSDL (Tạo 1 lần duy nhất)
        fun getDatabase(context: Context):MyRoomDatabasePlayList {
            return DB_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, MyRoomDatabasePlayList::class.java, DB_NAME).build()
                DB_INSTANCE = instance
                instance
            }
        }
    }
    // 2. Định nghĩa đối tượng DAO truy xuất CSDL
    abstract fun playListDao(): PlayListDAO
}