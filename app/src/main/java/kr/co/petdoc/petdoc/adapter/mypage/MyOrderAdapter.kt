package kr.co.petdoc.petdoc.adapter.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.AdapterMyOrderItemBinding
import kr.co.petdoc.petdoc.domain.Order
import kr.co.petdoc.petdoc.viewmodel.MyPurchaseHistoryModel

/**
 * Petdoc
 * Class: MyOrderAdapter
 * Created by kimjoonsung on 2/5/21.
 *
 * Description :
 */
class MyOrderAdapter(
    private val viewModel: MyPurchaseHistoryModel
):RecyclerView.Adapter<MyOrderAdapter.MyOrderViewHolder>() {
    private val mItems:MutableList<Order> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyOrderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_my_order_item,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MyOrderViewHolder, position: Int) {
        holder.bind(mItems[position], position)
    }

    override fun getItemCount() = mItems.size

    fun setItems(items: List<Order>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    inner class MyOrderViewHolder(view: View) : BindingViewHolder<AdapterMyOrderItemBinding>(view) {
        fun bind(item: Order, position: Int) {
            binding.viewModel = viewModel
            binding.item = item
            binding.position = position
            binding.itemCount = mItems.size
        }
    }
}