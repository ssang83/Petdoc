package kr.co.petdoc.petdoc.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.domain.GodoMallProduct
import kr.co.petdoc.petdoc.model.PagingItem

abstract class PagingDelegationAdapter<T: PagingItem> constructor(
    protected var delegatesManager: PagingAdapterDelegatesManager<T>
) : PagingDataAdapter<T, RecyclerView.ViewHolder>(PagingItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        delegatesManager.onCreateViewHolder(parent, viewType)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            delegatesManager.onBindViewHolder(it, position, holder, null)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>) {
        getItem(position)?.let {
            delegatesManager.onBindViewHolder(it, position, holder, payloads)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.let {
            delegatesManager.getItemViewType(it)
        } ?: -1
    }


}

private class PagingItemDiffCallback<T : PagingItem> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}