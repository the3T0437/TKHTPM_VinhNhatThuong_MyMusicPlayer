package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.R

import com.musicapp.mymusicplayer.model.Song

class PlayingQueueAdapter(private val context: Context, private val onItemClick: (Song) -> Unit) : RecyclerView.Adapter<PlayingQueueAdapter.SongViewHolder>() {

    private var songList: List<Song> = listOf()

    fun updateSongs(newSongs: List<Song>) {
        songList = newSongs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.song_layout, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songList[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int = songList.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvArtist: TextView = itemView.findViewById(R.id.tvArtist)

        fun bind(song: Song) {
            tvTitle.text = song.title
            tvArtist.text = song.artist
            itemView.setOnClickListener { onItemClick(song) }
        }
    }
}

