package com.musicapp.mymusicplayer.adapters

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicplayer.adapters.PlaylistActivity
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.databinding.SongLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*

interface SongClickListener{
    fun onArtistClick(artist: String)
    fun onSongClick(song: Song)
}

class SongAdapter(private val context: Context, private val arr: List<Song>) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    private var songClickListener: SongClickListener? = null

    inner class ViewHolder(val binding: SongLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private var loadJob: Job? = null
        var songPosition: Int = -1

        val callback: OnClickListener = object : OnClickListener {
            override fun onClick(v: View?) {
                if (v == null)
                    return

                when (v.id) {
                    binding.tvArtist.id -> {
                        songClickListener?.onArtistClick(arr[songPosition].artist ?: "")
                    }

                    else -> {
                        songClickListener?.onSongClick(arr[songPosition])
                    }
                }
            }
        }

        init {
            binding.tvArtist.setOnClickListener(callback)
            binding.root.setOnClickListener(callback)
        }
        fun bind(song: Song) {
            Log.d("SongAdapter", "Binding song: ${song.title}, Artist: ${song.artist}")

            binding.tvTitle.text = song.title
            binding.tvArtist.text = song.artist

            loadJob?.cancel()
            loadJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        song.id
                    )
                    val bitmap = context.contentResolver.loadThumbnail(uri, Size(640, 480), null)

                    withContext(Dispatchers.Main) {
                        binding.img.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.img.setImageResource(R.drawable.thumbnail)
                    }
                }
            }

            Log.d("SongAdapter", "ThreeDotMenuView: ${binding.threeDotMenu}")

            binding.threeDotMenu.setMenuResource(R.menu.menu_song_options)
            binding.threeDotMenu.setThreeDotMenuListener(object : ThreeDotMenuListener {

                override fun onPlayNext() {
                    Toast.makeText(context, "Play Next: ${song.title}", Toast.LENGTH_SHORT).show()
                }

                override fun onAddToFavorite() {
                    Toast.makeText(context, "Add to Favorite: ${song.title}", Toast.LENGTH_SHORT).show()
                }

                override fun onAddToPlaylist() {
                    val intent = Intent(binding.root.context, PlaylistActivity::class.java)
                    binding.root.context.startActivity(intent)
                    Toast.makeText(context, "Add to: ${song.title}", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: SongLayoutBinding = SongLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arr[position])
        holder.songPosition = position
    }

    override fun getItemCount(): Int {
        return arr.size
    }

    fun setSongClickListener(songClickListener: SongClickListener){
        this.songClickListener = songClickListener
    }
}