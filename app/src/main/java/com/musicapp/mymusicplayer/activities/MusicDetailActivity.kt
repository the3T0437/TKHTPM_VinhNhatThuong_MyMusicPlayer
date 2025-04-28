package com.musicapp.mymusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.database.OnGetItemCallback
import com.musicapp.mymusicplayer.databinding.MusicDetailLayoutBinding
import com.musicapp.mymusicplayer.model.FavoriteSong
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.songGetter
import com.musicapp.mymusicplayer.utils.store
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
            updateStateStartPauseButton()
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
        mediaController = store.mediaBrowser
        mediaController?.addListener(playerListener)
        databaseApi = DatabaseAPI(this)
        coroutineScope = CoroutineScope(Dispatchers.IO + Job())

        updateSeekbar()
        setupUpdateSeekbar()
        setEvents()
    }

    private fun setupUpdateSeekbar(){
        coroutineScope.launch {
            while(true){
                delay(1000)
                if (isUpdateSeekbar == false)
                    continue

                withContext(Dispatchers.Main){
                    updateSeekbar()
                }
            }
        }
    }

    private fun setEvents(){
        setEventButtonNext()
        setEventButtonPrevious()
        setEventButtonContinue()
        setEventButtonMode()
        setEventButtonDown()
        setEventSeekbar()
        setEventButtonFavorite()
        setEventBtnMenu()
    }

    private fun setEventButtonNext(){
        binding.btnNext.setOnClickListener{
            if (mediaController != null && mediaController!!.hasNextMediaItem())
                mediaController?.seekToNextMediaItem()
            else if (mediaController != null && !mediaController!!.hasNextMediaItem() &&
                getStateMode() == MusicPlayerMode.Repeat && mediaController?.mediaItemCount != 0){
                mediaController?.seekTo(0, 0)
            }
        }
    }

    private fun setEventButtonPrevious(){
        binding.btnPrevious.setOnClickListener{
            if (mediaController != null && mediaController!!.hasPreviousMediaItem())
                mediaController?.seekToPreviousMediaItem()
        }
    }

    private fun setEventButtonContinue(){
        binding.btnContinue.setOnClickListener{
            if (binding.btnContinue.isSelected == false && mediaController != null)
                mediaController?.play()
            else if (binding.btnContinue.isSelected == true && mediaController != null)
                mediaController?.pause()

            updateStateStartPauseButton()
        }
    }

    private fun setEventButtonMode(){
        binding.btnMode.setOnClickListener{
            if (mode == MusicPlayerMode.Line){
                mode = MusicPlayerMode.Circle
            }
            else if (mode == MusicPlayerMode.Circle){
                mode = MusicPlayerMode.Repeat
            }
            else if (mode == MusicPlayerMode.Repeat){
                mode = MusicPlayerMode.Shuffle
            }
            else if (mode == MusicPlayerMode.Shuffle){
                mode = MusicPlayerMode.Line
            }

            updateModePlayer()
        }
    }

    private fun setEventButtonDown(){
        binding.btnDown.setOnClickListener{
            this@MusicDetailActivity.finish()
        }
    }

    private fun setEventSeekbar(){
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
            }
        })
    }

    private fun setEventButtonFavorite() {
        binding.btnFavorite.setOnClickListener {
            val mediaItem = mediaController?.currentMediaItem
            if (mediaItem == null) {
                binding.btnFavorite.isSelected = false
                return@setOnClickListener
            }

            val uri = mediaItem.localConfiguration?.uri
            if (uri == null) {
                binding.btnFavorite.isSelected = false
                return@setOnClickListener
            }

            val song = songGetter.getSong(this, uri)
            if (song == null) {
                binding.btnFavorite.isSelected = false
                return@setOnClickListener
            }

            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                val favorite = databaseApi?.getFavorite(song.id)
                val favoriteId = favorite?.id ?: -1

                if (favoriteId == -1) {
                    databaseApi?.insertFavoriteSong(FavoriteSong(song.id), object : OnDatabaseCallBack {
                        override fun onSuccess(id: Long) {
                            binding.btnFavorite.isSelected = true
                        }
                        override fun onFailure(e: Exception) {
                            Toast.makeText(this@MusicDetailActivity, "Không thể thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    databaseApi?.deleteFavroiteSong(favoriteId, object : OnDatabaseCallBack {
                        override fun onSuccess(id: Long) {
                            binding.btnFavorite.isSelected = false
                        }
                        override fun onFailure(e: Exception) {
                            Toast.makeText(this@MusicDetailActivity, "Không thể xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }

    private fun setEventBtnMenu(){
        binding.btnMenu.setOnClickListener{
            val intent = Intent(this@MusicDetailActivity, PlayingSongsActivity::class.java)
            startActivity(intent)
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
            setDefaultMusicDetail()
            return
        }

        val uri = mediaItem.localConfiguration?.uri
        if (uri == null) {
            setDefaultMusicDetail()
            return
        }

        val song = songGetter.getSong(this, uri)
        if (song == null) {
            setDefaultMusicDetail()
            return
        }

        databaseApi?.getSong(song.id, object: OnGetItemCallback{
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

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val favorite = databaseApi?.getFavorite(song.id)
            binding.btnFavorite.isSelected = favorite != null
        }

        updateSeekbar()
        mode = getStateMode()
        updateModePlayer()
        updateStateStartPauseButton()
    }

    private fun setDefaultMusicDetail(){
        binding.tvTitle.setText("unknown")
        binding.tvArtist.setText("unknown")
        binding.imgThumbnail.setImageResource(this.DEFAULT_THUMBNAIL)
        binding.btnFavorite.isSelected = false
    }

    private fun updateStateStartPauseButton(){
        if (mediaController == null){
            binding.btnContinue.isSelected = false
            return
        }

        binding.btnContinue.isSelected = mediaController!!.isPlaying
    }

    private fun getStateMode(): MusicPlayerMode{
        if (mediaController == null)
            return MusicPlayerMode.Line;
        if (mediaController!!.repeatMode == Player.REPEAT_MODE_OFF && mediaController!!.shuffleModeEnabled == false)
            return MusicPlayerMode.Line;
        if (mediaController?.repeatMode == Player.REPEAT_MODE_ONE)
            return MusicPlayerMode.Repeat;
        if (mediaController?.repeatMode == Player.REPEAT_MODE_ALL)
            return MusicPlayerMode.Circle;

        return MusicPlayerMode.Shuffle;
    }

    private fun updateModePlayer(){
        if (mode == MusicPlayerMode.Line){
            mediaController?.repeatMode = Player.REPEAT_MODE_OFF
            mediaController?.shuffleModeEnabled = false
            binding.btnMode.setImageResource(R.drawable.line)
        }
        else if (mode == MusicPlayerMode.Circle){
            mediaController?.repeatMode = Player.REPEAT_MODE_ALL
            mediaController?.shuffleModeEnabled = false
            binding.btnMode.setImageResource(R.drawable.music_mode_circle)
        }
        else if (mode == MusicPlayerMode.Repeat){
            mediaController?.repeatMode = Player.REPEAT_MODE_ONE
            mediaController?.shuffleModeEnabled = false
            binding.btnMode.setImageResource(R.drawable.music_mode_repeat)
        }
        else if (mode == MusicPlayerMode.Shuffle){
            mediaController?.repeatMode = Player.REPEAT_MODE_OFF
            mediaController?.shuffleModeEnabled = true
            binding.btnMode.setImageResource(R.drawable.shuffle)
        }
    }

    fun updateSeekbar(){
        var process: Int = 0 ;
        var duration: Int = 0;
        process = ((mediaController?.currentPosition ?: 0) / 1000).toInt()
        duration = ((mediaController?.contentDuration ?: 100000)/ 1000).toInt()
        Log.d("myLog", "seekbar: $process $duration")
        binding.seekbar.setProgress(process, true)
        binding.seekbar.max = duration
    }
}