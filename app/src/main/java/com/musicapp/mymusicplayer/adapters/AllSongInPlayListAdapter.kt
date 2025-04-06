package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.SongLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllSongInPlayListAdapter(private val context: Context, private val songs : List<Song>, private val playlistId: Int) :
    RecyclerView.Adapter<AllSongInPlayListAdapter.ViewHolder>(){
    private val currentSongs = songs.toMutableList()
    class ViewHolder(val binding: SongLayoutBinding, private val adapter: AllSongInPlayListAdapter) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.tvTitle.text = song.title
            binding.tvArtist.text = song.artist
            binding.threeDotMenu.setMenuResource(R.menu.playing_song_menu)

            binding.threeDotMenu.setThreeDotMenuListener(object : ThreeDotMenuListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    if (item.itemId == R.id.menuRemove) {
                        adapter.removeSong(bindingAdapterPosition, song.id)
                        Log.d("song", "song: ${song}")
                        return true
                    }
                    return false
                }
            })

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SongLayoutBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, this) // Truyền Adapter vào ViewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(songs.get(position))
    }

    override fun getItemCount(): Int = songs.size
    private fun removeSong(position: Int, songID: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val databaseAPI = com.musicapp.mymusicplayer.database.DatabaseAPI(context)
                databaseAPI.deleteSongFromPlaylist(playlistId, songID, object : OnDatabaseCallBack{
                    override fun onSuccess(id: Long) {
                        currentSongs.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(context, "Đã xóa bài hát khỏi playlist!", Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(e: Exception) {
                    }
                })
            }
        }
    }
}