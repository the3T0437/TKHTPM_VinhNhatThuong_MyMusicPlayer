package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.activities.AllSongInPlayListActivity
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.PlaylistMemberLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener


class PlayListAdapter(private val context: Context, private val arrs: List<PlayList>, private val songID : Long,private val isFromAddPlayList: Boolean):
RecyclerView.Adapter<PlayListAdapter.ViewHolder>(){
    private  var databaseAPI: DatabaseAPI = DatabaseAPI(context)

        inner class ViewHolder(val binding: PlaylistMemberLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                binding.optionPlayList.setMenuResource(R.menu.menu_playlist_options)
                binding.optionPlayList.setThreeDotMenuListener(object : ThreeDotMenuListener {
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        when (item.itemId) {
                            R.id.remove -> {
                                val dialogBuilder = android.app.AlertDialog.Builder(context)
                                dialogBuilder.setTitle("Xóa Playlist")
                                dialogBuilder.setMessage("Bạn có muốn xóa?")
                                dialogBuilder.setPositiveButton("Ok") { _, _ ->
                                    val position = bindingAdapterPosition
                                    if (position != RecyclerView.NO_POSITION) {
                                        val playlist = arrs[position]
                                        val playlistID = playlist.id.toLong()
                                        databaseAPI.deletePlayList(playlistID, object : OnDatabaseCallBack {
                                            override fun onSuccess(id: Long) {
                                                Toast.makeText(context, "Đã xóa Playlist", Toast.LENGTH_SHORT).show()
                                                (arrs as MutableList).removeAt(position)
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
                                // Hiển thị Dialog để nhập tên mới
                                val dialogBuilder = android.app.AlertDialog.Builder(context)
                                dialogBuilder.setTitle("Đổi tên Playlist")
                                dialogBuilder.setMessage("Nhập tên mới:")

                                val input = android.widget.EditText(context)
                                input.hint = "Tên mới"
                                dialogBuilder.setView(input)

                                dialogBuilder.setPositiveButton("OK") { _, _ ->
                                    val newName = input.text.toString()
                                    if (newName.isNotEmpty()) {
                                        val playlist = arrs[adapterPosition]
                                        playlist.name = newName
                                        databaseAPI.capNhatPlayList(
                                            playlist,
                                            object : OnDatabaseCallBack {
                                                override fun onSuccess(id: Long) {
                                                    Toast.makeText(
                                                        context,
                                                        "Tên Playlist đã được cập nhật",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                override fun onFailure(e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "Cập nhật thất bại",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                        notifyItemChanged(adapterPosition) // Cập nhật giao diện
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Tên không được để trống",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                dialogBuilder.setNegativeButton("Hủy") { dialog, _ ->
                                    dialog.dismiss()
                                }

                                dialogBuilder.show()
                                return true
                            }
                        }
                        return false
                    }
                })
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: PlaylistMemberLayoutBinding = PlaylistMemberLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        holder.binding.name.text = arrs[position].name
        if(isFromAddPlayList == true){
         holder.itemView.setOnClickListener{
             if(songID != -1L){
                 databaseAPI.themSongPlayList(songID,arrs[position].id, object :
                     OnDatabaseCallBack {
                     override fun onSuccess(id: Long) {
                         Toast.makeText(
                             context,
                             "Đã thêm bài hát vào playlist",
                             Toast.LENGTH_SHORT
                         ).show()
                         (context as android.app.Activity).finish()
                     }

                     override fun onFailure(e: Exception) {
                         Toast.makeText(context, "Thêm bài hát thất bại", Toast.LENGTH_SHORT).show()
                     }

                 })
             }
         }
        }
        else if(isFromAddPlayList == false) {
            holder.itemView.setOnClickListener {
                val intent = Intent(context, AllSongInPlayListActivity::class.java)
                intent.putExtra("PLAYLIST_ID", arrs[position].id)
                intent.putExtra("PLAYLIST_NAME", arrs[position].name)
                context.startActivity(intent)
            }
        }
    }
}