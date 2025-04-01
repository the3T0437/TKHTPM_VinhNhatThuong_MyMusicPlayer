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
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicapp.mymusicplayer.activities.FavoriteActitivy
import com.musicapp.mymusicplayer.activities.MusicDetailActivity
import com.musicapp.mymusicplayer.activities.PlaylistActivity
import com.musicapp.mymusicplayer.activities.PlayingSongsActivity
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.MainLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.service.PlayBackService
import com.musicapp.mymusicplayer.utils.songGetter
import com.musicapp.mymusicplayer.utils.store
import com.musicapp.mymusicplayer.widget.MusicPlayerSmallClickListener
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var songs: ArrayList<Song>
    private var factory: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val mediaControllerThread = newSingleThreadContext("mediaControllerThread")
    private val getDataFromDatabasethread = newSingleThreadContext("getDataFromDatabase")
    private lateinit var databaseApi: DatabaseAPI
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

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        updateArrSongs()

        mediaController = store.mediaController
    }

    private fun updateArrSongs(){
        runBlocking {

            CoroutineScope(getDataFromDatabasethread).launch {
                updateSongsDatabase().join()
                Log.d("concurrent", "2")

                val arr = songGetter.getAllSongs(this@MainActivity)
                arr.sortBy { it.id }
                val oldSongs = arrayListOf<Song>()
                oldSongs.addAll(songs)
                oldSongs.sortBy { it.id }

                val arrAddSongs = arrayListOf<Song>()
                val arrRemoveSongIds = arrayListOf<Long>()
                Log.d("myLog", "old songs: ${oldSongs.size}, new songs: ${arr.size}")

                val newArr = arrayListOf<Song>()
                var songIndex = 0;
                var tempIndex = 0;
                while (songIndex < oldSongs.size && tempIndex < arr.size){
                    val songId = oldSongs[songIndex].id
                    val tempId = arr[tempIndex].id

                    when{
                        songId == tempId ->{
                            newArr.add(oldSongs[songIndex])
                            songIndex ++
                            tempIndex ++
                        }
                        songId < tempId ->{
                            songIndex++
                            arrRemoveSongIds.add(songId)
                        }
                        songId > tempId ->{
                            newArr.add(arr[tempIndex])
                            arrAddSongs.add(arr[tempIndex])
                            tempIndex++
                        }
                    }
                }

                while (songIndex < oldSongs.size){
                    arrRemoveSongIds.add(oldSongs[songIndex].id)
                    songIndex++
                }

                while (tempIndex < arr.size){
                    arrAddSongs.add(arr[tempIndex])
                    newArr.add(arr[tempIndex])
                    tempIndex++
                }

                launch {
                    for(song in arrAddSongs){
                        databaseApi.insertSong(song, object: OnDatabaseCallBack{
                            override fun onSuccess(id: Long) {
                            }

                            override fun onFailure(e: Exception) {
                            }
                        })
                    }

                    for (id in arrRemoveSongIds){
                        databaseApi.deleteSong(id, object : OnDatabaseCallBack{
                            override fun onSuccess(id: Long) {
                            }

                            override fun onFailure(e: Exception) {
                            }
                        })
                    }
                }

                withContext(Dispatchers.Main){
                    Log.d("myLog","add ${arrAddSongs.size}, remove: ${arrRemoveSongIds.size}")
                    if (arrAddSongs.size == 0 && arrRemoveSongIds.size == 0)
                        return@withContext
                    songs.clear()
                    newArr.sortBy { it.title}
                    songs.addAll(newArr)
                    adapter.notifyDataSetChanged()
                }
            }
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

    fun setup(){
        databaseApi = DatabaseAPI(this)
        createMediaController()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SongAdapter(this, songs)

        binding.btnFilter.setMenuResource(R.menu.menu_filter_options)
        binding.btnFilter.setThreeDotMenuListener(object: ThreeDotMenuListener{
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when(item.itemId){
                    R.id.playlist->{
                        val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
                        startActivity(intent)
                        return true
                    }
                    R.id.favorite->{
                        val intent = Intent(this@MainActivity, FavoriteActitivy::class.java)
                        startActivity(intent)
                        return true
                    }
                }

                return false
            }
        })

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager

        binding.musicPlayer.setOnMusicPlayerClickListener(object: MusicPlayerSmallClickListener{
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
                val intent = Intent(this@MainActivity, PlayingSongsActivity::class.java)
                startActivity(intent)
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

            override fun onSongClick(song: Song, position: Int) {
                val mediaItem = MediaItem.fromUri(song.getUri())
                mediaController?.removeMediaItems(0, mediaController?.mediaItemCount?:0)
                mediaController?.addMediaItem(mediaItem)
                val coroutineScope = CoroutineScope(mediaControllerThread + Job())
                coroutineScope.launch {
                    val mediaItems = arrayListOf<MediaItem>()
                    for (i in 0 until position){
                        val mediaItem = MediaItem.fromUri(songs[i].getUri())
                        mediaItems.add(mediaItem)
                    }
                    withContext(Dispatchers.Main){
                        mediaController?.addMediaItems(0, mediaItems)
                    }
                }
                coroutineScope.launch {
                    val mediaItems = arrayListOf<MediaItem>()
                    for (i in position+1 until songs.size){
                        val mediaItem = MediaItem.fromUri(songs[i].getUri())
                        mediaItems.add(mediaItem)
                    }
                    withContext(Dispatchers.Main){
                        mediaController?.addMediaItems(mediaItems)
                    }
                }

                store.playingSongs?.addAll(songs)
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
                store.mediaController = factory?.let {
                    if (it.isDone)
                        it.get()
                    else
                        null
                }
                mediaController = store.mediaController
                binding.musicPlayer.mediaController = mediaController
            },
            MoreExecutors.directExecutor()
        )

    }
}