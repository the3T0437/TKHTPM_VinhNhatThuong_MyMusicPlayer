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
import com.musicapp.mymusicplayer.databinding.FavoriteScreenLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class SongsOfAlbumActivity : AppCompatActivity() {
    private lateinit var binding: FavoriteScreenLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var songs: ArrayList<Song>
    private lateinit var databaseAPI: DatabaseAPI
    private lateinit var mediaController: MediaControllerWrapper
    private var albumName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        albumName = intent.getStringExtra("album_name") ?: return
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

    private fun setup() {
        songs = arrayListOf()
        databaseAPI = DatabaseAPI(this)

        binding.title.setText(albumName)

        mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.musicPlayer.mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.musicPlayer.songs = songs

        binding.btnDown.setOnClickListener {
            this@SongsOfAlbumActivity.finish()
        }

        adapter = SongAdapter(this, songs, mediaController)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager
    }

    override fun onResume() {
        super.onResume()
        databaseAPI.getSongsByAlbum(albumName, songs, object : OnDatabaseCallBack {
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(this@SongsOfAlbumActivity, "Lỗi", Toast.LENGTH_LONG).show()
            }
        })
    }
} 