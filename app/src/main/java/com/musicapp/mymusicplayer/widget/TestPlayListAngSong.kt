package com.musicapp.mymusicplayer.test

import android.content.Context
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.model.Song
import kotlinx.coroutines.runBlocking

class TestPlayListAndSong(context: Context) {
    val databaseAPI = DatabaseAPI(context)

    // Dữ liệu giả cho PlayList
    private val playLists = listOf(
        PlayList(id = 1, name = "Pop Hits"),
        PlayList(id = 2, name = "Relaxing Music"),
        PlayList(id = 3, name = "Workout Beats")
    )

    // Dữ liệu giả cho Song
    private val songs = listOf(
        Song(
            101,
            "Shape of You",
            "",
            "",
            "Lyrics 1",
            123,
            150
        ),
        Song(
            102,
            "Blinding Lights",
            "2",
            "2",
            "Lyrics 2",
            123,
            200,
        ),
        Song(
            103,
            "title 3",
            "3",
            "3",
            "Lyrics 3",
            123,
            200,
        )
    )


    // Hàm kiểm tra thêm PlayList
    fun testThemPlayList() {
        playLists.forEach { playList ->
            runBlocking {
                databaseAPI.themPlayList(playList, object : OnDatabaseCallBack {
                    override fun onSuccess(id: Long) {
                        println("Thêm PlayList thành công: $playList với ID = $id")
                    }

                    override fun onFailure(e: Exception) {
                        println("Thêm PlayList thất bại: ${e.message}")
                    }
                })
            }
        }
    }

    // Hàm kiểm tra thêm Song
    fun testThemSong() {
        songs.forEach { song ->
            runBlocking {
                databaseAPI.insertSong(song, object : OnDatabaseCallBack{
                    override fun onSuccess(id: Long) {
                        println("Thêm Song thành công: $song với ID = $id")
                    }

                    override fun onFailure(e: Exception) {
                        println("Thêm Song thất bại: ${e.message}")
                    }
                })
            }
        }
    }

    // Hàm kiểm tra đọc dữ liệu từ PlayList
    fun testDocPlayList() {
        val danhSachPlayList = ArrayList<PlayList>()
        runBlocking {
            databaseAPI.docPlayList(danhSachPlayList, object : OnDatabaseCallBack {
                override fun onSuccess(size: Long) {
                    println("Đọc PlayList thành công, tổng số: $size")
                    danhSachPlayList.forEach {
                        println("PlayList: ${it.name} (ID: ${it.id})")
                    }
                }

                override fun onFailure(e: Exception) {
                    println("Đọc PlayList thất bại: ${e.message}")
                }
            })
        }
    }

    // Hàm kiểm tra đọc dữ liệu từ Song
    fun testDocSong() {
        val danhSachSong = ArrayList<Song>()
        runBlocking {
            databaseAPI.getAllSongs(danhSachSong, object : OnDatabaseCallBack{
                override fun onSuccess(size: Long) {
                    println("Đọc Song thành công, tổng số: $size")
                    danhSachSong.forEach {
                        println("Song: ${it.title} (ID: ${it.id})")
                    }
                }

                override fun onFailure(e: Exception) {
                    println("Đọc Song thất bại: ${e.message}")
                }
            })
        }
    }
}