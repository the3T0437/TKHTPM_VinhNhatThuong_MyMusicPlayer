package com.musicapp.mymusicplayer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = PlayList.TABLE_NAME)
class PlayList {
    // Thuộc tính tĩnh của lớp sẽ không được lưu và database không cần phải @Ignore cho chúng
    companion object {
        const val TABLE_NAME = "play_list"
        const val ID = "id"
        const val NAME = "name"
    }


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id:Int = 0
    @ColumnInfo(name = NAME)
    var name:String =""

    // Constructor
    constructor(name:String) {
        this.name = name
    }
    constructor()
}