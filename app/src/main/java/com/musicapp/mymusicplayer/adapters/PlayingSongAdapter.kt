package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.view.MenuItem
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener


interface RemoveSongListener{
    fun onRemoveSong(song: Song, index: Int)
}

class PlayingSongAdapter(context: Context, arr: ArrayList<Song>) : DragableSongAdapter(context, arr) {
    private var removeSongListener : RemoveSongListener? = null

    override fun getMenuResource(): Int {
        return R.menu.playing_song_menu
    }

    override fun getThreeDotMenuListener(holder: ViewHolder, song: Song, position: Int): ThreeDotMenuListener {
        return object : ThreeDotMenuListener{
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when(item.itemId){
                    R.id.menuRemove ->{
                        removeSongListener?.onRemoveSong(song, holder.bindingAdapterPosition)
                        return true
                    }
                }

                return false
            }
        }
    }

    fun setRemoveSongListener(removeSongListener: RemoveSongListener?){
        this.removeSongListener = removeSongListener
    }
}