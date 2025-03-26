package com.musicapp.mymusicplayer.activities

import android.content.ComponentName
import android.media.browse.MediaBrowser.MediaItem
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.media3.common.Player.RepeatMode
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.MainLayoutBinding
import com.musicapp.mymusicplayer.databinding.MusicDetailLayoutBinding
import com.musicapp.mymusicplayer.model.FavoriteSong
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.service.PlayBackService
import com.musicapp.mymusicplayer.utils.songGetter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MusicDetailActivity : AppCompatActivity() {
    private var databaseApi: DatabaseAPI? = null
    private val playerListener = object:Player.Listener{
        override fun onMediaItemTransition(
            mediaItem: androidx.media3.common.MediaItem?,
            reason: Int
        ) {
            super.onMediaItemTransition(mediaItem, reason)
            this@MusicDetailActivity.onResume()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            this@MusicDetailActivity.onResume()
        }
    }
    private val DEFAULT_THUMBNAIL = R.drawable.thumbnail
    enum class MusicPlayerMode{
        Circle, Repeat, Shuffle, Line
    }
    private lateinit var binding: MusicDetailLayoutBinding
    private var mode: MusicPlayerMode = MusicPlayerMode.Line
    private var mediaController: MediaController? = null
    private lateinit var coroutineScope: CoroutineScope
    //if true, will update seekbar every second
    private var isUpdateSeekbar: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = MusicDetailLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setup()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController?.removeListener(playerListener)
    }

    @OptIn(UnstableApi::class)
    fun setup(){
        createMediaController()
        databaseApi = DatabaseAPI(this)

        coroutineScope = CoroutineScope(Dispatchers.IO + Job())
        setupUpdateSeekbar()

        binding.btnNext.setOnClickListener{
            if (mediaController != null && mediaController!!.hasNextMediaItem())
                mediaController?.seekToNextMediaItem()
        }
        binding.btnPrevious.setOnClickListener{
            if (mediaController != null && mediaController!!.hasPreviousMediaItem())
                mediaController?.seekToPreviousMediaItem()
        }
        binding.btnContinue.setOnClickListener{
            if (binding.btnContinue.isSelected == false && mediaController != null)
                mediaController?.play()
            else if (binding.btnContinue.isSelected == true && mediaController != null)
                mediaController?.pause()

            updateStateStartPauseButton()
        }

        binding.btnMode.setOnClickListener{
            if (mode == MusicPlayerMode.Line){
                binding.btnMode.setImageResource(R.drawable.music_mode_circle)
                mode = MusicPlayerMode.Circle
            }
            else if (mode == MusicPlayerMode.Circle){
                binding.btnMode.setImageResource(R.drawable.music_mode_repeat)
                mode = MusicPlayerMode.Repeat
            }
            else if (mode == MusicPlayerMode.Repeat){
                binding.btnMode.setImageResource(R.drawable.shuffle)
                mode = MusicPlayerMode.Shuffle
            }
            else if (mode == MusicPlayerMode.Shuffle){
                binding.btnMode.setImageResource(R.drawable.music_mode_repeat)
                mode = MusicPlayerMode.Line
            }

            updateModePlayer()
        }

        binding.btnDown.setOnClickListener{
            this@MusicDetailActivity.finish()
        }

        binding.seekbar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("myLog", "process bar ${seekBar?.progress}")
                mediaController?.seekTo((seekBar?.progress ?: 0).toLong() * 1000)
                isUpdateSeekbar = true
                this@MusicDetailActivity.onResume()
            }
        })

        binding.btnFavorite.setOnClickListener{
            var favoriteId: Int = -1

            val mediaItem = mediaController?.currentMediaItem
            if (mediaItem == null){
                binding.btnFavorite.isSelected = false
                return@setOnClickListener
            }

            val uri = mediaItem.localConfiguration?.uri!!
            val songId : Long? = songGetter.getSong(this, uri)?.id
            if (songId == null){
                binding.btnFavorite.isSelected = false
                return@setOnClickListener
            }

            databaseApi?.getFavorite(songId, object: OnGetItemCallback{
                override fun onSuccess(value: Any) {
                    favoriteId = (value as FavoriteSong).id
                }

                override fun onFailure(e: Exception) {
                    favoriteId = -1
                }
            })

            /*
            runBlocking {
                databaseApi?.join()
                if (favoriteId == -1){
                    databaseApi?.insertFavoriteSong(FavoriteSong(songId), object: OnDatabaseCallBack{
                        override fun onSuccess(id: Long) {
                        }

                        override fun onFailure(e: Exception) {
                        }
                    })

                    binding.btnFavorite.isSelected = true
                    return@runBlocking
                }

                databaseApi?.deleteFavroiteSong(favoriteId, object: OnDatabaseCallBack{
                    override fun onSuccess(id: Long) {
                    }

                    override fun onFailure(e: Exception) {
                    }
                })

                binding.btnFavorite.isSelected = false
                return@runBlocking
            }
             */
        }
    }

    fun createMediaController(){
        val sessionToken = SessionToken(this, ComponentName(this, PlayBackService::class.java))
        val factory: ListenableFuture<MediaController> = MediaController.Builder(this, sessionToken).buildAsync()

        var tempMediaController: MediaController? = null
        factory.addListener(
            {
                // MediaController is available here with controllerFuture.get()
                this.mediaController = factory.let {
                    if (it.isDone)
                        it.get()
                    else
                        null
                }

                this.mediaController?.addListener(playerListener)

                this@MusicDetailActivity.onResume()
            },
            MoreExecutors.directExecutor()
        )
    }

    fun setupUpdateSeekbar(){
        coroutineScope.launch {
            while(this@MusicDetailActivity.isUpdateSeekbar){
                delay(1000)
                var isFineMediaControl = false;
                var process: Int = 0 ;
                var duration: Int = 0;
                withContext(Dispatchers.Main){
                    isFineMediaControl = mediaController != null && mediaController!!.currentMediaItem != null
                    if (isFineMediaControl){
                        process = (mediaController!!.currentPosition/ 1000).toInt()
                        duration = (mediaController!!.contentDuration / 1000).toInt()
                        Log.d("myLog", "seekbar: $process $duration")
                    }
                }
                if (isFineMediaControl){
                    binding.seekbar.progress = process
                    binding.seekbar.max = duration
                }
            }
        }
    }

    override fun onStop() {
        this.coroutineScope.cancel()
        super.onStop()
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        Log.d("myLog", "start resume")
        val mediaItem = mediaController?.currentMediaItem

        if (mediaItem == null){
            binding.tvTitle.setText("Not playing music")
            binding.tvArtist.setText("Unknown")
            binding.seekbar.progress = 0
            return;
        }

        val uri = mediaItem.localConfiguration?.uri!!
        val songId : Long? = songGetter.getSong(this, uri)?.id
        if (songId == null)
            setDefaultMusicDetail()
        else{
            databaseApi?.getSong(songId, object: OnGetItemCallback{
                override fun onSuccess(value: Any) {
                    val song: Song = value as Song

                    try{
                        val bitmap = this@MusicDetailActivity.contentResolver.loadThumbnail(uri, Size(640, 480), null)
                        binding.imgThumbnail.setImageBitmap(bitmap)
                    }catch (e: Exception){
                        binding.imgThumbnail.setImageResource(this@MusicDetailActivity.DEFAULT_THUMBNAIL)
                    }

                    binding.tvTitle.setText(song.title)
                    binding.tvArtist.setText(song.artist)
                }

                override fun onFailure(e: Exception) {
                    setDefaultMusicDetail()
                }
            })

            databaseApi?.getFavorite(songId, object: OnGetItemCallback{
                override fun onSuccess(value: Any) {
                    binding.btnFavorite.isSelected = value != null
                }

                override fun onFailure(e: Exception) {
                    binding.btnFavorite.isSelected = false
                }
            })
        }
        updateModePlayer()
        updateStateStartPauseButton()
    }

    private fun setDefaultMusicDetail(){
        binding.tvTitle.setText("unknown")
        binding.tvArtist.setText("unknown")
        binding.imgThumbnail.setImageResource(this.DEFAULT_THUMBNAIL)

    }

    private fun updateStateStartPauseButton(){
        if (mediaController == null){
            binding.btnContinue.isSelected = false
            return
        }

        binding.btnContinue.isSelected = mediaController!!.isPlaying
    }

    fun updateModePlayer(){
        if (mode == MusicPlayerMode.Line){
            mediaController?.repeatMode = Player.REPEAT_MODE_OFF
            mediaController?.shuffleModeEnabled = false
        }
        else if (mode == MusicPlayerMode.Circle){
            mediaController?.repeatMode = Player.REPEAT_MODE_ALL
            mediaController?.shuffleModeEnabled = false
        }
        else if (mode == MusicPlayerMode.Repeat){
            mediaController?.repeatMode = Player.REPEAT_MODE_ONE
            mediaController?.shuffleModeEnabled = false
        }
        else if (mode == MusicPlayerMode.Shuffle){
            mediaController?.repeatMode = Player.REPEAT_MODE_OFF
            mediaController?.shuffleModeEnabled = true
        }
    }
}