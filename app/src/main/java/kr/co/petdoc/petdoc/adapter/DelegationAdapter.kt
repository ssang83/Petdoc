package kr.co.petdoc.petdoc.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class DelegationAdapter<T> constructor(
    protected var delegatesManager: AdapterDelegatesManager<T>,
    open var items: List<T>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        delegatesManager.onCreateViewHolder(parent, viewType)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        delegatesManager.onBindViewHolder(items, position, holder, null)

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>) =
        delegatesManager.onBindViewHolder(items, position, holder, payloads)

    override fun getItemViewType(position: Int): Int =
        delegatesManager.getItemViewType(items, position)

}