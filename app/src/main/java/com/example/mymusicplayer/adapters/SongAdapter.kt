package com.example.mymusicplayer.adapters

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

import com.example.mymusicplayer.R
import com.example.mymusicplayer.databinding.SongLayoutBinding
import com.example.mymusicplayer.model.Song
import kotlinx.coroutines.*

class SongAdapter(private val context: Context, private val arr: List<Song>) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: SongLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private var loadJob: Job? = null

        fun bind(song: Song) {
            binding.tvTitle.text = song.title
            binding.tvArtist.text = song.artist
            loadJob?.cancel()
            loadJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
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

            binding.imgThreeDot.setOnClickListener { view ->
                val popupMenu = PopupMenu(view.context, binding.imgThreeDot)
                popupMenu.menuInflater.inflate(R.menu.menu_song_options, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_play_next -> {
                            Toast.makeText(view.context, "Play Next: ${song.title}", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.action_add_favorite -> {
                            Toast.makeText(view.context, "Add to Favorite: ${song.title}", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.action_add_to -> {
                            val intent = Intent(binding.root.context, PlaylistActivity::class.java)
                            binding.root.context.startActivity(intent)
                            Toast.makeText(view.context, "Add to: ${song.title}", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SongLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arr[position])
    }

    override fun getItemCount(): Int {
        return arr.size
    }
}
