package kr.co.petdoc.petdoc.adapter.chat.v2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sendbird.android.*
import kotlinx.android.synthetic.main.adapter_sb_chat_admin.view.*
import kotlinx.android.synthetic.main.adapter_sb_chat_file.view.*
import kotlinx.android.synthetic.main.adapter_sb_chat_user.view.*
import kotlinx.android.synthetic.main.adapter_sb_chat_vet_doctor.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.utils.DateUtils
import kr.co.petdoc.petdoc.utils.image.ImageUtil
import kr.co.petdoc.petdoc.utils.image.StorageUtils

/**
 * Petdoc
 * Class: SBChatAdapter
 * Created by kimjoonsung on 12/4/20.
 *
 * Description :
 */
class SBChatAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_VET_DOCTOR_MESSAGE = 10
        private const val VIEW_TYPE_FILE_MESSAGE = 20
        private const val VIEW_TYPE_ADMIN_MESSAGE = 30
        private const val VIEW_TYPE_USER_MESSAGE = 40
    }

    private var messageList:MutableList<BaseMessage> = mutableListOf()
    private var mItemClickListener: OnItemClickListener? = null
    private var mItemLongClickListener: OnItemLongClickListener? = null

    private var chatId = ""

    init {
        chatId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            VIEW_TYPE_VET_DOCTOR_MESSAGE -> {
                VetDoctorMessageHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_sb_chat_vet_doctor, parent, false)
                )
            }

            VIEW_TYPE_FILE_MESSAGE -> {
                FileMessageHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.adapter_sb_chat_file, parent, false)
                )
            }

            VIEW_TYPE_USER_MESSAGE -> {
                UserMessageHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.adapter_sb_chat_user, parent, false)
                )
            }

            else -> {
                AdminMessageHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.adapter_sb_chat_admin, parent, false)
                )
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        var isNewDay = false

        // If there is at least one item preceding the current one, check the previous message.
        if (position < messageList.size - 1) {
            val prevMessage = messageList[position + 1]

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (DateUtils.hasSameDate(message.createdAt, prevMessage.createdAt).not()) {
                isNewDay = true
            }
        } else if (position == messageList.size - 1) {
            isNewDay = true
        }

        when (holder.itemViewType) {
            VIEW_TYPE_VET_DOCTOR_MESSAGE -> {
                (holder as VetDoctorMessageHolder).bind(
                    message as UserMessage,
                    isNewDay,
                    position,
                    mItemClickListener,
                    mItemLongClickListener
                )
            }

            VIEW_TYPE_USER_MESSAGE -> {
                (holder as UserMessageHolder).bind(
                        message as UserMessage,
                        isNewDay,
                        position,
                        mItemClickListener,
                        mItemLongClickListener
                )
            }

            VIEW_TYPE_FILE_MESSAGE -> {
                (holder as FileMessageHolder).bind(
                        message as FileMessage,
                        isNewDay,
                        mItemClickListener,
                        mItemLongClickListener
                )
            }

            else -> {
                (holder as AdminMessageHolder).bind(
                        message as AdminMessage,
                        isNewDay,
                        mItemClickListener
                )
            }
        }
    }

    override fun getItemCount() = messageList.size

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        if (message is UserMessage) {
            if (message.sender.userId == SendBird.getCurrentUser().userId) {
                return VIEW_TYPE_USER_MESSAGE
            } else {
                return VIEW_TYPE_VET_DOCTOR_MESSAGE
            }
        } else if(message is AdminMessage) {
            return VIEW_TYPE_ADMIN_MESSAGE
        } else if (message is FileMessage) {
            return VIEW_TYPE_FILE_MESSAGE
        } else {
            return -1
        }
    }

    fun setClickListener(listener: OnItemClickListener) {
        this.mItemClickListener = listener
    }

    fun setLongClickListener(listener: OnItemLongClickListener) {
        this.mItemLongClickListener = listener
    }

    fun setMessageList(messages: MutableList<BaseMessage>) {
        messageList = messages
        notifyDataSetChanged()
    }

    fun addFirst(message: BaseMessage) {
        messageList.add(0, message)
        notifyDataSetChanged()
    }

    fun addLast(message: BaseMessage) {
        messageList.add(message)
        notifyDataSetChanged()
    }

    fun deleteMessage(msgId: Long) {
        for (msg in messageList) {
            if (msg.messageId == msgId) {
                messageList.remove(msg)
                break
            }
        }
    }

    fun updateMessage(message: BaseMessage) {
        for (index in 0 until messageList.size) {
            if (message.messageId == messageList[index].messageId) {
                messageList.remove(message)
                messageList.add(index, message)
                notifyDataSetChanged()
                break
            }
        }
    }

    //==============================================================================================
    inner class VetDoctorMessageHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
                message: UserMessage,
                isNewDay: Boolean,
                position: Int,
                clickListener: OnItemClickListener?,
                longClickListener: OnItemLongClickListener?
        ) {
            val sender = message.sender

            // Show the date if the message was sent on a different date than the previous one.
            view.doctorDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            view.doctorNickname.text = message.sender.nickname
            view.doctorMsg.text = message.message
            view.doctorTime.text = DateUtils.formatTime(message.createdAt)

            view.doctorEdited.apply {
                when {
                    message.updatedAt > 0 -> {
                        visibility = View.VISIBLE
                    }

                    else -> visibility = View.GONE
                }
            }

            Glide.with(view.context)
                .load(message.sender.profileUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(view.doctorProfile)

            view.setOnClickListener { clickListener?.onUserMessageItemClick(message) }
            view.setOnLongClickListener {
                longClickListener?.onUserMessageItemLongClick(message, position)
                true
            }
        }
    }

    inner class AdminMessageHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(message: AdminMessage, isNewDay: Boolean, listener: OnItemClickListener?) {
            view.adminMsg.text = message.message
            view.adminDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            view.setOnClickListener { listener?.onAdminMessageItemClick(message) }
        }
    }

    inner class FileMessageHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
                message: FileMessage,
                isNewDay: Boolean,
                clickListener: OnItemClickListener?,
                longClickListener: OnItemLongClickListener?
        ) {
            val sender = message.sender

            // If current user sent the message, display name in different color
//            if (sender.userId == SendBird.getCurrentUser().userId) {
//                view.nickName.setTextColor(Helper.readColorRes(R.color.orange))
//            } else {
//                view.nickName.setTextColor(Helper.readColorRes(R.color.slateGrey))
//            }

            // Show the date if the message was sent on a different date than the previous one.
            view.date.apply {
                when{
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            if (message.type.toLowerCase().startsWith("image")) {
                val thumbnails = message.thumbnails as MutableList<FileMessage.Thumbnail>
                if (thumbnails.size > 0) {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(view.context, message.url, view.thumbnail, thumbnails[0].url)
                    } else {
                        Glide.with(view.context)
                            .load(thumbnails[0].url)
                            .into(view.thumbnail)
                    }
                } else {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(view.context, message.url, view.thumbnail, null)
                    } else {
                        Glide.with(view.context)
                            .load(message.url)
                            .into(view.thumbnail)
                    }
                }
            } else if(message.type.toLowerCase().startsWith("video")) {
                val thumbnails = message.thumbnails as MutableList<FileMessage.Thumbnail>
                if (thumbnails.size > 0) {
                    ImageUtil.displayImageFromUrlWithPlaceHolder(view.context, thumbnails[0].url, view.thumbnail, R.drawable.ic_file_message)
                } else {
                    view.thumbnail.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_play))
                }
            } else {
                view.thumbnail.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_file_message))
            }

            view.setOnClickListener { clickListener?.onFileMessageItemClick(message) }
            view.setOnLongClickListener {
                longClickListener?.onFileMessageItemLongClick(message)
                true
            }
        }
    }

    inner class UserMessageHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
                message: UserMessage,
                isNewDay: Boolean,
                position: Int,
                clickListener: OnItemClickListener?,
                longClickListener: OnItemLongClickListener?
        ) {

            // Show the date if the message was sent on a different date than the previous one.
            view.userDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            view.userMsg.text = message.message
            view.userTime.text = DateUtils.formatTime(message.createdAt)

            view.userEdited.apply {
                when {
                    message.updatedAt > 0 -> {
                        visibility = View.VISIBLE
                    }

                    else -> visibility = View.GONE
                }
            }

            view.setOnClickListener { clickListener?.onUserMessageItemClick(message) }
            view.setOnLongClickListener {
                longClickListener?.onUserMessageItemLongClick(message, position)
                true
            }
        }
    }

    /**
     * An interface to implement item click callbacks in the activity or fragment that
     * uses this adapter.
     */
    interface OnItemClickListener {
        fun onUserMessageItemClick(message: UserMessage)
        fun onFileMessageItemClick(message: FileMessage)
        fun onAdminMessageItemClick(message: AdminMessage)
    }

    interface OnItemLongClickListener {
        fun onUserMessageItemLongClick(message: UserMessage, position: Int)
        fun onFileMessageItemLongClick(message: FileMessage)
        fun onAdminMessageItemLongClick(message: AdminMessage)
    }
}