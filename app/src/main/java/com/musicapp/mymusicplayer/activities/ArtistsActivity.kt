package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.ArtistClickListener
import com.musicapp.mymusicplayer.adapters.ArtistsAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.ArtistsLayoutBinding
import com.musicapp.mymusicplayer.model.Artist
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class ArtistsActivity : AppCompatActivity() {
    private lateinit var binding: ArtistsLayoutBinding
    private lateinit var adapter: ArtistsAdapter
    private var artists = arrayListOf<Artist>()
    private lateinit var databaseAPI: DatabaseAPI
    private lateinit var mediaController: MediaControllerWrapper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupMusicPlayerSmall()
        setupRecyclerView()
        setupDatabase()

        setEvents()
    }

    private fun setupUI() {
        enableEdgeToEdge()
        binding = ArtistsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = ArtistsAdapter(this, artists, mediaController)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager

        adapter.setArtistClickListener(object: ArtistClickListener{
            override fun onArtistClick(artistID: Long) {
                val intent = Intent(this@ArtistsActivity, SongsOfArtistActivity::class.java)
                intent.putExtra(Artist.ID, artistID)
                startActivity(intent)
            }
        })
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
            this@ArtistsActivity.finish()
        }
    }

    private fun setOnMusicPlayerClick(){
        binding.musicPlayer.setOnMusicPlayerClickListener(object: MusicPlayerSmallClickListener {
            override fun onPauseClick() {
            }

            override fun onStartClick() {
            }

            override fun onNextClick() {
            }

            override fun onMenuClick() {
            }

            override fun onMusicPlayerClick() {
            }
        })
    }

    override fun onResume() {
        super.onResume()

        databaseAPI.getAllArtists(artists, object: OnDatabaseCallBack{
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(this@ArtistsActivity, "get all artists fail", Toast.LENGTH_LONG)
            }
        })
    }
}