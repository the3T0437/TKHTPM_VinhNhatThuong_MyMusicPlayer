package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.MainActivity
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.activities.AddPlayListActivity
import com.musicapp.mymusicplayer.activities.AllSongInPlayListActivity
import com.musicapp.mymusicplayer.activities.PlayingSongsActivity
import com.musicapp.mymusicplayer.activities.SongsOfArtistActivity
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.PlaylistMemberLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener

/**
 * Adapter để hiển thị danh sách các playlist
 * @param context Context của ứng dụng
 * @param playlists Danh sách các playlist cần hiển thị
 * @param songID ID của bài hát (nếu đang thêm bài hát vào playlist)
 * @param isFromAddPlayList Có phải đang ở màn hình thêm bài hát vào playlist không
 */
class PlayListAdapter(
    private val context: Context, 
    private val playlists: MutableList<PlayList>, 
    private val songID: Long,
    private val isFromAddPlayList: Boolean
): RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {
    private var databaseAPI: DatabaseAPI = DatabaseAPI(context)

    /**
     * Interface để xử lý callback khi thêm bài hát vào playlist thành công
     */
    interface OnSongAddedListener {
        fun onSongAdded(playlistId: Int)
    }
    private var onSongAddedListener: OnSongAddedListener? = null

    /**
     * Set listener để xử lý khi thêm bài hát vào playlist
     */
    fun setOnSongAddedListener(listener: OnSongAddedListener) {
        this.onSongAddedListener = listener
    }

    /**
     * ViewHolder để hiển thị thông tin của một playlist
     */
    inner class ViewHolder(val binding: PlaylistMemberLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(playlist: PlayList) {
            // Set menu cho nút 3 chấm
            binding.optionPlayList.setMenuResource(R.menu.menu_playlist_options)
            binding.optionPlayList.setThreeDotMenuListener(object : ThreeDotMenuListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.remove -> {
                            // Hiển thị dialog xác nhận xóa playlist
                            val dialogBuilder = android.app.AlertDialog.Builder(context)
                            dialogBuilder.setTitle("Xóa Playlist")
                            dialogBuilder.setMessage("Bạn có muốn xóa?")
                            dialogBuilder.setPositiveButton("Ok") { _, _ ->
                                val position = bindingAdapterPosition
                                if (position != RecyclerView.NO_POSITION) {
                                    val playlistId = playlist.id.toLong()
                                    // Xóa playlist
                                    databaseAPI.deletePlayList(playlistId, object : OnDatabaseCallBack {
                                        override fun onSuccess(id: Long) {
                                            Toast.makeText(context, "Đã xóa Playlist", Toast.LENGTH_SHORT).show()
                                            playlists.removeAt(position)
                                            notifyItemRemoved(position)
                                        }

                                        override fun onFailure(e: Exception) {
                                            Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                            }
                            dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            dialogBuilder.show()
                            return true
                        }
                        R.id.rename -> {
                            // TODO: Implement rename functionality
                            return true
                        }
                    }
                    return false
                }
            })

            // Xử lý sự kiện click vào playlist
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (isFromAddPlayList) {
                        // Nếu đang thêm bài hát vào playlist
                        databaseAPI.themSongPlayList(songID, playlist.id, object : OnDatabaseCallBack {
                            override fun onSuccess(id: Long) {
                                Toast.makeText(context, "Đã thêm vào playlist", Toast.LENGTH_SHORT).show()
                                onSongAddedListener?.onSongAdded(playlist.id)
                                if (context is android.app.Activity) {
                                    (context as android.app.Activity).finish()
                                }
                            }
                            override fun onFailure(e: Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        // Nếu đang xem danh sách playlist
                        android.util.Log.d("PlayListAdapter", "Open playlist id: ${playlist.id}, name: ${playlist.name}")
                        val intent = Intent(context, com.musicapp.mymusicplayer.activities.AllSongInPlayListActivity::class.java)
                        intent.putExtra("PLAYLIST_ID", playlist.id)
                        intent.putExtra("PLAYLIST_NAME", playlist.name)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlaylistMemberLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.binding.name.text = playlist.name
        holder.bind(playlist)
    }

    override fun getItemCount(): Int = playlists.size
}