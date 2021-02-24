package kr.co.petdoc.petdoc.adapter.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_inquiry_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.QuestionData
import kr.co.petdoc.petdoc.utils.Helper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: InquiryAdapter
 * Created by kimjoonsung on 2020/04/16.
 *
 * Description :
 */
class InquiryAdapter : RecyclerView.Adapter<InquiryAdapter.InquiryHolder>() {

    private var mItems:MutableList<QuestionData> = mutableListOf()
    private var mListener: AdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InquiryHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_inquiry_item, parent, false))

    override fun onBindViewHolder(holder: InquiryHolder, position: Int) {
        mItems[position].let { item ->
            holder.itemView.title.text = item.title
            holder.itemView.desc.text = item.content

            val createdAt = item.createdAt!!.split("T")
            holder.itemView.date.text = calculateDate(createdAt[0])

            holder.itemView.layoutReplyStatus.apply {
                when {
                    item.isConfirm == false -> {
                        setBackgroundResource(R.drawable.grey_round_rect_11)
                        holder.itemView.replyStatus.text = Helper.readStringRes(R.string.mypage_inquiry_reply_waiting)
                        holder.itemView.replyStatus.setTextColor(Helper.readColorRes(R.color.light_grey3))
                    }

                    else -> {
                        setBackgroundResource(R.drawable.orange_round_11)
                        holder.itemView.replyStatus.text = Helper.readStringRes(R.string.mypage_inquiry_reply_complete)
                        holder.itemView.replyStatus.setTextColor(Helper.readColorRes(R.color.orange))
                    }
                }
            }

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: List<QuestionData>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    fun addItems(items: List<QuestionData>) {
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

    inner class InquiryHolder(view : View) : RecyclerView.ViewHolder(view)

    interface AdapterListener {
        fun onItemClicked(item:QuestionData)
    }
}