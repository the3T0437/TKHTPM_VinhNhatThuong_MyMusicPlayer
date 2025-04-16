package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.databinding.ArtistLayoutBinding
import com.musicapp.mymusicplayer.model.Artist
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener

interface ArtistClickListener{
    fun onArtistClick(artistID: Long)
}

class ArtistsAdapter(val context: Context, val artists: ArrayList<Artist>) : RecyclerView.Adapter<ArtistsAdapter.ViewHolder>(){

    private var artistClickListener: ArtistClickListener? = null

    inner class ViewHolder(val binding: ArtistLayoutBinding): RecyclerView.ViewHolder(binding.root){
        private val callback = object: OnClickListener{
            override fun onClick(v: View?) {
                when(v!!.id){
                    else -> {
                        artistClickListener?.onArtistClick(artists[absoluteAdapterPosition].id)
                    }
                }
            }
        }

        init {
            setupThreeDotWidget()
            binding.root.setOnClickListener(callback)
        }
    }

    private fun ViewHolder.setupThreeDotWidget() {
        setupOptions()
        setupEvents()
    }

    private fun ViewHolder.setupEvents() {
        binding.btnThreeDot.setThreeDotMenuListener(object : ThreeDotMenuListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.addToQueue -> {
                        Log.d("myLog", "add to queue")
                    }

                    else -> {
                        Log.d("myLog", "Action not implemented")
                        return false;
                    }
                }

                return true;
            }
        })
    }

    private fun ViewHolder.setupOptions() {
        binding.btnThreeDot.setMenuResource(R.menu.artist_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ArtistLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvArtist.setText(artists[position].artistName)
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    fun setArtistClickListener(artistClickListener: ArtistClickListener){
        this.artistClickListener = artistClickListener
    }
}