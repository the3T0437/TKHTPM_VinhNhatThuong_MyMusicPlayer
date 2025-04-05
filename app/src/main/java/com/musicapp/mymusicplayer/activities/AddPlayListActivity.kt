package com.musicapp.mymusicplayer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.PlayListAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.databinding.AddToPlaylistLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList

class AddPlayListActivity : AppCompatActivity() {
    private lateinit var binding: AddToPlaylistLayoutBinding
    private lateinit var adapter : PlayListAdapter
    private var playlist = arrayListOf<PlayList>()
    private lateinit var databaseApi: DatabaseAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseApi = DatabaseAPI(this)

        binding = AddToPlaylistLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewAddToPlaylist.layoutManager = LinearLayoutManager(this)
        adapter = PlayListAdapter(this, playlist)

        binding.recyclerViewAddToPlaylist.adapter = adapter
    }
}