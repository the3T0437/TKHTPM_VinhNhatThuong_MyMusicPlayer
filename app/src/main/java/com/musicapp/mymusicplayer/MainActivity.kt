package com.musicapp.mymusicplayer

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicapp.mymusicplayer.activities.MusicDetailActivity
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.databinding.MainLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.service.PlayBackService
import com.musicapp.mymusicplayer.utils.songGetter
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var songs: ArrayList<Song>
    private var factory: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        songs = arrayListOf()
        setSupportActionBar(binding.toolbar)
        Toast.makeText(this, "open app", Toast.LENGTH_SHORT).show()
        setup()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        songGetter.getAllSongs(this, songs)
        adapter.notifyDataSetChanged()
        createMediaController()
    }

    override fun onStop() {
        factory?.let {
            MediaController.releaseFuture(it)
        }
        factory = null
        super.onStop()
    }

    fun setup(){
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SongAdapter(this, songs)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager
        adapter.notifyDataSetChanged()

        binding.musicPlayer.setOnMusicPlayerClickListener(object: MusicPlayerSmallClickListener{
            override fun onPauseClick() {
                if (mediaController!= null && mediaController!!.isPlaying)
                    mediaController?.pause()
            }

            override fun onStartClick() {
            }

            override fun onNextClick() {
            }

            override fun onMenuClick() {
            }

            override fun onMusicPlayerClick() {
                val intent= Intent(this@MainActivity, MusicDetailActivity::class.java)
                startActivity(intent)
            }
        })

        setupPlayMusic()
    }

    fun setupPlayMusic(){
        adapter.setSongClickListener(object: SongClickListener {
            override fun onArtistClick(artist: String) {
            }

            override fun onSongClick(song: Song) {
                val mediaItem = MediaItem.fromUri(song.getUri())
                mediaController?.addMediaItem(mediaItem)
                mediaController?.prepare()
                mediaController?.play()
            }
        })
    }

    fun createMediaController(){
        val sessionToken = SessionToken(this, ComponentName(this, PlayBackService::class.java))
        factory = MediaController.Builder(this, sessionToken).buildAsync()

        factory?.addListener(
            {
                // MediaController is available here with controllerFuture.get()
                mediaController = factory?.let {
                    if (it.isDone)
                        it.get()
                    else
                        null
                }

                binding.musicPlayer.mediaController = mediaController
            },
            MoreExecutors.directExecutor()
        )

    }
}