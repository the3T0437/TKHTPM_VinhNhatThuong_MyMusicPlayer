package com.musicapp.mymusicplayer.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.databinding.MusicPlayerSmallLayoutBinding
import com.musicapp.mymusicplayer.utils.songGetter

interface MusicPlayerSmallClickListener {
    fun onPauseClick()
    fun onStartClick()
    fun onNextClick()
    fun onMenuClick()
    fun onMusicPlayerClick()
}

class MusicPlaySmall : ConstraintLayout {
    private val context: Context;
    private lateinit var binding: MusicPlayerSmallLayoutBinding
    private var musicPlayerClickListener: MusicPlayerSmallClickListener? = null
    var mediaController: MediaController? = null
        set(value) {
            if (value == null) {
                field = null
                return
            }

            value.addListener(listener)
            field = value
        }

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

        val uri = mediaController!!.currentMediaItem?.localConfiguration?.uri
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

    private fun updateStateOfStartPauseButton() {
        if (mediaController == null) {
            binding.btnPauseStart.isSelected = false
            return
        }
        binding.btnPauseStart.isSelected = mediaController!!.isPlaying
    }

    private val callback: OnClickListener = object : OnClickListener {
        override fun onClick(v: View?) {
            if (v == null)
                return
            when (v.id) {
                binding.btnPauseStart.id -> {
                    if (mediaController?.currentMediaItem != null) {
                        if (binding.btnPauseStart.isSelected)
                            musicPlayerClickListener?.onPauseClick()
                        else
                            musicPlayerClickListener?.onStartClick()

                        updateStateOfStartPauseButton()
                    }
                }

                binding.btnNext.id -> {
                    musicPlayerClickListener?.onNextClick()
                }

                binding.btnMenu.id -> {
                    musicPlayerClickListener?.onMenuClick()
                }

                else -> {
                    musicPlayerClickListener?.onMusicPlayerClick()
                }
            }
        }
    }

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

    private fun setUp(context: Context) {
        binding = MusicPlayerSmallLayoutBinding.inflate(LayoutInflater.from(context), this, true)

        binding.btnMenu.setOnClickListener(callback)
        binding.btnNext.setOnClickListener(callback)
        binding.btnPauseStart.setOnClickListener(callback)
        binding.root.setOnClickListener(callback)
    }

    fun setOnMusicPlayerClickListener(musicPlayerSmallClickListener: MusicPlayerSmallClickListener) {
        musicPlayerClickListener = musicPlayerSmallClickListener
    }
}