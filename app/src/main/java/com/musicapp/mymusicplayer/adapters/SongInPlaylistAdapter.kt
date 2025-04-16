package com.musicapp.mymusicplayer.adapters

import android.content.Context
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.utils.MediaControllerWrapper

class SongInPlaylistAdapter(context: Context, arr: ArrayList<Song>, mediaController: MediaControllerWrapper) : DragableSongAdapter(context, arr, mediaController){

}