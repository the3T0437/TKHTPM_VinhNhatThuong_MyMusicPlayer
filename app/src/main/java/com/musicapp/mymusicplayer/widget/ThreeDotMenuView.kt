package com.musicapp.mymusicplayer.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.databinding.ViewThreeDotMenuLayoutBinding

interface ThreeDotMenuListener {
    fun onPlayNext()
    fun onAddToFavorite()
    fun onAddToPlaylist()
}

class ThreeDotMenuView : ConstraintLayout {


    private lateinit var binding: ViewThreeDotMenuLayoutBinding
    private var menuListener: ThreeDotMenuListener? = null
    private var menuResId: Int = R.menu.menu_song_options
    private lateinit var context: Context

    constructor(context: Context) : super(context){
        this.context = context
        setupWidget()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        this.context = context
        setupWidget()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        this.context = context
        setupWidget()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes){
        this.context = context
        setupWidget()
    }

    private fun setupWidget(){
        binding = ViewThreeDotMenuLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setMenuResource(menuResId: Int) {
        this.menuResId = menuResId
    }

    fun setThreeDotMenuListener(listener: ThreeDotMenuListener) {
        this.menuListener = listener
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(view.context, binding.imgThreeDot)
        popupMenu.menuInflater.inflate(menuResId, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_play_next -> {
                    menuListener?.onPlayNext()
                    true
                }
                R.id.action_add_favorite -> {
                    menuListener?.onAddToFavorite()
                    true
                }
                R.id.action_add_to -> {
                    menuListener?.onAddToPlaylist()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
