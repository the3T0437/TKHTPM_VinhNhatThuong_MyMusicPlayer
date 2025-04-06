package com.musicapp.mymusicplayer.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.AllSongInPlayListAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.SongPlayListDAO
import com.musicapp.mymusicplayer.databinding.MusicSongInPlaylistLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllSongInPlayListActivity : AppCompatActivity() {
    private lateinit var binding: MusicSongInPlaylistLayoutBinding
    private lateinit var databaseAPI: DatabaseAPI
    private var songs = arrayListOf<Song>()
    private lateinit var adapter: AllSongInPlayListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseAPI = DatabaseAPI(this)

        binding = MusicSongInPlaylistLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val playlistId = intent.getIntExtra("PLAYLIST_ID", -1)
        Log.d("id", "id của list ${playlistId}")
        adapter = AllSongInPlayListAdapter(this, songs, playlistId)
        binding.recyclerView.adapter = adapter

        binding.btnBack.setOnClickListener{
            finish()
        }
        val name  = intent.getStringExtra("PLAYLIST_NAME")
        binding.tvNameList.text = name.toString()

    }

    override fun onResume() {
        super.onResume()
        readSongs(songs)
    }
    private fun readSongs(songs: ArrayList<Song>) {
        val playlistId = intent.getIntExtra("PLAYLIST_ID", -1)
        Log.d("id", "id của readsong ${playlistId}")
        if (playlistId != -1) {
            lifecycleScope.launch {
                val fetchedSongs = withContext(Dispatchers.IO) {
                    databaseAPI.getSongPlayListDAO().getSongsInPlayList(playlistId)
                }
                songs.clear()
                songs.addAll(fetchedSongs)
                adapter.notifyDataSetChanged()
            }
        }
    }

}