package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.mikhaellopez.circularimageview.CircularImageView
import com.musicapp.mymusicplayer.databinding.DragableSongLayoutBinding
import com.musicapp.mymusicplayer.databinding.SongLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.widget.ThreeDotMenuView

open class DragableSongAdapter(context: Context, arr: ArrayList<Song>) : SongAdapter(context, arr){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: DragableSongLayoutBinding = DragableSongLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getCallback(viewHolder: ViewHolderWrapper): OnClickListener {
        return super.getCallback(viewHolder)
    }


    override fun getSongLayoutBindingWrapper(binding: ViewBinding): SongLayoutBindingWrapper {
        val dragableBinding = binding as DragableSongLayoutBinding
        return object: SongLayoutBindingWrapper{
            override fun getTvTitle(): TextView {
                return dragableBinding.tvTitle
            }

            override fun getTvArtirst(): TextView {
                return dragableBinding.tvArtist
            }

            override fun getImg(): CircularImageView {
                return dragableBinding.img
            }

            override fun getThreeDot(): ThreeDotMenuView {
                return dragableBinding.threeDotMenu
            }

            override fun getRoot(): View {
                return dragableBinding.root
            }
        }
    }
}