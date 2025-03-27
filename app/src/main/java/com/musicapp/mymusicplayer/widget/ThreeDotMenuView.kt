package com.musicapp.mymusicplayer.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import com.musicapp.mymusicplayer.R

interface ThreeDotMenuListener {
    fun onPlayNext()
    fun onAddToFavorite()
    fun onAddToPlaylist()
}

class ThreeDotMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var menuListener: ThreeDotMenuListener? = null
    private var menuResId: Int = R.menu.menu_song_options
    private val imgThreeDot: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.view_three_dot_menu_layout, this, true)
        imgThreeDot = findViewById(R.id.imgThreeDot)


        imgThreeDot.setOnClickListener { showPopupMenu(it) }
    }

    fun setMenuResource(menuResId: Int) {
        this.menuResId = menuResId
    }

    fun setThreeDotMenuListener(listener: ThreeDotMenuListener) {
        this.menuListener = listener
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(view.context, imgThreeDot)
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
