package com.musicapp.mymusicplayer.activities
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
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
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.store

class PlayListActivity : AppCompatActivity() {
    private lateinit var binding: PlaylistLayoutBinding
    private lateinit var adapter : PlayListAdapter
    private var playlists = arrayListOf<PlayList>()
    private lateinit var databaseApi: DatabaseAPI
    private var selectedPlayList: PlayList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseApi = DatabaseAPI(this)

        binding = PlaylistLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        binding.musicPlayer.mediaController = mediaController

        binding.recyclerViewPlaylist.layoutManager = LinearLayoutManager(this)
        adapter = PlayListAdapter(this, playlists, -1L, false, mediaController)

        binding.recyclerViewPlaylist.adapter = adapter

        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnAddPlaylist.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("PlayList Name")


            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            input.hint = "Enter playlist name"
            input.setPadding(70,40,40,50)
            val params =  ViewGroup.MarginLayoutParams(  ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(16, 16, 16, 16) // Left, Top, Right, Bottom in pixels
            input.layoutParams = params
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val playListName = input.text.toString()
                if(playListName.isNotEmpty()){
                    val playlist =  PlayList(playListName)
                    playlists.add(playlist)
                    databaseApi.insertPlaylist(playlist, object: OnDatabaseCallBack{
                        override fun onSuccess(id: Long) {
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