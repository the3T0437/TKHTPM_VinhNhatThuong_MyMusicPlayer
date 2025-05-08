package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.model.Album

class AlbumsAdapter(
    private val context: Context,
    private val albums: ArrayList<Album>
) : RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder>() {

    private var albumClickListener: ((Album) -> Unit)? = null

    fun setAlbumClickListener(listener: (Album) -> Unit) {
        albumClickListener = listener
    }

    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAlbumName: TextView = itemView.findViewById(R.id.tvAlbumName)
        val tvReleaseDate: TextView = itemView.findViewById(R.id.tvReleaseDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    albumClickListener?.invoke(albums[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.album_layout, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.tvAlbumName.text = album.albumName
        holder.tvReleaseDate.text = album.releaseDate
    }

    override fun getItemCount(): Int = albums.size
} 