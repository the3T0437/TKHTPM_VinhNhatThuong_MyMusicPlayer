package com.example.mymusicplayer

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusicplayer.adapters.SongAdapter
import com.example.mymusicplayer.databinding.MainLayoutBinding
import com.example.mymusicplayer.model.Song
import com.example.mymusicplayer.utils.songGetter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var songs: ArrayList<Song>
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
    }


}