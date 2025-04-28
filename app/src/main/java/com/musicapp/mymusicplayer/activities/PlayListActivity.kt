package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.adapters.PlayListAdapter
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.MusicSongInPlaylistLayoutBinding
import com.musicapp.mymusicplayer.databinding.PlaylistLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.MoveListener
import com.musicapp.mymusicplayer.utils.SimpleItemTouchHelperCallBack
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class PlayListActivity : AppCompatActivity() {
    private lateinit var binding: PlaylistLayoutBinding
    private lateinit var adapter: PlayListAdapter
    private var playlists = arrayListOf<PlayList>()
    private lateinit var databaseAPI: DatabaseAPI
    private var selectedPlayList: PlayList? = null
    private lateinit var mediaController: MediaControllerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseAPI = DatabaseAPI(this)
        binding = PlaylistLayoutBinding.inflate(layoutInflater)
        mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)

        setContentView(binding.root)
        binding.musicPlayer.mediaController = mediaController

        binding.recyclerViewPlaylist.layoutManager = LinearLayoutManager(this)
        adapter = PlayListAdapter(this, playlists, -1L, false)
        binding.recyclerViewPlaylist.adapter = adapter

        binding.musicPlayer.setOnMusicPlayerClickListener(object : MusicPlayerSmallClickListener {
            override fun onPauseClick() {
                mediaController.pause()
            }

            override fun onStartClick() {
                if (mediaController.getSize() == 0) {
                    if (playlists.isNotEmpty()) {
                        val firstPlaylist = playlists[0]
                        val songs = arrayListOf<Song>()
                        databaseAPI.getAllSongsInPlaylist(firstPlaylist.id.toInt(), songs, object: OnDatabaseCallBack {
                            override fun onSuccess(id: Long) {
                                if (songs.isNotEmpty()) {
                                    mediaController.clear()
                                    mediaController.addSongs(songs)
                                    mediaController.prepare()
                                    mediaController.play()
                                }
                            }

                            override fun onFailure(e: Exception) {
                                Toast.makeText(this@PlayListActivity, "Không thể phát nhạc", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    mediaController.play()
                }
            }

            override fun onNextClick() {
                if (mediaController.hasNextMediaItem()) {
                    mediaController.seekToNextMediaItem()
                }
            }

            override fun onMenuClick() {
                val intent = Intent(this@PlayListActivity, PlayingSongsActivity::class.java)
                startActivity(intent)
            }

            override fun onMusicPlayerClick() {
                val intent = Intent(this@PlayListActivity, MusicDetailActivity::class.java)
                startActivity(intent)
            }
        })

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddPlaylist.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("PlayList Name")
            builder.setMessage("Enter playlist name")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val playListName = input.text.toString()
                if(playListName.isNotEmpty()){
                    val playlist = PlayList(playListName)
                    playlists.add(playlist)
                    databaseAPI.insertPlaylist(playlist, object: OnDatabaseCallBack{
                        override fun onSuccess(id: Long) {
                            playlists.clear()
                            readPlaylists(playlists)
                            adapter.notifyDataSetChanged()
                            Toast.makeText(this@PlayListActivity, "Thêm thành công", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(e: Exception) {
                            Toast.makeText(this@PlayListActivity, "Thêm thất bại", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    })
                }
            }
            builder.setNegativeButton("Cancel"){dialog,_->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    override fun onResume() {
        super.onResume()
        readPlaylists(playlists)
    }

    fun readPlaylists(playlists: ArrayList<PlayList>) {
        databaseAPI.getAllPlaylists(playlists, object: OnDatabaseCallBack{
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
            }
        })
    }
}