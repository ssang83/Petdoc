package kr.co.petdoc.petdoc.adapter.chat.v2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_chat_category.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.chat.ChatRegisterFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.ChatCategoryItem
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: SBCategoryAdapter
 * Created by kimjoonsung on 12/11/20.
 *
 * Description :
 */
class SBCategoryAdapter : RecyclerView.Adapter<SBCategoryAdapter.CategoryHolder>() {

    private var chatCategoryItems:MutableList<ChatCategoryItem> = mutableListOf()
    private var mListener:Listener? = null

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CategoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_chat_category, parent, false))

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        if (selectedPosition == position) {
            holder.itemView.category.setTextColor(Helper.readColorRes(R.color.orange))
            holder.itemView.category.setBackgroundResource(R.drawable.orange_round_rect_6)
        } else {
            holder.itemView.category.setTextColor(Helper.readColorRes(R.color.dark_grey))
            holder.itemView.category.setBackgroundResource(R.drawable.lightbluegrey_round_rect_6)
        }

        holder.setCategory(chatCategoryItems[position].name)

        holder.itemView.setOnClickListener {
            Logger.d("position : $position")
            mListener?.onCategoryClicked(chatCategoryItems[position])
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = chatCategoryItems.size

    fun addAll(items: MutableList<ChatCategoryItem>) {
        this.chatCategoryItems.clear()
        this.chatCategoryItems.addAll(items)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    inner class CategoryHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setCategory(_category: String) {
            item.category.text = _category
        }
    }

    interface Listener {
        fun onCategoryClicked(item:ChatCategoryItem)
    }
}