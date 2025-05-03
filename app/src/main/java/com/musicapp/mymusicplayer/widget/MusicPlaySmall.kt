package com.musicapp.mymusicplayer.widget

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.activities.MusicDetailActivity
import com.musicapp.mymusicplayer.activities.PlayingSongsActivity
import com.musicapp.mymusicplayer.databinding.MusicPlayerSmallLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.utils.songGetter


interface MusicPlayerSmallClickListener{
    fun onPauseClick()
    fun onStartClick()
    fun onNextClick()
    fun onMenuClick()
    fun onMusicPlayerClick()
}

/*
 * need to init mediaController, songs to make it work
 */
class MusicPlaySmall : ConstraintLayout {
    private val context: Context;
    private lateinit var binding: MusicPlayerSmallLayoutBinding
    private var musicPlayerClickListener: MusicPlayerSmallClickListener? = null
    var mediaController: MediaControllerWrapper? = null
        set(value) {
            field = value
            field?.addListener(listener)
            updateState()
        }
    var songs: ArrayList<Song>? = null


    private val listener: Player.Listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            this@MusicPlaySmall.updateState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            this@MusicPlaySmall.updateState()
        }
    }

    private fun updateState() {
        if (mediaController == null) {
            binding.img.setImageResource(R.drawable.thumbnail)
            binding.tvTitle.setText("title")
            binding.tvArtist.setText("artist")
            updateStateOfStartPauseButton()
            return
        }

        val uri = mediaController!!.getCurrentMediaItem()?.localConfiguration?.uri
        if (uri == null)
            return;
        try {
            val bitmap = context.contentResolver.loadThumbnail(uri!!, Size(640, 480), null)
            binding.img.setImageBitmap(bitmap)
        } catch (e: Exception) {
            binding.img.setImageResource(R.drawable.thumbnail)
        }

        val song = songGetter.getSong(context, uri)
        binding.tvTitle.setText(song!!.title)
        binding.tvArtist.setText(song!!.artist)

        updateStateOfStartPauseButton()
    }

    fun updateStateOfStartPauseButton() {
        if (mediaController == null) {
            binding.btnPauseStart.isSelected = false
            return
        }
        binding.btnPauseStart.isSelected = mediaController!!.isPlaying()
    }

    private var callback: OnClickListener? = null

    constructor(context: Context) : super(context) {
        this.context = context
        setUp(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context;
        setUp(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.context = context
        setUp(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        this.context = context
        setUp(context)
    }

    fun setSongInfo(title: String ,artist :String)
    {
        binding.tvTitle.text = title
        binding.tvArtist.text = artist
    }


    private fun setUp(context: Context) {
        binding = MusicPlayerSmallLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        setUpCallback()

        binding.btnMenu.setOnClickListener(callback)
        binding.btnNext.setOnClickListener(callback)
        binding.btnPauseStart.setOnClickListener(callback)
        binding.root.setOnClickListener(callback)
    }

    private fun setUpCallback(){
        callback = object : OnClickListener {
            override fun onClick(v: View?) {
                if (v == null)
                    return

                when (v.id) {
                    binding.btnPauseStart.id -> onBtnPauseStartClicked()
                    binding.btnNext.id -> onButtonNextClicked()
                    binding.btnMenu.id -> onMenuClicked()
                    else ->  onMusicPlayerClicked()
                }
            }

            private fun onBtnPauseStartClicked() {
                if (binding.btnPauseStart.isSelected) {
                    onPauseClicked()
                } else {
                    onStartClicked()
                }

                updateStateOfStartPauseButton()
            }

            private fun onPauseClicked() {
                mediaController?.pause()
                musicPlayerClickListener?.onPauseClick()
            }

            private fun onStartClicked() {
                mediaController?.play()
                if (mediaController?.getSize() == 0)
                    playAllSong()
                updateStateOfStartPauseButton()

                musicPlayerClickListener?.onStartClick()
            }

            private fun onButtonNextClicked() {
                musicPlayerClickListener?.onNextClick()
                if (mediaController != null && mediaController!!.hasNextMediaItem())
                    mediaController?.seekToNextMediaItem()
            }

            private fun onMenuClicked() {
                musicPlayerClickListener?.onMenuClick()

                val intent = Intent(context, PlayingSongsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(intent)
            }

            private fun onMusicPlayerClicked() {
                musicPlayerClickListener?.onMusicPlayerClick()

                val intent = Intent(context, MusicDetailActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(intent)
            }
        }
    }

    fun setOnMusicPlayerClickListener(musicPlayerSmallClickListener: MusicPlayerSmallClickListener) {
        musicPlayerClickListener = musicPlayerSmallClickListener
    }

    private fun playAllSong(position: Int = 0, isShuffle : Boolean = false){
        if (songs == null)
            return

        mediaController?.apply {
            clear()
            setShuffleMode(isShuffle)
            addSongs(songs!!)
            seekToMediaItem(position)
            prepare()
            play()
        }
    }
}