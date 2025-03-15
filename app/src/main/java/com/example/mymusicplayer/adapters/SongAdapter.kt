package com.example.mymusicplayer.adapters

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicplayer.databinding.SongLayoutBinding
import com.example.mymusicplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongAdapter(val context: Context, val arr: List<Song>) : RecyclerView.Adapter<SongAdapter.ViewHolder>(){
    inner class ViewHolder(val binding: SongLayoutBinding): RecyclerView.ViewHolder(binding.root){
        var coroutineScope: CoroutineScope? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: SongLayoutBinding = SongLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song: Song = arr[position]

        holder.coroutineScope = CoroutineScope(Dispatchers.IO + Job())
        holder.coroutineScope!!.launch {
            try {
                val uri =
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
                val bitmap = context.contentResolver.loadThumbnail(uri, Size(640, 480), null)

                withContext(Dispatchers.Main){
                    holder.binding.img.setImageBitmap(bitmap)
                }
            }
            catch (e: Exception){

            }
        }
        holder.binding.tvTitle.setText(song.title)
        holder.binding.tvArtist.setText(song.artist)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.coroutineScope?.cancel()
    }

    override fun getItemCount(): Int {
        return arr.size
    }
}