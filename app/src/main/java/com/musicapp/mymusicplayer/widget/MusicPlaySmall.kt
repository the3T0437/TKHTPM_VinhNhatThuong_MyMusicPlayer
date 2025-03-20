package com.musicapp.mymusicplayer.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.musicapp.mymusicplayer.databinding.MusicPlayerSmallLayoutBinding

interface MusicPlayerSmallClickListener{
    fun onPauseClick()
    fun onStartClick()
    fun onNextClick()
    fun onMenuClick()
    fun onMusicPlayerClick()
}

class MusicPlaySmall : ConstraintLayout{
    private val context: Context;
    private lateinit var binding: MusicPlayerSmallLayoutBinding
    private var musicPlayerClickListener: MusicPlayerSmallClickListener? = null
    var isDisablePauseStartBtn : Boolean = false

    private val callback: OnClickListener = object :OnClickListener{
        override fun onClick(v: View?) {
            if (v == null)
                return

            when(v.id){
                binding.btnPauseStart.id ->{
                    if (isDisablePauseStartBtn != false){
                        if (binding.btnPauseStart.isSelected)
                            musicPlayerClickListener?.onStartClick()
                        else
                            musicPlayerClickListener?.onPauseClick()

                        val nextState = !binding.btnPauseStart.isSelected
                        binding.btnPauseStart.isSelected = nextState
                    }
                }
                binding.btnNext.id ->{
                    musicPlayerClickListener?.onNextClick()
                }
                binding.btnMenu.id ->{
                    musicPlayerClickListener?.onMenuClick()
                }
                else -> {
                    musicPlayerClickListener?.onMusicPlayerClick()
                }
            }
        }
    }

    constructor(context: Context) : super(context){
        this.context = context
        setUp(context)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        this.context = context;
        setUp(context)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        this.context = context
        setUp(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes){
        this.context = context
        setUp(context)
    }

    private fun setUp(context: Context){
        binding = MusicPlayerSmallLayoutBinding.inflate(LayoutInflater.from(context), this, true)

        binding.btnMenu.setOnClickListener(callback)
        binding.btnNext.setOnClickListener(callback)
        binding.btnPauseStart.setOnClickListener(callback)
        binding.root.setOnClickListener(callback)
    }

    fun setOnMusicPlayerClickListener(musicPlayerSmallClickListener: MusicPlayerSmallClickListener){
        musicPlayerClickListener = musicPlayerSmallClickListener
    }
}