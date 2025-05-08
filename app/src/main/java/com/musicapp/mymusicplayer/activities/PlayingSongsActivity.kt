package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.adapters.PlayingSongAdapter
import com.musicapp.mymusicplayer.adapters.RemoveSongListener
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.databinding.PlayingSongLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.MoveListener
import com.musicapp.mymusicplayer.utils.SimpleItemTouchHelperCallBack
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class PlayingSongsActivity : AppCompatActivity() {
    private lateinit var binding: PlayingSongLayoutBinding// Thêm biến binding
    private lateinit var adapter: PlayingSongAdapter
    private lateinit var databaseApi: DatabaseAPI
    private lateinit var playingSongs: ArrayList<Song>
    private var currentSongId: Long = -1
    private lateinit var mediaController: MediaControllerWrapper
    private val mediaControllerListener: Player.Listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            hightlightPlayingSong()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlayingSongLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        mediaController.addListener(mediaControllerListener)
        playingSongs = mediaController.playingSongs

        setup()
        setEvent()
    }

    fun setup(){
        setupRecyclerView()

        databaseApi = DatabaseAPI(this)
        makeDragable()
    }

    private fun setupRecyclerView() {
        bindingData()
        setEventRecyclerView()
        scrollRecyclerView()
    }

    private fun bindingData() {
        adapter = PlayingSongAdapter(this, playingSongs, mediaController)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
    }

    private fun setEventRecyclerView(){
        adapter.setSongClickListener(object: SongClickListener(){
            override fun isPlayingSongWhenClicked(): Boolean {
                return false
            }

            override fun onSongClick(song: Song, index: Int) {
                mediaController.seekToMediaItem(index)
            }
        })
    }

    private fun scrollRecyclerView() {
        val positionScrollTo = getPositionToScrollTo()
        binding.recyclerView.scrollToPosition(positionScrollTo)
    }

    private fun getPositionToScrollTo(): Int {
        var positionScrollTo = store.mediaBrowser?.currentMediaItemIndex ?: 0
        positionScrollTo -= 2;
        if (positionScrollTo < 0)
            positionScrollTo = 0;
        return positionScrollTo
    }

    fun makeDragable(){
        val callBack = SimpleItemTouchHelperCallBack()
        callBack.setMoveListener(object: MoveListener{
            override fun onMove(from: Int, to: Int): Boolean {
                Log.d("myLog", "move $from to $to")
                mediaController.moveMediaItem(from, to)
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

        setEventAdapter()
    }

    private fun setEventAdapter(){
        adapter.setRemoveSongListener(object: RemoveSongListener{
            override fun onRemoveSong(song: Song, index: Int) {
                mediaController.removeSong(index)
                adapter.notifyItemRemoved(index)
                Log.d("myLog", "remove at: $index")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        hightlightPlayingSong()

        loadPlayingSongs()
        binding.musicPlayer.mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
    }

    private fun hightlightPlayingSong(){
        adapter.currentPlayingSongId = mediaController.getCurrentSongId(this)
    }

    private fun loadPlayingSongs() {
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController.removeListener(mediaControllerListener)
    }
}