package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.AlbumsAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.AlbumsLayoutBinding
import com.musicapp.mymusicplayer.model.Album
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class Albums_Activity : AppCompatActivity() {
    private lateinit var binding: AlbumsLayoutBinding
    private lateinit var adapter: AlbumsAdapter
    private var albums = arrayListOf<Album>()
    private lateinit var databaseAPI: DatabaseAPI
    private lateinit var mediaController: MediaControllerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupRecyclerView()
        setupDatabase()
        setupMusicPlayerSmall()
        setEvents()
    }

    private fun setupUI() {
        enableEdgeToEdge()
        binding = AlbumsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = AlbumsAdapter(this, albums)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager

        adapter.setAlbumClickListener { album ->
            val intent = Intent(this@Albums_Activity, SongsOfAlbumActivity::class.java)
            intent.putExtra("album_name", album.albumName)
            startActivity(intent)
        }
    }

    private fun setupDatabase() {
        databaseAPI = DatabaseAPI(this)
    }

    private fun setupMusicPlayerSmall() {
        mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.musicPlayer.mediaController = mediaController
    }

    private fun setEvents() {
        setOnBtnDownClick()
        setOnMusicPlayerClick()
    }

    private fun setOnBtnDownClick() {
        binding.btnDown.setOnClickListener {
            finish()
        }
    }

    private fun setOnMusicPlayerClick() {
        binding.musicPlayer.setOnMusicPlayerClickListener(object : MusicPlayerSmallClickListener {
            override fun onPauseClick() {
                mediaController.pause()
            }

            override fun onStartClick() {
                mediaController.play()
            }

            override fun onNextClick() {
            }

            override fun onMenuClick() {

            }

            override fun onMusicPlayerClick() {
                val intent = Intent(this@Albums_Activity, PlayingSongsActivity::class.java)
                startActivity(intent)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadAlbums()
    }

    private fun loadAlbums() {
        databaseAPI.getAllAlbums(albums, object : OnDatabaseCallBack {
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(this@Albums_Activity, "Đọc album lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

