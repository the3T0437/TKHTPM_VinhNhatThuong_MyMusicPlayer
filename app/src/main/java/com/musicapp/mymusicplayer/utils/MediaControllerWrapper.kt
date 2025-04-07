package com.musicapp.mymusicplayer.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.session.MediaController
import com.musicapp.mymusicplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

class MediaControllerWrapper {
    private val mediaController: MediaController?
    val playingSongs: ArrayList<Song> = arrayListOf()
    private val mediaControllerThread = newSingleThreadContext("mediaControllerThread")
    private val coroutineScope = CoroutineScope(mediaControllerThread)

    companion object{
        var instance: MediaControllerWrapper? = null

        fun getInstance(mediaController: MediaController?): MediaControllerWrapper{
            if (instance == null)
                instance = MediaControllerWrapper(mediaController)

            return instance!!
        }
    }

    private constructor(mediaController: MediaController?) {
        this.mediaController = mediaController
    }

    fun prepare(){
        mediaController?.prepare()
    }

    fun play(){
        mediaController?.play()
    }

    fun addSong(song: Song){
        val mediaItem = MediaItem.fromUri(song.getUri())
        mediaController?.addMediaItem(mediaItem)
        playingSongs.add(song)
    }

    fun pause(){
        mediaController?.pause()
    }

    fun currentMediaItem(): MediaItem?{
        return mediaController?.currentMediaItem
    }

    fun isPlaying(): Boolean{
        return mediaController?.isPlaying ?: false
    }

    fun hasNextMediaItem(): Boolean{
        return mediaController?.hasNextMediaItem() ?: false
    }

    fun hasPrevMediaItem(): Boolean{
        return mediaController?.hasPreviousMediaItem() ?: false
    }

    fun seekToNextMediaItem(){
         mediaController?.seekToNextMediaItem()
    }

    fun seekTo(position: Long){
        mediaController?.seekTo(position)
    }

    fun seekToMediaItem(position: Int){
        mediaController?.seekTo(position, 0)
    }

    fun addSongs(songs: ArrayList<Song>){
        addSongs(songs, mediaController?.mediaItemCount?:0)
    }

    fun addSongs(songs: ArrayList<Song>, position: Int){
        coroutineScope.launch {
            var mediaItems = arrayListOf<MediaItem>()
            for(song in songs){
                val mediaItem = MediaItem.fromUri(song.getUri())
                mediaItems.add(mediaItem)
            }

            playingSongs.addAll(songs)
            withContext(Dispatchers.Main){
                mediaController?.addMediaItems(position, mediaItems)
            }
        }
    }

    fun removeSong(position: Int){
        mediaController?.removeMediaItem(position)
        playingSongs.removeAt(position)
    }

    fun moveMediaItem(from: Int, to: Int){
        mediaController?.moveMediaItem(from, to)
        val song = playingSongs.removeAt(from)
        playingSongs.add(to, song)
    }

    fun clear(){
        mediaController?.removeMediaItems(0, getSize())
        playingSongs.clear()
    }

    fun getSize(): Int{
        return mediaController?.mediaItemCount ?: 0
    }

    fun getCurrentSongId(context: Context): Long{
        val mediaItem = mediaController!!.currentMediaItem
        if (mediaItem == null)
            return -1

        val song = songGetter.getSong(context, mediaItem.localConfiguration!!.uri)
        return song?.id ?: -1
    }

    fun addListener(listener: Player.Listener){
        mediaController?.addListener(listener)
    }

    fun removeListener(listener: Player.Listener){
        mediaController?.removeListener(listener)
    }
}