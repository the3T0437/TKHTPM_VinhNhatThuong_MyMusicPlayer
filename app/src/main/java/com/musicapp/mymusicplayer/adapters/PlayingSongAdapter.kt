package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.activities.AddPlayListActivity
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener


interface RemoveSongListener{
    fun onRemoveSong(song: Song, index: Int)
}

class PlayingSongAdapter(context: Context, arr: ArrayList<Song>, mediaController: MediaControllerWrapper) : DragableSongAdapter(context, arr, mediaController) {
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
                    R.id.action_add_to ->{
                        val intent = Intent(context, AddPlayListActivity::class.java)
                        intent.putExtra("song_id", song.id)
                        context.startActivity(intent)
                        Toast.makeText(context, "Add to: ${song.title}", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    R.id.action_add_favorite ->{
                        if (isFavoriteSong(song))
                            Toast.makeText(context, "Removed to Favorite: ${song.title}", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(context, "Added to Favorite: ${song.title}", Toast.LENGTH_SHORT).show()
                        toggleFavoriteSong(song)
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