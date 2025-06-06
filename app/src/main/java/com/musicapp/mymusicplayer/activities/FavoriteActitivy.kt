package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.FavoriteScreenLayoutBinding
import com.musicapp.mymusicplayer.model.FavoriteSong
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class FavoriteActitivy : AppCompatActivity() {
    private lateinit var binding: FavoriteScreenLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var favoriteSongs: ArrayList<Song>
    private lateinit var databaseAPI: DatabaseAPI
    private lateinit var mediaController: MediaControllerWrapper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = FavoriteScreenLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setup()
    }

    fun setup(){
        favoriteSongs = arrayListOf()
        databaseAPI = DatabaseAPI(this)
        mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.musicPlayer.mediaController = mediaController
        binding.musicPlayer.songs = favoriteSongs

        binding.btnDown.setOnClickListener{
            this@FavoriteActitivy.finish()
        }


        adapter = SongAdapter(this, favoriteSongs, mediaController)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager

        //setupPlayMusic()
    }


    fun setupPlayMusic(){
        adapter.setSongClickListener(object: SongClickListener (){
            override fun onArtistClick(artist: Long) {
            }

            override fun onSongClick(song: Song, position: Int) {
                mediaController.clear()
                mediaController.addSongs(favoriteSongs)
                mediaController.seekToMediaItem(position)
                mediaController.prepare()
                mediaController.play()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        databaseAPI.getAllFavoriteSong(favoriteSongs, object: OnDatabaseCallBack{
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
            }
        })
    }

}