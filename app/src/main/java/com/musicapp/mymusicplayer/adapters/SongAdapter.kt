package com.musicapp.mymusicplayer.adapters

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.mikhaellopez.circularimageview.CircularImageView
import com.musicapp.mymusicplayer.R
import com.musicapp.mymusicplayer.activities.PlaylistActivity
import com.musicapp.mymusicplayer.databinding.SongLayoutBinding
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener
import com.musicapp.mymusicplayer.widget.ThreeDotMenuView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface SongClickListener{
    fun onArtistClick(artist: String)
    fun onSongClick(song: Song, index: Int)
}

interface ViewHolderWrapper{
    fun getItemPosition(): Int
    fun getBinding(): ViewBinding
}

interface SongLayoutBindingWrapper{
    fun getTvTitle(): TextView
    fun getTvArtirst(): TextView
    fun getImg(): CircularImageView
    fun getThreeDot(): ThreeDotMenuView
    fun getRoot(): View
}

/*
 * override onInitViewHolder, getCallBack, onCreateViewHolder to create new adapter
 * to reuse onBindViewHolder, override getSongLayoutBindingWrapper
 *
 * override getMenuResource, getThreeDotMenuListener to change behavior of three dot widget
 */
open class SongAdapter(protected val context: Context, protected val arr: ArrayList<Song>) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    protected var _songClickListener: SongClickListener? = null

    inner class ViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root), ViewHolderWrapper{
        var loadJob: Job? = null
        var songPosition: Int = -1

        override fun getItemPosition(): Int {
            return songPosition
        }

        override fun getBinding(): ViewBinding {
            return binding
        }

        init {
            onInitViewHolder(this)
        }
    }

    protected open fun onInitViewHolder(viewHolder: ViewHolderWrapper){
        val callBack = getCallback(viewHolder)
        val binding = getSongLayoutBindingWrapper(viewHolder.getBinding())
        binding.getTvTitle().setOnClickListener(callBack)
        binding.getRoot().setOnClickListener(callBack)
    }

    protected open fun getCallback(viewHolder: ViewHolderWrapper): OnClickListener{
        val callback: OnClickListener = object : OnClickListener {
            override fun onClick(v: View?) {
                if (v == null)
                    return

                val binding = viewHolder.getBinding()
                val bindingWrapper = getSongLayoutBindingWrapper(binding)
                val position = viewHolder.getItemPosition()
                when (v.id) {
                    bindingWrapper.getTvArtirst().id -> {
                        _songClickListener?.onArtistClick(arr[position].artist ?: "")
                    }

                    else -> {
                        _songClickListener?.onSongClick(arr[position], position)
                    }
                }
            }
        }

        return callback
    }

    open override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: SongLayoutBinding = SongLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = arr[position]
        Log.d("SongAdapter", "Binding song: ${song.title}, Artist: ${song.artist}")

        val binding = getSongLayoutBindingWrapper(holder.getBinding())
        binding.getTvTitle().text = song.title
        binding.getTvArtirst().text = song.artist

        holder.loadJob?.cancel()
        holder.loadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    song.id
                )
                val bitmap = context.contentResolver.loadThumbnail(uri, Size(640, 480), null)

                withContext(Dispatchers.Main) {
                    binding.getImg().setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.getImg().setImageResource(R.drawable.thumbnail)
                }
            }
        }

        binding.getThreeDot().setMenuResource(getMenuResource())
        binding.getThreeDot().setThreeDotMenuListener(getThreeDotMenuListener(holder, song, position))
        holder.songPosition = position
    }

    protected open fun getSongLayoutBindingWrapper(binding: ViewBinding): SongLayoutBindingWrapper{
        binding as SongLayoutBinding
        return object: SongLayoutBindingWrapper{
            override fun getTvTitle(): TextView {
                return binding.tvTitle
            }

            override fun getTvArtirst(): TextView {
                return binding.tvArtist
            }

            override fun getImg(): CircularImageView {
                return binding.img
            }

            override fun getThreeDot(): ThreeDotMenuView {
                return binding.threeDotMenu
            }

            override fun getRoot(): View{
                return binding.root
            }
        }
    }

    protected open fun getMenuResource(): Int{
        return R.menu.menu_song_options
    }

    protected open fun getThreeDotMenuListener(holder: ViewHolder, song: Song, position: Int): ThreeDotMenuListener{
       return object : ThreeDotMenuListener{
           override fun onMenuItemClick(item: MenuItem): Boolean {
               val binding = holder.getBinding() as SongLayoutBinding
               when(item.itemId){
                   R.id.action_play_next -> {
                       Toast.makeText(context, "Play Next: ${song.title}", Toast.LENGTH_SHORT).show()
                       return true
                   }
                   R.id.action_add_to ->{
                       val intent = Intent(binding.root.context, PlaylistActivity::class.java)
                       binding.root.context.startActivity(intent)
                       Toast.makeText(context, "Add to: ${song.title}", Toast.LENGTH_SHORT).show()
                       return true
                   }
                   R.id.action_add_favorite ->{
                       Toast.makeText(context, "Added to Favorite: ${song.title}", Toast.LENGTH_SHORT).show()
                       return true
                   }
               }

               return false;
           }
       }
    }

    override fun getItemCount(): Int {
        return arr.size
    }

    fun setSongClickListener(songClickListener: SongClickListener){
        this._songClickListener = songClickListener
    }
}