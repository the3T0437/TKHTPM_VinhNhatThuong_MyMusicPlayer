package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.DragableSongAdapter
import com.musicapp.mymusicplayer.adapters.PlayingSongAdapter
import com.musicapp.mymusicplayer.adapters.RemoveSongListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.PlayingSongLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.songGetter
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class PlayingSongsActivity : AppCompatActivity() {
    private lateinit var binding: PlayingSongLayoutBinding// Thêm biến binding
    private lateinit var adapter: PlayingSongAdapter
    private lateinit var databaseApi: DatabaseAPI
    private val playingSongs: ArrayList<Song> = arrayListOf()
    private var currentSongId: Long = -1
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlayingSongLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaController = store.mediaController

        setup()
        setEvent()
    }

    fun setup(){
        adapter = PlayingSongAdapter(this, playingSongs)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter

        databaseApi = DatabaseAPI(this)

        loadPlayingSongs()
    }

    fun setEvent(){
        binding.btnBack.setOnClickListener{
            finish()
        }

        adapter.setRemoveSongListener(object: RemoveSongListener{
            override fun onRemoveSong(song: Song, index: Int) {
                mediaController?.removeMediaItem(index)
            }
        })

        setupMusicPlayerSmall()
    }

    private fun setupMusicPlayerSmall(){
        binding.musicPlayer.setOnMusicPlayerClickListener(object: MusicPlayerSmallClickListener {
            override fun onPauseClick() {
                if (mediaController!= null && mediaController!!.isPlaying)
                    mediaController?.pause()
            }

            override fun onStartClick() {
                if (mediaController!= null && mediaController?.currentMediaItem != null)
                    mediaController?.play()
            }

            override fun onNextClick() {
                if (mediaController != null && mediaController!!.hasNextMediaItem())
                    mediaController?.seekToNextMediaItem()
            }

            override fun onMenuClick() {
            }

            override fun onMusicPlayerClick() {
                val intent= Intent(this@PlayingSongsActivity, MusicDetailActivity::class.java)
                startActivity(intent)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        binding.musicPlayer.mediaController = store.mediaController
    }

    private fun loadPlayingSongs() {
        if (store.mediaController == null)
            return

        val mediaItemCount = store.mediaController!!.mediaItemCount
        val songIds = arrayListOf<Long>()
        for (i in 0 until mediaItemCount) {
            val mediaItem = store.mediaController!!.getMediaItemAt(i)
            val song = songGetter.getSong(this, mediaItem.localConfiguration!!.uri)
            songIds.add(song!!.id)
        }

        databaseApi.getSongs(songIds, object : OnGetItemCallback {
            override fun onSuccess(value: Any) {
                playingSongs.clear()
                playingSongs.addAll(value as ArrayList<Song>)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
            }
        })
    }
}