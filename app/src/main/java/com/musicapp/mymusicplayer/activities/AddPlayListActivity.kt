package com.musicapp.mymusicplayer.activities

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.PlayListAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.AddToPlaylistLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList

class AddPlayListActivity : AppCompatActivity() {
    private lateinit var binding: AddToPlaylistLayoutBinding
    private lateinit var adapter : PlayListAdapter
    private var playlists = arrayListOf<PlayList>()
    private lateinit var databaseApi: DatabaseAPI
    private var songID: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseApi = DatabaseAPI(this)
        songID = intent.getLongExtra("song_id", -1)

        binding = AddToPlaylistLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewAddToPlaylist.layoutManager = LinearLayoutManager(this)
        adapter = PlayListAdapter(this, playlists, songID, true)
        binding.recyclerViewAddToPlaylist.adapter = adapter

        binding.tvAddPlaylist.setOnClickListener{
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
                    databaseApi.insertPlaylist(playlist, object: OnDatabaseCallBack{
                        override fun onSuccess(id: Long) {
                            playlist.id = id.toInt()
                            playlists.add(playlist)
                            adapter.notifyDataSetChanged()
                            if (songID != -1L) {
                                databaseApi.themSongPlayList(songID, playlist.id, object: OnDatabaseCallBack {
                                    override fun onSuccess(id: Long) {
                                        Toast.makeText(this@AddPlayListActivity, "Đã thêm bài hát vào playlist", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }

                                    override fun onFailure(e: Exception) {
                                        Toast.makeText(this@AddPlayListActivity, "Thêm bài hát thất bại", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }

                        override fun onFailure(e: Exception) {
                            Toast.makeText(this@AddPlayListActivity, "Thêm playlist thất bại", Toast.LENGTH_SHORT).show()
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
        databaseApi.getAllPlaylists(playlists , object: OnDatabaseCallBack {
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }
            override fun onFailure(e: Exception) {
            }
        })
    }
}