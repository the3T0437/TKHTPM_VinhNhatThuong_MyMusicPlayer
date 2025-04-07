package com.musicapp.mymusicplayer

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicapp.mymusicplayer.activities.FavoriteActitivy
import com.musicapp.mymusicplayer.activities.MusicDetailActivity
import com.musicapp.mymusicplayer.activities.SearchSongActivity
import com.musicapp.mymusicplayer.activities.PlayListActivity
import com.musicapp.mymusicplayer.activities.PlayingSongsActivity
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.MainLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.service.PlayBackService
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.songGetter
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var songs: ArrayList<Song>
    private var factory: ListenableFuture<MediaBrowser>? = null
    private lateinit var mediaController: MediaControllerWrapper
    private val mediaControllerThread = newSingleThreadContext("mediaControllerThread")
    private val getDataFromDatabasethread = newSingleThreadContext("getDataFromDatabase")
    private lateinit var databaseApi: DatabaseAPI

    private val mediaControllerListener: Player.Listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            hightlightPlayingSong()
        }
    }

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
        //val testRunner = test(this)
        //testRunner.runTests()
    }

    fun setup(){
        databaseApi = DatabaseAPI(this)
        setupRecyclerView()
        createMediaController()
        setupButtonFillter()
        setupSearch()
        setupMusicPlayer()
    }

    fun createMediaController(){
        val sessionToken = SessionToken(this, ComponentName(this, PlayBackService::class.java))
        factory = MediaBrowser.Builder(this, sessionToken).buildAsync()

        factory?.addListener(
            {
                // MediaController is available here with controllerFuture.get()
                store.mediaBrowser = factory?.let {
                    if (it.isDone){
                        return@let it.get()
                    }
                    else
                        null
                }
                binding.musicPlayer.mediaController = store.mediaBrowser
                store.mediaBrowser?.addListener(mediaControllerListener)
                mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
                adapter.mediaController = mediaController
            },
            MoreExecutors.directExecutor()
        )
    }


    private fun setupButtonFillter() {
        binding.btnFilter.setMenuResource(R.menu.menu_filter_options)
        binding.btnFilter.setThreeDotMenuListener(object : ThreeDotMenuListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.playlist -> {
                        val intent = Intent(this@MainActivity, PlayListActivity::class.java)
                        startActivity(intent)
                        return true
                    }

                    R.id.favorite -> {
                        val intent = Intent(this@MainActivity, FavoriteActitivy::class.java)
                        startActivity(intent)
                        return true
                    }
                }

                return false
            }
        })
    }

    private fun setupSearch(){
        binding.svSearch.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val intent = Intent(this, SearchSongActivity::class.java)
                startActivity(intent)
                binding.svSearch.clearFocus()
            }
        }
        binding.svSearch.setOnClickListener{
            val intent = Intent(this, SearchSongActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupMusicPlayer() {
        binding.musicPlayer.setOnMusicPlayerClickListener(object : MusicPlayerSmallClickListener {
            override fun onPauseClick() {
            }

            override fun onStartClick() {
                binding.musicPlayer.updateStateOfStartPauseButton()
            }

            override fun onNextClick() {
            }

            override fun onMenuClick() {
                val intent = Intent(this@MainActivity, PlayingSongsActivity::class.java)
                startActivity(intent)
            }

            override fun onMusicPlayerClick() {
                val intent = Intent(this@MainActivity, MusicDetailActivity::class.java)
                startActivity(intent)
            }
        })
    }

    fun setupRecyclerView(){
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SongAdapter(this, songs)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager

        adapter.setSongClickListener(object: SongClickListener {
            override fun onArtistClick(artist: String) {
            }

            override fun onSongClick(song: Song, position: Int) {
                Log.d("myLog", "playing songs count: ${mediaController.playingSongs.size}")
                mediaController.clear()
                mediaController.addSongs(songs)
                mediaController.seekToMediaItem(position)
                mediaController.prepare()
                mediaController.play()
            }
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        var job = updateSongsDatabase()
        job = updateArrSongs(job)
        hightlightPlayingSong(job)

        store.mediaBrowser?.let{
            mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
        }
    }

    private fun updateSongsDatabase(): Job{
        val arrSongs = arrayListOf<Song>()
        return databaseApi.getAllSongs(arrSongs, object: OnDatabaseCallBack{
            override fun onSuccess(id: Long) {
                if (arrSongs.size == songs.size)
                    return

                songs.clear()
                songs.addAll(arrSongs)
                songs.sortBy { it.title }
                adapter.notifyDataSetChanged()
                Log.d("concurrent", "1")
            }

            override fun onFailure(e: Exception) {
            }
        })
    }

    private fun updateArrSongs(jobForWainting: Job): Job{
        return CoroutineScope(getDataFromDatabasethread).launch {
            jobForWainting.join()
            Log.d("concurrent", "2")

            val localSongs = songGetter.getAllSongs(this@MainActivity)
            val databaseSongs = arrayListOf<Song>()
            databaseSongs.addAll(songs)
            val addSongs = arrayListOf<Song>()
            val removeSongs = arrayListOf<Song>()
            val newArr = getDifferencesSongs(databaseSongs, localSongs, addSongs, removeSongs)

            launch {
                for(song in addSongs){
                    databaseApi.insertSong(song, object: OnDatabaseCallBack{
                        override fun onSuccess(id: Long) {
                        }

                        override fun onFailure(e: Exception) {
                        }
                    })
                }

                for (song in removeSongs){
                    databaseApi.deleteSong(song.id, object : OnDatabaseCallBack{
                        override fun onSuccess(id: Long) {
                        }

                        override fun onFailure(e: Exception) {
                        }
                    })
                }
            }

            withContext(Dispatchers.Main){
                Log.d("myLog","add ${addSongs.size}, remove: ${removeSongs.size}")
                if (addSongs.size == 0 && removeSongs.size == 0)
                    return@withContext
                songs.clear()
                newArr.sortBy { it.title}
                songs.addAll(newArr)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun hightlightPlayingSong(jobForWainting: Job? = null): Job{
        return CoroutineScope(getDataFromDatabasethread).launch {
            jobForWainting?.join()

            withContext(Dispatchers.Main){
                adapter.currentPlayingSongId = mediaController.getCurrentSongId(this@MainActivity)
            }
        }
    }

    /*
     * proritize get songs from oldSongs
     * if this song id have in oldSongs, newSongs, all to resultSongs
     * if this song id don't have in newSongs, add to removeSongs
     * if this song id have in newSongs, but don't have in oldSongs, add to addSongs, resultSongs
     */
    private fun getDifferencesSongs(
        oldSongs: ArrayList<Song>,
        newSongs: ArrayList<Song>,
        addSongs: ArrayList<Song>,
        removeSongs: ArrayList<Song>,
    ): ArrayList<Song> {
        oldSongs.sortBy { it.id }
        newSongs.sortBy { it.id }
        addSongs.clear()
        removeSongs.clear()
        Log.d("myLog", "old songs: ${oldSongs.size}, new songs: ${newSongs.size}")

        val resultSongs = arrayListOf<Song>()
        var songIndex = 0;
        var tempIndex = 0;
        while (songIndex < oldSongs.size && tempIndex < newSongs.size) {
            val songId = oldSongs[songIndex].id
            val tempId = newSongs[tempIndex].id

            when {
                songId == tempId -> {
                    resultSongs.add(oldSongs[songIndex])
                    songIndex++
                    tempIndex++
                }

                songId < tempId -> {
                    songIndex++
                    removeSongs.add(oldSongs[songIndex])
                }

                songId > tempId -> {
                    resultSongs.add(newSongs[tempIndex])
                    addSongs.add(newSongs[tempIndex])
                    tempIndex++
                }
            }
        }

        while (songIndex < oldSongs.size) {
            removeSongs.add(oldSongs[songIndex])
            songIndex++
        }

        while (tempIndex < newSongs.size) {
            addSongs.add(newSongs[tempIndex])
            resultSongs.add(newSongs[tempIndex])
            tempIndex++
        }
        return resultSongs
    }

    override fun onDestroy() {
        super.onDestroy()
        store.mediaBrowser?.removeListener(mediaControllerListener)
    }
}