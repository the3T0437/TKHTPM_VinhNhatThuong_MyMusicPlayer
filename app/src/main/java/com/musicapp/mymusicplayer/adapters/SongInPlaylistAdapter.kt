package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.view.MenuItem
import android.widget.Toast
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter để hiển thị danh sách bài hát trong một playlist
 * Kế thừa từ DragableSongAdapter để có thể kéo thả sắp xếp bài hát
 * @param context Context của ứng dụng
 * @param songs Danh sách bài hát trong playlist (ArrayList<Song> để cập nhật trực tiếp)
 * @param playlistId ID của playlist
 */
class SongInPlayListAdapter(
    context: Context,
    songs: ArrayList<Song>,
    private val playlistId: Int
) : DragableSongAdapter(context, songs, null) {

    /**
     * Interface để xử lý callback khi xóa bài hát khỏi playlist
     */
    interface OnSongDeletedListener {
        fun onSongDeleted(songId: Long)
    }
    private var onSongDeletedListener: OnSongDeletedListener? = null

    /**
     * Set listener để xử lý khi xóa bài hát khỏi playlist
     */
    fun setOnSongDeletedListener(listener: OnSongDeletedListener) {
        this.onSongDeletedListener = listener
    }

    /**
     * Lấy resource ID của menu 3 chấm
     * @return Resource ID của menu
     */
    override fun getMenuResource(): Int {
        return R.menu.playing_song_menu
    }

    /**
     * Tạo listener cho menu 3 chấm
     * @param holder ViewHolder của item
     * @param song Bài hát được chọn
     * @param position Vị trí của bài hát trong danh sách
     * @return ThreeDotMenuListener để xử lý các sự kiện của menu
     */
    override fun getThreeDotMenuListener(holder: ViewHolder, song: Song, position: Int): ThreeDotMenuListener {
        return object : ThreeDotMenuListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (item.itemId == R.id.menuRemove) {
                    removeSong(position, song.id)
                    return true
                }
                return false
            }
        }
    }

    /**
     * Xóa bài hát khỏi playlist
     * @param position Vị trí của bài hát trong danh sách
     * @param songID ID của bài hát cần xóa
     */
    private fun removeSong(position: Int, songID: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val databaseAPI = com.musicapp.mymusicplayer.database.DatabaseAPI(context)
                databaseAPI.deleteSongFromPlaylist(playlistId, songID, object : OnDatabaseCallBack {
                    override fun onSuccess(id: Long) {
                        launch(Dispatchers.Main) {
                            onSongDeletedListener?.onSongDeleted(songID)
                        }
                    }
                    override fun onFailure(e: Exception) {
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Không thể xóa bài hát", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }
    }
}