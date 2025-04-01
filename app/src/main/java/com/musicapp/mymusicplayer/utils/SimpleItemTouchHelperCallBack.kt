package com.musicapp.mymusicplayer.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

interface MoveListener{
    fun onMove(from: Int, to: Int): Boolean
    fun onSwipe(viewHolder: ViewHolder, direction: Int)
}

class SimpleItemTouchHelperCallBack : ItemTouchHelper.Callback(){
    private var moveListener: MoveListener? = null

    fun setMoveListener(moveListener: MoveListener){
        this.moveListener = moveListener
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ): Int {
        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP or ItemTouchHelper.DOWN)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        return moveListener?.onMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition) ?: true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        moveListener?.onSwipe(viewHolder, direction)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
}