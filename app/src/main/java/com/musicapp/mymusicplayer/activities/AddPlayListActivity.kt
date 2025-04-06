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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseApi = DatabaseAPI(this)

        binding = AddToPlaylistLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewAddToPlaylist.layoutManager = LinearLayoutManager(this)
        val songID = intent.getLongExtra("song_id", -1L)
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
                    databaseApi.insertPlaylist(PlayList(playListName), object: OnDatabaseCallBack{
                        override fun onSuccess(id: Long) {
                            val playlist =  PlayList(playListName)
                            playlists.add(playlist)
                            adapter.notifyDataSetChanged()
                            Toast.makeText(this@AddPlayListActivity, "Thêm thành công", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(e: Exception) {
                            Toast.makeText(this@AddPlayListActivity, "Thêm thất bại", Toast.LENGTH_SHORT).show()
                        }

                    })
                }
            }
            builder.setNegativeButton("Cancel"){dailog,_->
                dailog.dismiss()
            }
            builder.show()
        }


    }
    override fun onResume() {
        super.onResume()
        readPlaylists(playlists)
    }

    //fun addPlayList()

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