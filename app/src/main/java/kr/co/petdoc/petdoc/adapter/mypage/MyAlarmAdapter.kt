package kr.co.petdoc.petdoc.adapter.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.AdapterMyAlarmItemBinding
import kr.co.petdoc.petdoc.domain.MyAlarm
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.viewmodel.MyAlarmViewModel

/**
 * Petdoc
 * Class: MyAlarmAdapter
 * Created by kimjoonsung on 1/25/21.
 *
 * Description :
 */
class MyAlarmAdapter(
    private val viewModel: MyAlarmViewModel
) : RecyclerView.Adapter<MyAlarmAdapter.MyAlarmViewHolder>() {

    private var mItems:MutableList<MyAlarm> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyAlarmViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_my_alarm_item,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MyAlarmViewHolder, position: Int) {
        holder.bind(mItems[position])
    }

    override fun getItemCount() = mItems.size

    fun setItems(items: List<MyAlarm>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        notifyDataSetChanged()
    }

    inner class MyAlarmViewHolder(view: View) : BindingViewHolder<AdapterMyAlarmItemBinding>(view) {
        fun bind(item: MyAlarm) {
            binding.viewModel = viewModel
            binding.item = item
        }
    }
}