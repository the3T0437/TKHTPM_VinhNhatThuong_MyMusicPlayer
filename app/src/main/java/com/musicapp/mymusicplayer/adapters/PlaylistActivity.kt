package com.example.mymusicplayer.adapters

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musicapp.mymusicplayer.databinding.PlaylistLayoutBinding


class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: PlaylistLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlaylistLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}