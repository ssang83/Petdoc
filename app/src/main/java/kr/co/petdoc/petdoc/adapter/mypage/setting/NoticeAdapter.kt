package kr.co.petdoc.petdoc.adapter.mypage.setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_notice_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.NoticeData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: NoticeAdapter
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 공지사항 어댑터
 */
class NoticeAdapter : RecyclerView.Adapter<NoticeAdapter.NoticeHolder>() {

    private var mItems:MutableList<NoticeData> = mutableListOf()
    private var mListener:AdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NoticeHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_notice_item, parent, false))

    override fun onBindViewHolder(holder: NoticeHolder, position: Int) {
        mItems[position].let { item ->
            holder.itemView.noticeTitle.text = item.title

            val createdAt = item.createdAt.split("T")
            holder.itemView.regDate.text = calculateDate(createdAt[0])

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    fun updateData(items: List<NoticeData>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun addItems(items: List<NoticeData>) {
        if(items.isNotEmpty()) {
            for(i in 0 until items.size) {
                this.mItems.add(items[i])
            }

            notifyItemInserted(mItems.size - items.size)
        }
    }

    private fun calculateDate(date: String) : String {
        val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val format = SimpleDateFormat("MM월dd일", Locale.KOREA)
        val date = format1.parse(date.replace("-", ""))

        return format.format(date)
    }

    inner class NoticeHolder(view:View) : RecyclerView.ViewHolder(view)

    interface AdapterListener {
        fun onItemClicked(item:NoticeData)
    }
}