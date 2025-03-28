package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.SongInPlayListLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.songGetter
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class PlayingSongsActivity : AppCompatActivity() {
    private lateinit var binding: SongInPlayListLayoutBinding // Thêm biến binding
    private lateinit var playingQueueAdapter: SongAdapter
    private lateinit var databaseApi: DatabaseAPI
    private val playingSongs: ArrayList<Song> = arrayListOf()
    private var currentSongId: Long = -1
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SongInPlayListLayoutBinding.inflate(layoutInflater) // Khởi tạo binding
        setContentView(binding.root) // Sử dụng binding.root để set content view
        mediaController = store.mediaController

        // Khởi tạo Adapter
        playingQueueAdapter = SongAdapter(this, playingSongs)

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
        loadPlayingSongs()
        //xử lý back button
        binding.btnBack.setOnClickListener{
            finish()
        }

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

        //lay id tu file duoi local, tai vi database luu id tuong tu
        val mediaItemCount = store.mediaController!!.mediaItemCount
        val songIds = arrayListOf<Long>()
        for (i in 0 until mediaItemCount) {
            val mediaItem = store.mediaController!!.getMediaItemAt(i)
            val song = songGetter.getSong(this, mediaItem.localConfiguration!!.uri)
            songIds.add(song!!.id)
        }

        databaseApi.getSongs(songIds, object : OnGetItemCallback {
            override fun onSuccess(value: Any) {
                playingSongs.clear()
                playingSongs.addAll(value as ArrayList<Song>)
                playingQueueAdapter.notifyDataSetChanged()
            }

            override fun onFailure(e: Exception) {
            }
        })
    }
}