package com.musicapp.mymusicplayer.database

import android.content.Context
import com.musicapp.mymusicplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Định nghĩa callback để thông báo cho nơi sử dụng tầng dữ liệu về tình trạng các thao tác
interface OnDatabaseSongCallback {
    fun onSuccess(id: Long)
    fun onFailure(e: Exception)
}

class DataBaseSongAPI(context: Context) {
    private var songDAO: SongDAO

    // Khởi tạo CSDL nếu chưa tồn tại và trả về đối tượng  cho các thao tác với CSDL
    init {
        songDAO = MyRoomDatabaseSong.getDatabase(context).songDao()
    }

    // Xử lý bất đồng bộ
    // Tạo một CoroutineScope với Job để quản lý vòng đời của coroutine
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Dinh nghia cac APIs cho tang tren
    //////////////////////////////////////////////////////////////////////////////////////////////

    // 1. Định nghia ham ghi du lieu nhan su
    fun themSong(song: Song, callback: OnDatabaseSongCallback) {
        // Mở một tiến trình mới cho việc truy xuất CSDL
        coroutineScope.launch {
            try {
                val id = songDAO.themSong(song)
                // Cập nhật id cho đối tượng nhân sự mới tạo trên giao diện
                if (id != -1L) {
                    song.id = id.toInt()
                }
                // Quay về main thread để gọi callback
                withContext(Dispatchers.Main) {
                    callback.onSuccess(id)
                }
            } catch (e: Exception) {
                // Quay về main thread để gọi callback onFailure khi có lỗi
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    // 2. Dinh nghia ham doc du lieu tu CSDL
    fun docSong(danhSachNhanSu:ArrayList<Song>, callback: OnDatabaseSongCallback) {
        // Mở một tiến trình mới cho việc truy xuất CSDL
        coroutineScope.launch {
            try {
                // Doc du lieu ve tu CSDL
                val danhsachDocVe = songDAO.docSong()
                // Dua toan bo nhan su doc ve tu CSDL vao tham bien danhSachNhanSu de truyen ra Listview
                danhSachNhanSu.addAll(danhsachDocVe)

                // Quay về main thread để gọi callback
                withContext(Dispatchers.Main) {
                    callback.onSuccess(danhsachDocVe.size.toLong())
                }
            } catch (e: Exception) {
                // Quay về main thread để gọi callback onFailure khi có lỗi
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
    // 3. Dinh nghia ham cap nhat lai du lieu
    fun capNhatSong(song: Song, callback: OnDatabaseSongCallback) {
        // Mở một tiến trình mới cho việc truy xuất CSDL
        coroutineScope.launch {
            try {
                val index = songDAO.capNhatSong(song)
                // Quay về main thread để gọi callback
                withContext(Dispatchers.Main) {
                    callback.onSuccess(index.toLong())
                }
            } catch (e: Exception) {
                // Quay về main thread để gọi callback onFailure khi có lỗi
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
}