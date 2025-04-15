package com.musicapp.mymusicplayer.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.musicapp.mymusicplayer.adapters.SongAdapter

class DragDropCallback(
    private val adapter: SongAdapter
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0) // chỉ hỗ trợ kéo lên xuống
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        adapter.moveItem(fromPosition, toPosition) // Cập nhật adapter
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // không thực hiện action vuốt
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true // cho phép  nhấn giữ để kéo
    }
}