package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.adapters.DragableSongAdapter
import com.musicapp.mymusicplayer.adapters.PlayingSongAdapter
import com.musicapp.mymusicplayer.adapters.RemoveSongListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.PlayingSongLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MoveListener
import com.musicapp.mymusicplayer.utils.SimpleItemTouchHelperCallBack
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
        makeDragable()
    }

    fun makeDragable(){
        val callBack = SimpleItemTouchHelperCallBack()
        callBack.setMoveListener(object: MoveListener{
            override fun onMove(from: Int, to: Int): Boolean {
                Log.d("myLog", "move $from to $to")

                val song = playingSongs.removeAt(from)
                playingSongs.add(to, song)

                mediaController?.moveMediaItem(from, to)
                adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwipe(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        })
        val itemTouchHelper = ItemTouchHelper(callBack)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        adapter.setItemTouchHelper(itemTouchHelper)
    }

    fun setEvent(){
        binding.btnBack.setOnClickListener{
            finish()
        }

        adapter.setRemoveSongListener(object: RemoveSongListener{
            override fun onRemoveSong(song: Song, index: Int) {
                mediaController?.removeMediaItem(index)
                Log.d("myLog", "remove at: $index")
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

        playingSongs.addAll(store.playingSongs ?: arrayListOf())
    }
}