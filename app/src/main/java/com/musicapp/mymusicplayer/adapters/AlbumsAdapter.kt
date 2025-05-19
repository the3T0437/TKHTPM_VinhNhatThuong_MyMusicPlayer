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
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.ArtistLayoutBinding
import com.musicapp.mymusicplayer.model.Album
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener

interface AlbumClickListener{
    fun onAlbumClick(albumName: String)
}

class AlbumsAdapter(val context: Context, val albums: ArrayList<Album>, val mediaController: MediaControllerWrapper) : RecyclerView.Adapter<AlbumsAdapter.ViewHolder>(){

    private var artistClickListener: AlbumClickListener? = null

    inner class ViewHolder(val binding: ArtistLayoutBinding): RecyclerView.ViewHolder(binding.root){
        private val callback = object: OnClickListener {
            override fun onClick(v: View?) {
                when(v!!.id){
                    else -> {
                        artistClickListener?.onAlbumClick(albums[absoluteAdapterPosition].albumName)
                    }
                }
            }
        }

        init {
            binding.root.setOnClickListener(callback)
            setupOptions()
            setupEvents()
        }
    }


    private fun ViewHolder.setupEvents() {
        binding.btnThreeDot.setThreeDotMenuListener(object : ThreeDotMenuListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.addToQueue -> {
                        val albumName = albums.get(this@setupEvents.absoluteAdapterPosition).albumName
                        val databaseAPI = DatabaseAPI(context)
                        val songs = arrayListOf<Song>()
                        databaseAPI.getSongsByAlbum(albumName, songs, object: OnDatabaseCallBack {
                            override fun onSuccess(id: Long) {
                                mediaController.addSongs(songs)
                            }

                            override fun onFailure(e: Exception) {
                            }
                        })
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
        holder.binding.tvArtist.setText(albums[position].albumName)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun setAlbumClickListener(artistClickListener: AlbumClickListener){
        this.artistClickListener = artistClickListener
    }
}
