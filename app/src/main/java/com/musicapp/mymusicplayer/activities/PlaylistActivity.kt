package com.musicapp.mymusicplayer.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.adapters.PlayListAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.databinding.PlaylistLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList

class PlayListActivity : AppCompatActivity() {
    private lateinit var binding: PlaylistLayoutBinding
    private lateinit var playListAdapter: PlayListAdapter
    private lateinit var databaseApi: DatabaseAPI
    private lateinit var playList: ArrayList<PlayList>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlaylistLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseApi = DatabaseAPI(this)
        playListAdapter = PlayListAdapter(this, playList)

    }
}