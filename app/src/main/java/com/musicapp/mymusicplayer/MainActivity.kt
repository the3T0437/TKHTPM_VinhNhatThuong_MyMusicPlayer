package com.musicapp.mymusicplayer

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicapp.mymusicplayer.activities.Albums_Activity
import com.musicapp.mymusicplayer.activities.ArtistsActivity
import com.musicapp.mymusicplayer.activities.FavoriteActitivy
import com.musicapp.mymusicplayer.activities.MusicDetailActivity
import com.musicapp.mymusicplayer.activities.SearchSongActivity
import com.musicapp.mymusicplayer.activities.PlayListActivity
import com.musicapp.mymusicplayer.activities.PlayingSongsActivity
import com.musicapp.mymusicplayer.adapters.SongAdapter
import com.musicapp.mymusicplayer.adapters.SongClickListener
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.MainLayoutBinding
import com.musicapp.mymusicplayer.model.Artist
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.permission.PermissionHelper
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
import java.util.Collections

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainLayoutBinding
    private lateinit var adapter: SongAdapter
    private lateinit var songs: ArrayList<Song>
    private var factory: ListenableFuture<MediaBrowser>? = null
    private lateinit var mediaController: MediaControllerWrapper
    private val mediaControllerThread = newSingleThreadContext("mediaControllerThread")
    private val getDataFromDatabasethread = newSingleThreadContext("getDataFromDatabase")
    private lateinit var databaseApi: DatabaseAPI
    private lateinit var jobUpdateArtist: Job
    private var isArrSongUpdated = false
    private lateinit var permissionHelper: PermissionHelper
    private var sortByTitle = true

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

        permissionHelper = PermissionHelper(this)

        if (!permissionHelper.hasPermissions()) {
            permissionHelper.requestPermissions()
        }

        songs = store.songs
        setup()
    }
    private fun loadSongs(): ArrayList<Song> {
        // Load your songs here from database or other sources
        return arrayListOf()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionHelper.handlePermissionsResult(
            requestCode,
            grantResults,
            onGranted = {
                Toast.makeText(this, "Đã cấp quyền cho ứng dụng!", Toast.LENGTH_SHORT).show()
                setup()
            },
            onDenied = {
                Toast.makeText(this, "Quyền bị từ chối! Ứng dụng có thể không hoạt động !.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun setup(){
        Toast.makeText(this, "Ứng dụng đã mở!", Toast.LENGTH_SHORT).show()

        databaseApi = DatabaseAPI(this)
        setupRecyclerView()
        createMediaController()
        setupButtonFillter()
        setupSearch()
        setupMusicPlayer()
        setupButtonPlayBig()
        setupSortButton()
        binding.btnSetting.setOnClickListener {
            showSleepTimerDialog()
        }
    }
    private fun showSleepTimerDialog() {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "Nhập thời gian (phút)"
        input.inputType = InputType.TYPE_CLASS_NUMBER

        builder.setTitle("Hẹn giờ đi ngủ")
        builder.setView(input)
        builder.setPositiveButton("Bắt đầu") { _, _ ->
            val minutes = input.text.toString().toIntOrNull()
            if (minutes != null && minutes > 0) {
                startSleepTimer(minutes)
            } else {
                Toast.makeText(this, "Vui lòng nhập thời gian hợp lệ!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    // Hàm bắt đầu hẹn giờ tắt nhạc
    private fun startSleepTimer(minutes: Int) {
        val milliseconds = minutes * 60 * 1000L
        val timer = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Hiển thị thời gian còn lại (nếu cần)
            }

            override fun onFinish() {
                stopMusic()
            }
        }
        timer.start()
        Toast.makeText(this, "Hẹn giờ đi ngủ trong $minutes phút.", Toast.LENGTH_SHORT).show()
    }

    // Hàm dừng phát nhạc
    private fun stopMusic() {
        mediaController.pause() // Hoặc mediaController.stop() nếu cần
        Toast.makeText(this, "Nhạc đã dừng!", Toast.LENGTH_SHORT).show()
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
                store.mediaBrowser?.addListener(mediaControllerListener)
                mediaController = MediaControllerWrapper.getInstance(store.mediaBrowser)
                binding.musicPlayer.mediaController = mediaController
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

                    R.id.artists -> {
                        val intent = Intent(this@MainActivity, ArtistsActivity::class.java)
                        startActivity(intent)
                        return true
                    }
                    R.id.album->{
                        val intent = Intent(this@MainActivity, Albums_Activity::class.java)
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
        binding.musicPlayer.songs = songs
    }

    private fun setupButtonPlayBig(){
        binding.btnPlayBig.setOnClickListener{
            playAllSong(isShuffle = true)
        }
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener {
            sortByTitle = !sortByTitle
            if (sortByTitle) {
                songs.sortBy { it.title.lowercase() }
            } else {
                songs.sortBy { it.artist.lowercase() }
            }
            adapter.notifyDataSetChanged()
        }
    }

    fun setupRecyclerView(){
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SongAdapter(this, songs)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager

        //makeDragable()
    }

    private fun makeDragable() {
        // Gắn ItemTouchHelper vô RecyclerView
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(dragFlags, 0) // bật chỉ kéo
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.moveItem(fromPosition, toPosition) // cập nhật thứ tự bài hát trong Adapter

                updateMediaControllerAfterReorder()
                return true
            }

            private fun updateMediaControllerAfterReorder() {
                val currentPlayingIndex =
                    songs.indexOfFirst { it.id == adapter.currentPlayingSongId }
                if (currentPlayingIndex != -1) {
                    mediaController.clear()
                    mediaController.addSongs(songs) // cập nhật danh sách phát
                    mediaController.seekToMediaItem(currentPlayingIndex) // Đặt lại bài hát hiện tại
                    mediaController.prepare()
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // ko cần xl vuốt
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true // xl để cho phép nhấn giữ để kéo
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun playAllSong(position: Int = 0, isShuffle : Boolean = false){
        mediaController.clear()
        mediaController.setShuffleMode(isShuffle)
        mediaController.addSongs(songs)
        mediaController.seekToMediaItem(position)
        mediaController.prepare()
        mediaController.play()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        var jobUpdateDatabase : Job? = null
        if (songs.size == 0)
            jobUpdateDatabase = updateSongsDatabase()

        var jobUpdateSongsMemory = updateArrSongs(jobUpdateDatabase)
        jobUpdateArtist = updateArist(jobUpdateSongsMemory)
        store.mediaBrowser?.let{ hightlightPlayingSong(jobUpdateDatabase) }

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
                songs.sortBy { it.title.lowercase() }
                adapter.notifyDataSetChanged()
                Log.d("concurrent", "1")
            }

            override fun onFailure(e: Exception) {
            }
        })
    }

    private fun updateArrSongs(jobForWainting: Job?): Job{
        return CoroutineScope(getDataFromDatabasethread).launch {
            jobForWainting?.join()
            Log.d("concurrent", "2")

            val localSongs = songGetter.getAllSongs(this@MainActivity)
            val databaseSongs = arrayListOf<Song>()
            databaseSongs.addAll(songs)
            val addSongs = arrayListOf<Song>()
            val removeSongs = arrayListOf<Song>()
            val newArr = getDifferencesSongs(databaseSongs, localSongs, addSongs, removeSongs)

            launch {
                for(song in addSongs){
                    databaseApi.insertSong(song, DatabaseAPI.onDatabaseCallBackDoNothing)
                }

                for (song in removeSongs){
                    databaseApi.deleteSong(song.id, DatabaseAPI.onDatabaseCallBackDoNothing)
                }
            }

            withContext(Dispatchers.Main){
                Log.d("myLog","add ${addSongs.size}, remove: ${removeSongs.size}")
                if (addSongs.size == 0 && removeSongs.size == 0)
                    return@withContext
                isArrSongUpdated = true
                songs.clear()
                newArr.sortBy { it.title.lowercase()}
                songs.addAll(newArr)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateArist(jobForWainting: Job?): Job{
        return CoroutineScope(getDataFromDatabasethread).launch {
            jobForWainting?.join()

            val finalSongs = Collections.unmodifiableList(songs)

            finalSongs.stream().forEach {
                databaseApi.insertArtistBySong(it, DatabaseAPI.onDatabaseCallBackDoNothing)
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