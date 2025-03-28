package com.musicapp.mymusicplayer.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import com.musicapp.mymusicplayer.R

interface ThreeDotMenuListener {
    fun onMenuItemClick(item: MenuItem): Boolean
}

class ThreeDotMenuView : androidx.appcompat.widget.AppCompatImageButton{
    private var menuListener: ThreeDotMenuListener? = null
    private var menuResId: Int = R.menu.menu_song_options
    private var context: Context

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

    private fun setupWidget(){
        /*
        binding = ViewThreeDotMenuLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        binding.imgThreeDot.setOnClickListener{
            showPopupMenu()
        }

         */

        this.setOnClickListener{
            showPopupMenu()
        }
    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this.context, this)
        popupMenu.menuInflater.inflate(menuResId, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            menuListener?.onMenuItemClick(item) ?: false
        }
        popupMenu.show()
    }

    fun setMenuResource(menuResId: Int) {
        this.menuResId = menuResId
    }

    fun setThreeDotMenuListener(listener: ThreeDotMenuListener) {
        this.menuListener = listener
    }
}
