package kr.co.petdoc.petdoc.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_chat_pet_doctor_item.view.*
import kotlinx.android.synthetic.main.adapter_chat_user_item.view.*
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.Message
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ChatAdapter
 * Created by kimjoonsung on 2020/05/26.
 *
 * Description :
 */
class MyChatAdapter(context:Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_DOCTOR = 1
    }

    private var messageItems:MutableList<Message> = mutableListOf()
    private var mContext = context

    private var margin20 = 0

    init {
        margin20 = Helper.convertDpToPx(mContext, 20f).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            VIEW_TYPE_DOCTOR -> DoctorHolder(
                LayoutInflater.from(mContext)
                    .inflate(R.layout.adapter_chat_pet_doctor_item, parent, false)
            )

            else -> UserHolder(
                LayoutInflater.from(mContext)
                    .inflate(R.layout.adapter_chat_user_item, parent, false))
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_DOCTOR -> onDoctorMessageBind(holder as DoctorHolder, position)
            else -> onUserMessageBind(holder as UserHolder, position)
        }
    }

    override fun getItemCount() = messageItems.size

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        val oldUserId = StorageUtils.loadValueFromPreference(mContext, AppConstants.PREF_KEY_OLD_USER_ID, "")

        if (item?.user != null) {
            if (oldUserId == item?.user.id.toString()) {
                return VIEW_TYPE_USER
            } else {
                return VIEW_TYPE_DOCTOR
            }
        } else {
            return VIEW_TYPE_USER
        }
    }

    fun setItems(items: List<Message>) {
        this.messageItems.clear()
        this.messageItems.addAll(items)

        notifyDataSetChanged()
    }

    fun addItems(newItem: Message) {
        this.messageItems.add(messageItems.size, newItem)
        notifyItemInserted(messageItems.size - 1)
    }

    fun getItem(position: Int): Message? = if (position in 0 until messageItems.size) messageItems[position] else null

    private fun onDoctorMessageBind(holder: DoctorHolder, position: Int) {
        val item = getItem(position)

        var prevDate = ""
        var prevUsername = ""
        if (position != 0) {
            val prevItem = getItem(position - 1)
            val prevDateArray = prevItem!!.createdAt.split("T")
            val prevDateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val prevSdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            prevDate = prevSdf.format(prevDateSdf.parse(prevDateArray[0]))
            prevUsername = prevItem!!.user?.name!!
        }

        if (item != null) {
            val dateArray = item.createdAt.split("T")  // 2018-12-18
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val sdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            val currentDate = sdf.format(date.parse(dateArray[0]))

            if (prevDate == currentDate) {
                holder.itemView.doctorDate.visibility = View.GONE
            } else {
                holder.itemView.doctorDate.visibility = View.VISIBLE
                holder.itemView.doctorDate.text = currentDate
            }

            val timeArray = dateArray[1].split(".")
            holder.itemView.doctorTime.text = timeArray[0].substring(0, timeArray[0].length - 3)
            holder.itemView.doctorMessage.text = item.text

            if (item.user?.name!! == prevUsername) {
                holder.itemView.doctorName.visibility = View.GONE
                holder.itemView.doctorImg.visibility = View.INVISIBLE
            } else {
                holder.itemView.doctorName.visibility = View.VISIBLE
                holder.itemView.doctorImg.visibility = View.VISIBLE

                holder.itemView.doctorName.text = item.user.name

                val url = item.user.avatar
                if (url.isNotEmpty()) {
                    Glide.with(mContext)
                        .load( if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.itemView.doctorImg)
                } else {
                    holder.itemView.doctorImg.setBackgroundResource(R.drawable.profile_default)
                }
            }
        }

        if (position == itemCount - 1) {
            (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                bottomMargin = margin20
            }
        }
    }

    private fun onUserMessageBind(holder: UserHolder, position: Int) {
        val item = getItem(position)

        var prevDate = ""
        if (position != 0) {
            val prevItem = getItem(position - 1)
            val prevDateArray = prevItem!!.createdAt.split("T")
            val prevDateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val prevSdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            prevDate = prevSdf.format(prevDateSdf.parse(prevDateArray[0]))
        }

        if (item != null) {
            if (item.user != null) {
                val dateArray = item.createdAt.split("T")  // 2018-12-18
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                val sdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
                val currentDate = sdf.format(date.parse(dateArray[0]))

                if (prevDate == currentDate) {
                    holder.itemView.userDate.visibility = View.GONE
                } else {
                    holder.itemView.userDate.visibility = View.VISIBLE
                    holder.itemView.userDate.text = currentDate
                }

                val timeArray = dateArray[1].split(".")
                holder.itemView.userTime.text = timeArray[0].substring(0, timeArray[0].length - 3)
            } else { // 보호자가 직접 작성한 경우
                val dateArray = item.createdAt.split(" ")  // 2018-12-18
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                val sdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
                val currentDate = sdf.format(date.parse(dateArray[0]))

                if (prevDate == currentDate) {
                    holder.itemView.userDate.visibility = View.GONE
                } else {
                    holder.itemView.userDate.visibility = View.VISIBLE
                    holder.itemView.userDate.text = currentDate
                }

                holder.itemView.userTime.text = dateArray[1]
            }

            holder.itemView.userMessage.apply {
                when {
                    item.text.isNotEmpty() -> {
                        visibility = View.VISIBLE
                        text = item.text
                    }

                    else -> {
                        visibility = View.GONE
                    }
                }
            }

            holder.itemView.userImage.apply {
                when {
                    item.image.isNotEmpty() -> {
                        visibility = View.VISIBLE

                        Glide.with(mContext)
//                            .load( if(item.image.startsWith("http")) item.image else "${BuildConfig.IMAGE_URL}${item.image}")
                            .load(item.image)
                            .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                            .into(holder.itemView.userImage)
                    }

                    else -> {
                        visibility = View.GONE
                    }
                }
            }
        }

        if (position == itemCount - 1) {
            (holder.itemView.layoutRoot.layoutParams as RecyclerView.LayoutParams).apply {
                bottomMargin = margin20
            }
        }
    }

    inner class UserHolder(item: View) : RecyclerView.ViewHolder(item)
    inner class DoctorHolder(item: View) : RecyclerView.ViewHolder(item)
}