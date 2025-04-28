package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.DragableSongLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SongInPlayListAdapter(private val context: Context, songs : List<Song>, private val playlistId: Int) :
    RecyclerView.Adapter<SongInPlayListAdapter.ViewHolder>(){
    private var itemTouchHelper: ItemTouchHelper? = null
    private val mutableSongs = songs.toMutableList()

    interface OnSongDeletedListener {
        fun onSongDeleted(songId: Long)
    }
    private var onSongDeletedListener: OnSongDeletedListener? = null

    fun setOnSongDeletedListener(listener: OnSongDeletedListener) {
        this.onSongDeletedListener = listener
    }

    fun getSongs(): List<Song> {
        return mutableSongs.toList()
    }

    fun updateSongs(newSongs: List<Song>) {
        mutableSongs.clear()
        mutableSongs.addAll(newSongs)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: DragableSongLayoutBinding, private val adapter: SongInPlayListAdapter) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.tvTitle.text = song.title
            binding.tvArtist.text = song.artist
            binding.threeDotMenu.setMenuResource(R.menu.playing_song_menu)

            binding.threeDotMenu.setThreeDotMenuListener(object : ThreeDotMenuListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    if (item.itemId == R.id.menuRemove) {
                        adapter.removeSong(bindingAdapterPosition, song.id)
                        return true
                    }
                    return false
                }
            })
            binding.btnDrag.setOnTouchListener { v, event ->
                adapter.itemTouchHelper?.startDrag(this)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DragableSongLayoutBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mutableSongs.get(position))

    }
    override fun getItemCount(): Int = mutableSongs.size
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
    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper){
        this.itemTouchHelper = itemTouchHelper
    }
}