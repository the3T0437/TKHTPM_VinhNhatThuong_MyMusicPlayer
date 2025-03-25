package com.musicapp.mymusicplayer.database

import android.content.Context
import com.musicapp.mymusicplayer.model.SongPlayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface OnDatabaseSongPlayListCallback {
    fun onSuccess(id: Long)
    fun onFailure(e: Exception)
}

class DatabaseSongPlayListAPI(context: Context) {
    private val songPlayListDAO: SongPlayListDAO
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    init {
        songPlayListDAO = MyRoomDatabaseSongPlayList.getDatabase(context).songPlayListDao()
    }

    // 1. Thêm liên kết giữa Song và PlayList
    fun themSongPlayList(songPlayList: SongPlayList, callback: OnDatabaseSongPlayListCallback) {
        coroutineScope.launch {
            try {
                val id = songPlayListDAO.insertSongPlayList(songPlayList)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(id)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    // 2. Đọc tất cả liên kết giữa Song và PlayList
    fun docSongPlayList(danhSachLienKet: ArrayList<SongPlayList>, callback: OnDatabaseSongPlayListCallback) {
        coroutineScope.launch {
            try {
                val danhSachDocVe = songPlayListDAO.getSongPlayLists()
                danhSachLienKet.addAll(danhSachDocVe)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(danhSachDocVe.size.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    // 3. Xóa liên kết giữa Song và PlayList
    fun xoaSongPlayList(songPlayList: SongPlayList, callback: OnDatabaseSongPlayListCallback) {
        coroutineScope.launch {
            try {
                val rowsAffected = songPlayListDAO.deleteSongPlayList(songPlayList)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(rowsAffected.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
}