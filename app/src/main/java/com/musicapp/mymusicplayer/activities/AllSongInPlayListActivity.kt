package com.musicapp.mymusicplayer.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.adapters.RemoveSongListener
import com.musicapp.mymusicplayer.adapters.SongInPlaylistAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.database.SongPlayListDAO
import com.musicapp.mymusicplayer.databinding.MusicSongInPlaylistLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.MoveListener
import com.musicapp.mymusicplayer.utils.SimpleItemTouchHelperCallBack
import com.musicapp.mymusicplayer.utils.store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllSongInPlayListActivity : AppCompatActivity() {
    private lateinit var binding: MusicSongInPlaylistLayoutBinding
    private lateinit var databaseAPI: DatabaseAPI
    private var songs = arrayListOf<Song>()
    private lateinit var adapter: SongInPlaylistAdapter
    private lateinit var mediaController: MediaControllerWrapper
    private var playlistId: Long = -1;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseAPI = DatabaseAPI(this)
        binding = MusicSongInPlaylistLayoutBinding.inflate(layoutInflater)
        mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)

        setContentView(binding.root)
        binding.musicPlayer.mediaController = mediaController
        binding.musicPlayer.songs = songs

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        playlistId = intent.getIntExtra("PLAYLIST_ID", -1).toLong()
        Log.d("id", "id của list ${playlistId}")
        adapter = SongInPlaylistAdapter(this, songs, mediaController)
        binding.recyclerView.adapter = adapter

        binding.btnBack.setOnClickListener{
            finish()
        }
        val name  = intent.getStringExtra("PLAYLIST_NAME")
        binding.tvNameList.text = name.toString()

        makeDragable()
        setupRemoveListener()
    }

    private fun setupRemoveListener() {
        adapter.setRemoveSongListener(object : RemoveSongListener {
            override fun onRemoveSong(song: Song, index: Int) {
                databaseAPI.deleteSongFromPlaylist(playlistId.toInt(), song.id, object : OnDatabaseCallBack {
                    override fun onSuccess(id: Long) {
                        songs.removeAt(index)
                        adapter.notifyItemRemoved(index)
                        Toast.makeText(this@AllSongInPlayListActivity, "Removed from playlist", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(e: Exception) {
                        Toast.makeText(this@AllSongInPlayListActivity, "Failed to remove song", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })
    }

    override fun onResume() {
        super.onResume()
        readSongs(songs)
    }

    private fun readSongs(songs: ArrayList<Song>) {
        val playlistId = intent.getIntExtra("PLAYLIST_ID", -1)
        Log.d("id", "id của readsong ${playlistId}")
        if (playlistId == -1)
            return

        databaseAPI.getAllSongsInPlaylist(playlistId, songs, object: OnDatabaseCallBack{
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(this@AllSongInPlayListActivity, "can't get songs from playlist", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun makeDragable(){
        val callBack = SimpleItemTouchHelperCallBack()
        callBack.setMoveListener(object: MoveListener {
            override fun onMove(from: Int, to: Int): Boolean {
                Log.d("myLog", "move $from to $to")
                databaseAPI.swapOrder(playlistId, songs[from].id, songs[to].id, object: OnDatabaseCallBack{
                    override fun onSuccess(id: Long) {
                    }

                    override fun onFailure(e: Exception) {
                    }
                })
                adapter.notifyItemMoved(from, to)
                val swapSong = songs.removeAt(from)
                songs.add(to, swapSong)
                return true
            }

            override fun onSwipe(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        })
        val itemTouchHelper = ItemTouchHelper(callBack)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        adapter.setItemTouchHelper(itemTouchHelper)
    }
}