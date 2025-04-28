package com.musicapp.mymusicplayer.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.adapters.SongInPlayListAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.MusicSongInPlaylistLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener

/**
 * Activity hiển thị danh sách bài hát trong một playlist
 * Cho phép:
 * - Xem danh sách bài hát
 * - Phát nhạc
 * - Xóa bài hát khỏi playlist
 * - Kéo thả để sắp xếp bài hát
 */
class AllSongInPlayListActivity : AppCompatActivity() {
    private lateinit var binding: MusicSongInPlaylistLayoutBinding
    private lateinit var databaseAPI: DatabaseAPI
    private var songs = arrayListOf<Song>()
    private lateinit var adapter: SongInPlayListAdapter
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

        // Thiết lập RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        playlistId = intent.getIntExtra("PLAYLIST_ID", -1).toLong()
        
        adapter = SongInPlayListAdapter(this, songs, playlistId.toInt())
        binding.recyclerView.adapter = adapter

        // Xử lý sự kiện nút back
        binding.btnBack.setOnClickListener{
            finish()
        }

        // Hiển thị tên playlist
        val name = intent.getStringExtra("PLAYLIST_NAME")
        binding.tvNameList.text = name.toString()

        // Lấy danh sách bài hát trong playlist
        databaseAPI.getAllSongsInPlaylist(playlistId.toInt(), songs, object: OnDatabaseCallBack {
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(this@AllSongInPlayListActivity, "Không thể tải danh sách bài hát", Toast.LENGTH_SHORT).show()
            }
        })

        // Thêm xử lý phát nhạc
        adapter.setSongClickListener(object : SongClickListener {
            override fun OnArtistClick(artist: Long) {
                // Xử lý khi nhấn vào tên nghệ sĩ
            }

            override fun onSongClick(song: Song, position: Int) {
                mediaController.clear()
                mediaController.addSongs(songs)
                mediaController.seekToMediaItem(position)
                mediaController.prepare()
                mediaController.play()
            }
        })

        makeDragable()
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật danh sách bài hát khi quay lại activity
        databaseAPI.getAllSongsInPlaylist(playlistId.toInt(), songs, object: OnDatabaseCallBack {
            override fun onSuccess(id: Long) {
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(this@AllSongInPlayListActivity, "Không thể tải danh sách bài hát", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun makeDragable() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.moveItem(fromPosition, toPosition)

                // Cập nhật thứ tự trong database
                val song1 = songs[fromPosition]
                val song2 = songs[toPosition]
                databaseAPI.swapOrder(playlistId, song1.id, song2.id, object: OnDatabaseCallBack {
                    override fun onSuccess(id: Long) {
                    }

                    override fun onFailure(e: Exception) {
                        Toast.makeText(this@AllSongInPlayListActivity, "Không thể cập nhật thứ tự bài hát", Toast.LENGTH_SHORT).show()
                    }
                })

                updateMediaControllerAfterReorder()
                return true
            }

            private fun updateMediaControllerAfterReorder() {
                val currentPlayingIndex =
                    songs.indexOfFirst { it.id == adapter.currentPlayingSongId }
                if (currentPlayingIndex != -1) {
                    mediaController.clear()
                    mediaController.addSongs(songs)
                    mediaController.seekToMediaItem(currentPlayingIndex)
                    mediaController.prepare()
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        adapter.setItemTouchHelper(itemTouchHelper)
    }
}