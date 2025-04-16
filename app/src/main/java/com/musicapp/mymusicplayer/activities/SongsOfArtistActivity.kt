package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.FavoriteScreenLayoutBinding
import com.musicapp.mymusicplayer.model.Artist
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class SongsOfArtistActivity : AppCompatActivity() {
    private lateinit var binding: FavoriteScreenLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var songs: ArrayList<Song>
    private lateinit var databaseAPI: DatabaseAPI
    private lateinit var mediaController: MediaControllerWrapper
    private var artistId: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        artistId = intent.getLongExtra(Artist.ID, 0)
        setupUI()
        setup()
    }

    private fun setupUI() {
        binding = FavoriteScreenLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun setup(){
        songs = arrayListOf()
        databaseAPI = DatabaseAPI(this)
        mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.musicPlayer.mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.musicPlayer.songs = songs

        binding.btnDown.setOnClickListener{
            this@SongsOfArtistActivity.finish()
        }
        databaseAPI.getArtist(artistId, object: OnGetItemCallback{
            override fun onSuccess(value: Any) {
                val artist = value as Artist
                binding.title.setText(artist.artistName)
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(this@SongsOfArtistActivity, "can't get artist", Toast.LENGTH_LONG)
            }
        })


        adapter = SongAdapter(this, songs, mediaController)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager

        setupPlayMusic()
    }


    fun setupPlayMusic(){
        /*
        adapter.setSongClickListener(object: SongClickListener {
            override fun OnArtistClick(artist: Long) {
            }

            override fun onSongClick(song: Song, position: Int) {
                mediaController.clear()
                mediaController.addSongs(songs)
                mediaController.seekToMediaItem(position)
                mediaController.prepare()
                mediaController.play()
            }
        })
        */
    }

    override fun onResume() {
        super.onResume()
        databaseAPI.getSongsByArtistId(artistId, songs, object: OnDatabaseCallBack{
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
            }
        })
    }}