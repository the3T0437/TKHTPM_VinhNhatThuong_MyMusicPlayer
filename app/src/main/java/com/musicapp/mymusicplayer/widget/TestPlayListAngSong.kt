package com.musicapp.mymusicplayer.test

import android.content.Context
import com.musicapp.mymusicplayer.database.DatabasePlayListAPI
import com.musicapp.mymusicplayer.database.DataBaseSongAPI
import com.musicapp.mymusicplayer.database.OnDatabasePlayListAPICallback
import com.musicapp.mymusicplayer.database.OnDatabaseSongCallback
import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.model.Song
import kotlinx.coroutines.runBlocking
import java.util.Date

class TestPlayListAndSong(context: Context) {
    private val databasePlayListAPI = DatabasePlayListAPI(context)
    private val databaseSongAPI = DataBaseSongAPI(context)

    // Dữ liệu giả cho PlayList
    private val playLists = listOf(
        PlayList(id = 1, name = "Pop Hits"),
        PlayList(id = 2, name = "Relaxing Music"),
        PlayList(id = 3, name = "Workout Beats")
    )

    // Dữ liệu giả cho Song
    private val songs = listOf(
        Song(
            id = 101,
            url = "https://example.com/song1.mp3",
            title = "Shape of You",
            artist = 1,
            album = 1,
            lyrics = "Lyrics 1",
            release_date = "123",
            played_time = 150,
            favorite = 1
        ),
        Song(
            id = 102,
            url = "https://example.com/song2.mp3",
            title = "Blinding Lights",
            artist = 2,
            album = 2,
            lyrics = "Lyrics 2",
            release_date = "123",
            played_time = 200,
            favorite = 1
        ),
        Song(
            id = 103,
            url = "https://example.com/song3.mp3",
            title = "Levitating",
            artist = 3,
            album = 3,
            lyrics = "Lyrics 3",
            release_date = "123",
            played_time = 100,
            favorite = 0
        )
    )


    // Hàm kiểm tra thêm PlayList
    fun testThemPlayList() {
        playLists.forEach { playList ->
            runBlocking {
                databasePlayListAPI.themPlayList(playList, object : OnDatabasePlayListAPICallback {
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
                databaseSongAPI.themSong(song, object : OnDatabaseSongCallback {
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
            databasePlayListAPI.docPlayList(danhSachPlayList, object : OnDatabasePlayListAPICallback {
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
            databaseSongAPI.docSong(danhSachSong, object : OnDatabaseSongCallback {
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