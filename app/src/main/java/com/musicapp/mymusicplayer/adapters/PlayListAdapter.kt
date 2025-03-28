package com.musicapp.mymusicplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.database.DatabaseAPI
import com.musicapp.mymusicplayer.databinding.PlaylistMemberLayoutBinding
import com.musicapp.mymusicplayer.model.PlayList

class PlayListAdapter(private val context: Context, private val arr: List<PlayList>):
RecyclerView.Adapter<PlayListAdapter.ViewHolder>(){
    private var databaseAPI: DatabaseAPI = DatabaseAPI(context)
    private var playLists = ArrayList<PlayList>()
    inner class ViewHolder(val binding: PlaylistMemberLayoutBinding) :RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: PlaylistMemberLayoutBinding = PlaylistMemberLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arr.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = arr[position].name

    }
}