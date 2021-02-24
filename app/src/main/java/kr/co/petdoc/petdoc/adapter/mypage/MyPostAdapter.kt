package kr.co.petdoc.petdoc.adapter.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_my_post_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.response.submodel.MyPetTalkData
import kr.co.petdoc.petdoc.utils.Helper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: MyPostAdapter
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 내 게시글 하면 어댑터
 */
class MyPostAdapter(private val clickListener:(MyPetTalkData) -> Unit)
    : RecyclerView.Adapter<MyPostAdapter.MyPostHolder>() {

    private var mItems:MutableList<MyPetTalkData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyPostHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_my_post_item, parent, false))

    override fun onBindViewHolder(holder: MyPostHolder, position: Int) {
        mItems[position].let { item ->
            if (item.titleImage != null && item.titleImage!!.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(if(item.titleImage!!.startsWith("http")) item.titleImage else "${AppConstants.IMAGE_URL}${item.titleImage}")
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.itemView.profileImage)
            } else {
                holder.itemView.profileImage.setBackgroundResource(R.drawable.profile_default)
            }

            holder.itemView.postTitle.text = item.title
            holder.itemView.petName.text = item.nickName

            val createdAt = item.createdAt.split("T")
            holder.itemView.regDate.text = calculateDate(createdAt[0])
            holder.itemView.replyCount.text = String.format("%s %d", Helper.readStringRes(R.string.reply), item.commentCount)

            holder.itemView.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: List<MyPetTalkData>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun addItems(items: List<MyPetTalkData>) {
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

    inner class MyPostHolder(view: View) : RecyclerView.ViewHolder(view)
}