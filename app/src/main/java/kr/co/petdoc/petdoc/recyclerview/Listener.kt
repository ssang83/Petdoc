package kr.co.petdoc.petdoc.recyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * Petdoc
 * Class: ItemDragListener
 * Created by kimjoonsung on 2020/10/19.
 *
 * Description :
 */
interface ItemDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}

interface ItemActionListener {
    fun onItemMoved(from: Int, to: Int)
    fun onItemSwiped(position: Int)
}