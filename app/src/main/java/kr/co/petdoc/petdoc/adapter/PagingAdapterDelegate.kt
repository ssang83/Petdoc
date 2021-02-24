package kr.co.petdoc.petdoc.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.model.PagingItem

interface PagingAdapterDelegate<in T: PagingItem> {

    fun isForViewType(item: T): Boolean

    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    fun onBindViewHolder(
        item: T,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<*>
    )
}