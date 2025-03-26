package com.musicapp.mymusicplayer.widget

import android.content.Context
import com.musicapp.mymusicplayer.test.TestPlayListAndSong

class test(context: Context) {
    private val testPlayListAndSong = TestPlayListAndSong(context)

    fun runTests() {
        println("=== Thêm PlayList ===")
        testPlayListAndSong.testThemPlayList()

        println("=== Thêm Song ===")
        testPlayListAndSong.testThemSong()

        println("=== Đọc PlayList ===")
        testPlayListAndSong.testDocPlayList()

        println("=== Đọc Song ===")
        testPlayListAndSong.testDocSong()
    }
}