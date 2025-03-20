package com.musicapp.mymusicplayer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.databinding.MainLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.songGetter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var songs: ArrayList<Song>
    private lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        songs = arrayListOf()
        setSupportActionBar(binding.toolbar)
        Toast.makeText(this, "open app", Toast.LENGTH_SHORT).show()
        setup()
    }

    override fun onResume() {
        super.onResume()
        songGetter.getAllSongs(this, songs)
        adapter.notifyDataSetChanged()
    }

    fun setup(){
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SongAdapter(this, songs)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager
        adapter.notifyDataSetChanged()

        setupPlayMusic()
    }

    fun setupPlayMusic(){
        player = ExoPlayer.Builder(this).build()

        adapter.setSongClickListener(object: SongClickListener {
            override fun onArtistClick(artist: String) {
            }

            override fun onSongClick(song: Song) {
                val mediaItem = MediaItem.fromUri(song.getUri())
                player.setMediaItem(mediaItem);
                player.prepare()
                player.play()
            }
        })
    }
}