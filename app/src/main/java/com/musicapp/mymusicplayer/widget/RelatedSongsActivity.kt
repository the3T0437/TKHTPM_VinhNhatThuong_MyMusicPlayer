package com.musicapp.mymusicplayer.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.PlayingQueueAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.SongInPlayListLayoutBinding
import com.musicapp.mymusicplayer.model.Song

class RelatedSongsActivity : AppCompatActivity() {
    private lateinit var binding: SongInPlayListLayoutBinding // Thêm biến binding
    private lateinit var playingQueueAdapter: PlayingQueueAdapter
    private lateinit var databaseApi: DatabaseAPI
    private var currentSongId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SongInPlayListLayoutBinding.inflate(layoutInflater) // Khởi tạo binding
        setContentView(binding.root) // Sử dụng binding.root để set content view

        // Khởi tạo Adapter
        playingQueueAdapter = PlayingQueueAdapter(this) { song ->
            // Xử lý sự kiện click vào bài hát
            println("Selected Song: ${song.title}")
        }

        // Cấu hình RecyclerView (sử dụng binding.recyclerViewQueue thay vì findViewById)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = playingQueueAdapter

        // Khởi tạo DatabaseAPI
        databaseApi = DatabaseAPI(this)

        // Lấy currentSongId từ Intent
        currentSongId = intent.getLongExtra("songId", 1) // Sửa lỗi lấy giá trị mặc định là -1
        // kiểm tra xem có tồn tại currentSongId không, nếu không thì kết thúc Activity
        if (currentSongId == -1L) {
            finish()
            return
        }
        // Lấy dữ liệu từ database
        loadRelatedSongs()
        //xử lý back button
        binding.btnBack.setOnClickListener{
            finish()
        }
    }

    private fun loadRelatedSongs() {
        databaseApi.getRelatedSongs(currentSongId, object : OnGetItemCallback {
            override fun onSuccess(value: Any) {
                val songs = value as List<Song>
                runOnUiThread {
                    playingQueueAdapter.updateSongs(songs)
                }
            }
            override fun onFailure(e: Exception) {
                Log.e("RelatedSongsActivity", "Failed to load related songs", e)
            }
        })
    }
}