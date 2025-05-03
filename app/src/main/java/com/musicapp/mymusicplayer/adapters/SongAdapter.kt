package com.musicapp.mymusicplayer.adapters

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.musicapp.mymusicplayer.activities.AddPlayListActivity
import com.musicapp.mymusicplayer.activities.SongsOfArtistActivity
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.database.OnDatabaseCallBack
import com.musicapp.mymusicplayer.databinding.SongLayoutBinding
import com.musicapp.mymusicplayer.model.Artist
import com.musicapp.mymusicplayer.model.FavoriteSong
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper
import com.musicapp.mymusicplayer.widget.ThreeDotMenuListener
import com.musicapp.mymusicplayer.widget.ThreeDotMenuView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Collections


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

abstract class SongClickListener{
    open fun onArtistClick(artistId: Long){
    }

    open fun onSongClick(song: Song, index: Int){
    }

    open fun isPlayingSongWhenClicked(): Boolean{
        return true
    }
}
/*
 * need to set mediaController to make threeDotWidget work, playable song
 *
 * override onInitViewHolder, getCallBack, onCreateViewHolder to create new adapter
 * to reuse onBindViewHolder, override getSongLayoutBindingWrapper
 *
 * override getMenuResource, getThreeDotMenuListener to change behavior of three dot widget
 *
 * set currentPlayingSongId to hightlight playing song
 */
open class SongAdapter(protected val context: Context, protected val songs: ArrayList<Song>, var mediaController: MediaControllerWrapper? = null) :

    RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    protected var _songClickListener: SongClickListener? = null
    var currentPlayingSongId : Long = -1
        set(value) {
            CoroutineScope(Dispatchers.IO).launch{
                val oldPlayingSongId = field
                val newPlayingSongId = value
                field = value

                hightlightPlayingSong(oldPlayingSongId, newPlayingSongId)
            }
        }
    // b1. viết hàm để xử lý lại danh sach
    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                songs.swap(i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                songs.swap(i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        // update  lại danh sách phát nhạc trong mediaController
        mediaController?.clear()
        mediaController?.addSongs(songs)
    }
    private fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
        val temp = this[index1]
        this[index1] = this[index2]
        this[index2] = temp
    }

    private suspend fun hightlightPlayingSong(oldPlayingSongId: Long, newPlayingSongId: Long) {
        if (oldPlayingSongId == newPlayingSongId)
            return

        try {
            for (i in 0 until songs.size) {
                if (songs[i].id == oldPlayingSongId)
                    withContext(Dispatchers.Main) {
                        this@SongAdapter.notifyItemChanged(i)
                    }
                if (songs[i].id == newPlayingSongId)
                    withContext(Dispatchers.Main) {
                        this@SongAdapter.notifyItemChanged(i)
                    }
            }
        }catch(e: IndexOutOfBoundsException){
            //it is fine if remove while hightlighting
            Log.d("myLog", "remove while highlighting")
        }
    }

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
        binding.getTvArtirst().setOnClickListener(callBack)
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
                when (v) {
                    bindingWrapper.getTvArtirst() -> {
                        _songClickListener?.onArtistClick(songs[position].artistId ?: 0)

                        val intent = Intent(context, SongsOfArtistActivity::class.java)
                        intent.putExtra(Artist.ID, songs[position].artistId)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        context.startActivity(intent)
                    }

                    else -> {
                        _songClickListener?.onSongClick(songs[position], position)

                        if (_songClickListener == null || _songClickListener!!.isPlayingSongWhenClicked())
                            playAllSong(position)
                    }
                }
            }
        }

        return callback
    }

    private fun playAllSong(position: Int = 0, isShuffle : Boolean = false){
        mediaController?.apply {
            clear()
            setShuffleMode(isShuffle)
            addSongs(songs)
            seekToMediaItem(position)
            prepare()
            play()
        }
    }

    open override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: SongLayoutBinding = SongLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val finalSongs = Collections.unmodifiableList(songs)
        val song = finalSongs[position]
        Log.d("SongAdapter", "Binding song: ${song.title}, Artist: ${song.artist}")

        val binding = getSongLayoutBindingWrapper(holder.getBinding())
        binding.getTvTitle().text = song.title
        binding.getTvArtirst().text = song.artist

        if (song.id == currentPlayingSongId)
            binding.getRoot().setBackgroundColor(Color.rgb(48, 210, 210))
        else
            binding.getRoot().setBackgroundColor(Color.TRANSPARENT)


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
                       mediaController?.addSong(song)
                       return true
                   }
                   R.id.action_add_to ->{
                       val intent = Intent(binding.root.context, AddPlayListActivity::class.java)
                       intent.putExtra("song_id", song.id)
                       binding.root.context.startActivity(intent)
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

               return false;
           }
       }
    }

    private fun toggleFavoriteSong(song: Song){
        val databaseApi = DatabaseAPI(context)
        runBlocking {
            val favoriteId = databaseApi.getFavorite(song.id)?.id ?: -1

            if (favoriteId == -1){
                databaseApi.insertFavoriteSong(FavoriteSong(song.id), object:
                    OnDatabaseCallBack {
                    override fun onSuccess(id: Long) {
                    }

                    override fun onFailure(e: Exception) {
                    }
                })
            }
            else{
                databaseApi.deleteFavroiteSong(favoriteId, object: OnDatabaseCallBack {
                    override fun onSuccess(id: Long) {
                    }

                    override fun onFailure(e: Exception) {
                    }
                })
            }
            return@runBlocking favoriteId
        }
    }

    private fun isFavoriteSong(song: Song): Boolean = runBlocking {
        val databaseApi = DatabaseAPI(context)
        return@runBlocking databaseApi.getFavorite(song.id) != null
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun setSongClickListener(songClickListener: SongClickListener){
        this._songClickListener = songClickListener
    }
}