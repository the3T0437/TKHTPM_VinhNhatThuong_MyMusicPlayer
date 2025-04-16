package com.musicapp.mymusicplayer.activities

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.MusicSearchBarLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.store

class SearchSongActivity : AppCompatActivity() {
    private lateinit var binding: MusicSearchBarLayoutBinding
    private lateinit var songAdapter: SongAdapter
    private lateinit var mediaController :MediaControllerWrapper
    private var songList = ArrayList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MusicSearchBarLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.svSearch.requestFocusFromTouch()

        setupRecyclerView()
        setEvents()
    }

    private fun setupRecyclerView(){
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        songAdapter = SongAdapter(this, songList)
        songAdapter.mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.recyclerView.adapter = songAdapter
    }

    private fun setEvents(){
        setEventRecyclerView()
        setEventSearch()
        setEventBtnBack()
    }

    private fun setEventRecyclerView(){
        songAdapter.setSongClickListener( object: SongClickListener{
            override fun OnArtistClick(artist: Long) {

            }

            override fun onSongClick(song: Song, index: Int) {
                mediaController.clear()
                mediaController.addSongs(songList)
                mediaController.prepare()
                mediaController.play()
            }
        })
    }

    private fun setEventSearch(){
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("SearchView", "Query submitted: $query")
                if (!query.isNullOrEmpty()) {
                    searchSongs(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("SearchView", "Query changed: $newText")
                if (!newText.isNullOrEmpty()) {
                    searchSongs(newText)
                }
                return true
            }
        })
    }

    private fun setEventBtnBack(){
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun searchSongs(query: String) {
        DatabaseAPI(this).searchSongs(query, object : OnGetItemCallback {
            override fun onSuccess(value: Any) {
                if (value is List<*>) {
                    songList.clear()
                    songList.addAll(value.filterIsInstance<Song>())
                    songAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(
                    this@SearchSongActivity,
                    "Lỗi khi tìm kiếm: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
