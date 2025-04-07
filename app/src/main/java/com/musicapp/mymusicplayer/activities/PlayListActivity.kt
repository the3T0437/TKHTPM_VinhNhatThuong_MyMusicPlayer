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
import com.musicapp.mymusicplayer.databinding.PlaylistLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList

class PlayListActivity : AppCompatActivity() {
    private lateinit var binding: PlaylistLayoutBinding
    private lateinit var adapter : PlayListAdapter
    private var playlist = arrayListOf<PlayList>()
    private lateinit var databaseApi: DatabaseAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseApi = DatabaseAPI(this)

        binding = PlaylistLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewPlaylist.layoutManager = LinearLayoutManager(this)
        adapter = PlayListAdapter(this, playlist)

        binding.recyclerViewPlaylist.adapter = adapter

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddPlaylist.setOnClickListener{
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
                            adapter.notifyDataSetChanged()
                            Toast.makeText(this@PlayListActivity, "Thêm thành công", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(e: Exception) {
                            Toast.makeText(this@PlayListActivity, "Thêm thất bại", Toast.LENGTH_SHORT).show()
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
        readPlaylists(playlist)
    }

    //fun addPlayList()

    fun readPlaylists(playlists: ArrayList<PlayList>) {
        databaseApi.getAllPlaylists(playlists , object: OnDatabaseCallBack{
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }
            override fun onFailure(e: Exception) {
            }
        })
    }
}