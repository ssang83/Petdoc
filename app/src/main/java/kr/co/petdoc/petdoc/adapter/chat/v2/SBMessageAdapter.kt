package kr.co.petdoc.petdoc.adapter.chat.v2

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.sendbird.android.*
import kotlinx.android.synthetic.main.adapter_sb_msg_admin.view.*
import kotlinx.android.synthetic.main.adapter_sb_msg_image_file_agent.view.*
import kotlinx.android.synthetic.main.adapter_sb_msg_image_file_me.view.*
import kotlinx.android.synthetic.main.adapter_sb_msg_unsupport.view.*
import kotlinx.android.synthetic.main.adapter_sb_msg_user_agent.view.*
import kotlinx.android.synthetic.main.adapter_sb_msg_user_me.view.*
import kotlinx.android.synthetic.main.adapter_sb_msg_video_file_agent.view.*
import kotlinx.android.synthetic.main.adapter_sb_msg_video_file_me.view.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskUserRichMessage
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.ChatCategoryItem
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.DateUtils
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.ImageUtil
import java.util.*

/**
 * Petdoc
 * Class: SBMessageAdapter
 * Created by kimjoonsung on 12/10/20.
 *
 * Description :
 */
class SBMessageAdapter(context: Context, items:List<PetInfoItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_UNSUPPORTED = 0
        private const val TYPE_USER_MESSAGE_ME = 1
        private const val TYPE_USER_MESSAGE_AGENT = 2
        private const val TYPE_FILE_IMAGE_MESSAGE_ME = 3
        private const val TYPE_FILE_IMAGE_MESSAGE_AGENT = 4
        private const val TYPE_FILE_VIDEO_MESSAGE_ME = 5
        private const val TYPE_FILE_VIDEO_MESSAGE_AGENT = 6
        private const val TYPE_ADMIN_MESSAGE = 7

        private const val FILE_TYPE_ALL = 0
        private const val FILE_TYPE_IMAGE = 1
        private const val FILE_TYPE_VIDEO = 2
    }

    private var mContext = context
    private var messageList: MutableList<BaseMessage> = mutableListOf()
    private var petInfoItems = items
    private val failedMessageIdList = mutableListOf<String>()
    private val tempFileMessageUriTable = Hashtable<String, Uri>()

    private var urlPreviewTempMessage: BaseMessage? = null
    private var mListener:OnItemClickListener? = null

    private var margin20 = 0
    private var margin4 = 0

    init {
        margin20 = Helper.convertDpToPx(context, 20f).toInt()
        margin4 = Helper.convertDpToPx(context, 4f).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_USER_MESSAGE_ME -> {
                    UserMessageMeHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_sb_msg_user_me, parent, false)
                    )
                }

                TYPE_USER_MESSAGE_AGENT -> {
                    UserMessageAgentHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_sb_msg_user_agent, parent, false)
                    )
                }

                TYPE_FILE_IMAGE_MESSAGE_ME -> {
                    FileImageMessageMeHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_sb_msg_image_file_me, parent, false)
                    )
                }

                TYPE_FILE_IMAGE_MESSAGE_AGENT -> {
                    FileImageMessageAgentHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_sb_msg_image_file_agent, parent, false)
                    )
                }

                TYPE_FILE_VIDEO_MESSAGE_ME -> {
                    FileVideoMessageMeHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_sb_msg_video_file_me, parent, false)
                    )
                }

                TYPE_FILE_VIDEO_MESSAGE_AGENT -> {
                    FileVideoMessageAgentHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_sb_msg_video_file_agent, parent, false)
                    )
                }

                TYPE_ADMIN_MESSAGE -> {
                    AdminMessageHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_sb_msg_admin, parent, false)
                    )
                }

                else -> {
                    UnSupportHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_sb_msg_unsupport, parent, false)
                    )
                }
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messageList.size > 0) {
            var isContinuousFromPrevious = false
            var isContinuousToNext = false
            var isNewDay = false
            var isTempMessage = false
            var isFailedMessage = false
            var tempFileMessageUri: Uri? = null

            val message = messageList[position]
            val prevMessage = if(position > 0) messageList[position - 1] else null
            val nextMessage = if(position < messageList.size - 1) messageList[position + 1] else null

            if (prevMessage == null) {
                isNewDay = true
                isContinuousFromPrevious = false
            } else {
                if (DateUtils.hasSameDate(message.createdAt, prevMessage.createdAt).not()) {
                    isNewDay = true
                    isContinuousFromPrevious = false
                } else {
                    isContinuousFromPrevious = isContinuousFromPrevious(message, prevMessage)
                }
            }

            isContinuousToNext = (nextMessage != null // If nextMessage is null, the current message is the last one.
                    && DateUtils.hasSameDate(message.createdAt, nextMessage.createdAt)
                    && isContinuousToNext(message, nextMessage))

            isTempMessage = isTempMessage(message)
            tempFileMessageUri = getTempFileMessageUri(message)
            isFailedMessage = isFailedMessage(message)

            when (holder.itemViewType) {
                TYPE_USER_MESSAGE_ME -> {
                    (holder as UserMessageMeHolder).bind(
                            message as UserMessage,
                            isNewDay,
                            isContinuousToNext,
                            isContinuousFromPrevious,
                            isFailedMessage,
                            mListener
                    )
                }

                TYPE_USER_MESSAGE_AGENT -> {
                    (holder as UserMessageAgentHolder).bind(
                            message as UserMessage,
                            isNewDay,
                            isContinuousToNext,
                            isContinuousFromPrevious,
                            isFailedMessage,
                            mListener
                    )
                }

                TYPE_FILE_IMAGE_MESSAGE_ME -> {
                    (holder as FileImageMessageMeHolder).bind(
                            message as FileMessage,
                            isNewDay,
                            isContinuousToNext,
                            isContinuousFromPrevious,
                            isFailedMessage,
                            isTempMessage,
                            tempFileMessageUri,
                            mListener
                    )
                }

                TYPE_FILE_IMAGE_MESSAGE_AGENT -> {
                    (holder as FileImageMessageAgentHolder).bind(
                            message as FileMessage,
                            isNewDay,
                            isContinuousToNext,
                            isContinuousFromPrevious,
                            isTempMessage,
                            tempFileMessageUri,
                            mListener
                    )
                }

                TYPE_FILE_VIDEO_MESSAGE_ME -> {
                    (holder as FileVideoMessageMeHolder).bind(
                            message as FileMessage,
                            isNewDay,
                            isContinuousToNext,
                            isContinuousFromPrevious,
                            isFailedMessage,
                            isTempMessage,
                            tempFileMessageUri,
                            mListener
                    )
                }

                TYPE_FILE_VIDEO_MESSAGE_AGENT -> {
                    (holder as FileVideoMessageAgentHolder).bind(
                            message as FileMessage,
                            isNewDay,
                            isContinuousToNext,
                            isContinuousFromPrevious,
                            isFailedMessage,
                            isTempMessage,
                            tempFileMessageUri,
                            mListener
                    )
                }

                TYPE_ADMIN_MESSAGE -> {
                    (holder as AdminMessageHolder).bind(
                            message as AdminMessage,
                            isNewDay
                    )
                }
            }
        } else {
            (holder as UnSupportHolder).bind()
        }
    }

    override fun getItemCount() =
            if(messageList.size == 0 ) {
                1
            } else {
                messageList.size
            }

    override fun getItemViewType(position: Int): Int {
        if (messageList.size > 0) {
            val message = messageList[position]
            if (message is UserMessage) {
                if (message.sender.userId == SendBird.getCurrentUser().userId) {
                    return TYPE_USER_MESSAGE_ME
                } else {
                    return TYPE_USER_MESSAGE_AGENT
                }
            } else if (message is FileMessage) {
                val me = message.sender.userId == SendBird.getCurrentUser().userId
                val fileType = getFileType(message)
                when (fileType) {
                    FILE_TYPE_IMAGE -> {
                        if (me) {
                            return TYPE_FILE_IMAGE_MESSAGE_ME
                        } else {
                            return TYPE_FILE_IMAGE_MESSAGE_AGENT
                        }
                    }

                    FILE_TYPE_VIDEO -> {
                        if (me) {
                            return TYPE_FILE_VIDEO_MESSAGE_ME
                        } else {
                            return TYPE_FILE_VIDEO_MESSAGE_AGENT
                        }
                    }
                }
            } else if (message is AdminMessage) {
                return TYPE_ADMIN_MESSAGE
            }
        }

        return TYPE_UNSUPPORTED
    }

    fun clear() {
        messageList.clear()
    }

    fun setListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    fun setUrlPrevTempMessage(message: BaseMessage?) {
        this.urlPreviewTempMessage = message
    }

    fun isTempMessage(message: BaseMessage): Boolean {
        return message.messageId == 0L
    }

    fun isFailedMessage(message: BaseMessage): Boolean {
        if (!isTempMessage(message)) {
            return false
        }
        if (message is UserMessage) {
            val index: Int = failedMessageIdList.indexOf(message.requestId)
            if (index >= 0) {
                return true
            }
        } else if (message is FileMessage) {
            val index: Int = failedMessageIdList.indexOf(message.requestId)
            if (index >= 0) {
                return true
            }
        }
        return false
    }

    fun markMessageFailed(requestId: String) {
        failedMessageIdList.add(requestId)
        notifyDataSetChanged()
    }

    fun markMessageSent(message: BaseMessage) {
        var msg: Any
        for (i in messageList.indices.reversed()) {
            msg = messageList[i]
            if (message is UserMessage && msg is UserMessage) {
                if (msg.requestId == message.requestId) {
                    messageList.set(i, message)
                    notifyDataSetChanged()
                    return
                }
            } else if (message is FileMessage && msg is FileMessage) {
                if (msg.requestId == message.requestId) {
                    tempFileMessageUriTable.remove(message.requestId)
                    messageList.set(i, message)
                    notifyDataSetChanged()
                    return
                }
            }
        }
    }

    fun insertMessage(message: BaseMessage) {
        if (isTempMessage(message) || isFailedMessage(message)) {
            messageList.add(0, message)
        } else {
            for (item in messageList) {
                if (message.messageId == item.messageId) {
                    return
                }
            }

            messageList.add(0, message)
        }
    }

    fun appendMessage(message: BaseMessage) {
        for (item in messageList) {
            if (message.messageId == item.messageId) {
                return
            }
        }

        messageList.add(message)
    }

    fun addMessage(message: BaseMessage) {
        messageList.add(message)
    }

    fun replaceMessage(message: BaseMessage) {
        var index = -1
        for (oriMessage in messageList) {
            if (oriMessage.messageId == message.messageId) {
                index = messageList.indexOf(oriMessage)
                messageList.remove(oriMessage)
                break
            }
        }
        if (index != -1) {
            messageList.add(index, message)
        }
    }

    fun getMessageList() = this.messageList

    fun addTempFileMessageInfo(message: FileMessage, uri: Uri) {
        tempFileMessageUriTable.put(message.requestId, uri)
    }

    fun getTempFileMessageUri(message: BaseMessage): Uri? {
        if (!isTempMessage(message)) {
            return null
        }
        return if (message !is FileMessage) {
            null
        } else tempFileMessageUriTable.get(message.requestId)
    }

    fun removeFailedMessage(message: BaseMessage) {
        if (message is UserMessage) {
            failedMessageIdList.remove(message.requestId)
            messageList.remove(message)
        } else if (message is FileMessage) {
            failedMessageIdList.remove(message.requestId)
            tempFileMessageUriTable.remove(message.requestId)
            messageList.remove(message)
        }

        notifyDataSetChanged()
    }

    fun getFileType(fileMessage: FileMessage):Int {
        var fileType = FILE_TYPE_ALL
        val type = fileMessage.type
        if (type != null) {
            if (type.startsWith("image")) {
                fileType = FILE_TYPE_IMAGE
            } else if (type.startsWith("video")) {
                fileType = FILE_TYPE_VIDEO
            }
        }

        return fileType
    }

    //==============================================================================================
    inner class UserMessageMeHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            message: UserMessage,
            isNewDay: Boolean,
            isContinuousToNext: Boolean,
            isContinuousFromPrevious: Boolean,
            isFailedMessage: Boolean,
            clickListener: OnItemClickListener?
        ) {
            view.userDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            if (DeskUserRichMessage.isUrlPreviewType(message)) {
                try {
                    val info = DeskUserRichMessage.getUrlPreviewInfo(message)
                    if (info != null) {
                        if (info.mTitle.isNotEmpty() && info.mImageUrl.isNotEmpty()) {
                            view.previewLayer.visibility = View.VISIBLE
                            ImageUtil.displayImageFromUrl(
                                mContext,
                                info.mImageUrl,
                                view.imagePreview,
                                null
                            )
                            view.previewTitle.text = info.mTitle
                            view.previewDomainName.text = info.getDomainName()
                        } else {
                            view.previewLayer.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                    view.previewLayer.visibility = View.GONE
                }
            } else {
                view.previewLayer.visibility = View.GONE
            }

            if (isContinuousToNext.not()) {
                view.userTime.visibility = View.VISIBLE
                view.dividerMe.visibility = View.VISIBLE
                view.userTime.text = DateUtils.formatTime(message.createdAt)
            } else {
                view.userTime.visibility = View.GONE
                view.dividerMe.visibility = View.GONE
            }

            view.dividerMe.apply {
                when {
                    isContinuousFromPrevious.not() -> {
                        visibility = View.VISIBLE
                    }
                    else -> visibility = View.GONE
                }
            }

            if (isFailedMessage) {
                view.userMsg.setOnClickListener {
                    clickListener?.onUserMessageItemClick(message)
                }
            } else {
                view.previewLayer.setOnClickListener {
                    clickListener?.onUserMessageUrlPreviewClicked(message)
                }
            }
        }
    }

    inner class UserMessageAgentHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            message: UserMessage,
            isNewDay: Boolean,
            isContinuousToNext: Boolean,
            isContinuousFromPrevious: Boolean,
            isFailedMessage: Boolean,
            clickListener: OnItemClickListener?
        ) {
            view.agentDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            if (DeskUserRichMessage.isUrlPreviewType(message)) {
                try {
                    val info = DeskUserRichMessage.getUrlPreviewInfo(message)
                    if (info != null) {
                        if (info.mTitle.isNotEmpty() && info.mImageUrl.isNotEmpty()) {
                            view.agentPreviewLayer.visibility = View.VISIBLE
                            ImageUtil.displayImageFromUrl(
                                mContext,
                                info.mImageUrl,
                                view.imageAgentPreview,
                                null
                            )
                            view.previewAgentTitle.text = info.mTitle
                            view.previewAgentDomainName.text = info.getDomainName()
                        } else {
                            view.agentPreviewLayer.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                    view.agentPreviewLayer.visibility = View.GONE
                }
            } else {
                view.agentPreviewLayer.visibility = View.GONE
            }

            if (isContinuousToNext.not()) {
                view.agentTime.visibility = View.VISIBLE
                view.dividerAgent.visibility = View.VISIBLE
                view.agentProfile.visibility = View.VISIBLE

                Glide.with(mContext)
                        .load(message.sender.profileUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(view.agentProfile)

                view.userTime.text = DateUtils.formatTime(message.createdAt)
            } else {
                view.agentProfile.setBackgroundResource(R.drawable.profile_default)
                view.agentTime.visibility = View.GONE
                view.dividerAgent.visibility = View.GONE
            }

            view.agentName.apply {
                when {
                    isContinuousFromPrevious.not() -> {
                        visibility = View.VISIBLE
                        text = message.sender.nickname
                    }
                    else -> visibility = View.GONE
                }
            }

            if (isFailedMessage) {
                view.userMsg.setOnClickListener {
                    clickListener?.onUserMessageItemClick(message)
                }
            } else {
                view.previewLayer.setOnClickListener {
                    clickListener?.onUserMessageUrlPreviewClicked(message)
                }
            }
        }
    }

    inner class FileImageMessageMeHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            message: FileMessage,
            isNewDay: Boolean,
            isContinuousToNext: Boolean,
            isContinuousFromPrevious: Boolean,
            isFailedMessage: Boolean,
            isTempMessage: Boolean,
            tempFileMessageUri: Uri?,
            clickListener: OnItemClickListener?
        ) {
            view.imgDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            view.imgStatus.apply {
                when {
                    isFailedMessage == true -> {
                        text = "Failed"
                        visibility = View.VISIBLE
                    }

                    isTempMessage == true -> {
                        text = "Sending"
                        visibility = View.VISIBLE
                    }

                    else -> visibility = View.GONE
                }
            }

            if (isTempMessage && tempFileMessageUri != null) {
                ImageUtil.displayImageFromUrl(
                    mContext,
                    tempFileMessageUri.toString(),
                    view.imgThumbnail,
                    null
                )
            } else {
                // Get thumbnails from FileMessage
                val thumbnails = message.getThumbnails()

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size > 0) {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(
                            mContext,
                            message.url,
                            view.imgThumbnail,
                            thumbnails[0].url
                        )
                    } else {
                        ImageUtil.displayImageFromUrl(
                            mContext,
                            thumbnails[0].url,
                            view.imgThumbnail,
                            null
                        )
                    }
                } else {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(
                            mContext,
                            message.url,
                            view.imgThumbnail,
                            null
                        )
                    } else {
                        ImageUtil.displayImageFromUrl(
                            mContext,
                            thumbnails[0].url,
                            view.imgThumbnail,
                            null
                        )
                    }
                }
            }

            if (isContinuousToNext.not()) {
                view.imgTime.text = DateUtils.formatTime(message.createdAt)
                view.imgTime.visibility = View.VISIBLE
                view.dividerFile.visibility = View.VISIBLE
            } else {
                view.imgTime.visibility = View.GONE
                view.dividerFile.visibility = View.GONE
            }

            view.dividerFile.apply {
                when {
                    isContinuousFromPrevious.not() -> visibility = View.VISIBLE
                    else -> visibility = View.GONE
                }
            }

            view.imgThumbnail.setOnClickListener {
                clickListener?.onFileMessageItemClick(message)
            }
        }
    }

    inner class FileImageMessageAgentHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            message: FileMessage,
            isNewDay: Boolean,
            isContinuousToNext: Boolean,
            isContinuousFromPrevious: Boolean,
            isTempMessage: Boolean,
            tempFileMessageUri: Uri?,
            clickListener: OnItemClickListener?
        ) {
            view.imgAgentDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            if (isTempMessage && tempFileMessageUri != null) {
                ImageUtil.displayImageFromUrl(
                    mContext,
                    tempFileMessageUri.toString(),
                    view.imgAgentThumbnail,
                    null
                )
            } else {
                // Get thumbnails from FileMessage
                val thumbnails = message.getThumbnails()

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size > 0) {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(
                            mContext,
                            message.url,
                            view.imgAgentThumbnail,
                            thumbnails[0].url
                        )
                    } else {
                        ImageUtil.displayImageFromUrl(
                            mContext,
                            thumbnails[0].url,
                            view.imgAgentThumbnail,
                            null
                        )
                    }
                } else {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(
                            mContext,
                            message.url,
                            view.imgAgentThumbnail,
                            null
                        )
                    } else {
                        ImageUtil.displayImageFromUrl(
                            mContext,
                            thumbnails[0].url,
                            view.imgAgentThumbnail,
                            null
                        )
                    }
                }
            }

            if (isContinuousToNext.not()) {
                view.imgAgentTime.visibility = View.VISIBLE
                view.dividerImgAgent.visibility = View.VISIBLE

                Glide.with(mContext)
                        .load(message.sender.profileUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(view.imgAgentProfile)

                view.imgAgentTime.text = DateUtils.formatTime(message.createdAt)
            } else {
                view.imgAgentProfile.setBackgroundResource(R.drawable.profile_default)
                view.dividerImgAgent.visibility = View.GONE
                view.imgAgentTime.visibility = View.GONE
            }

            view.imgAgentName.apply {
                when {
                    isContinuousFromPrevious.not() -> {
                        visibility = View.VISIBLE
                        text = message.sender.nickname
                    }
                    else -> visibility = View.GONE
                }
            }

            view.imgAgentThumbnail.setOnClickListener {
                clickListener?.onFileMessageItemClick(message)
            }
        }
    }

    inner class FileVideoMessageMeHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            message: FileMessage,
            isNewDay: Boolean,
            isContinuousToNext: Boolean,
            isContinuousFromPrevious: Boolean,
            isFailedMessage: Boolean,
            isTempMessage: Boolean,
            tempFileMessageUri: Uri?,
            clickListener: OnItemClickListener?
        ) {
            view.videoDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            view.videoStatus.apply {
                when {
                    isFailedMessage == true -> {
                        text = "Failed"
                        visibility = View.VISIBLE
                    }

                    isTempMessage == true -> {
                        text = "Sending"
                        visibility = View.VISIBLE
                    }

                    else -> visibility = View.GONE
                }
            }

            if (isTempMessage && tempFileMessageUri != null) {
                ImageUtil.displayImageFromUrl(
                    mContext,
                    tempFileMessageUri.toString(),
                    view.videoThumbnail,
                    null
                )
            } else {
                // Get thumbnails from FileMessage
                val thumbnails = message.getThumbnails()

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size > 0) {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(
                            mContext,
                            message.url,
                            view.videoThumbnail,
                            thumbnails[0].url
                        )
                    } else {
                        ImageUtil.displayImageFromUrl(
                            mContext,
                            thumbnails[0].url,
                            view.videoThumbnail,
                            null
                        )
                    }
                } else {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(
                            mContext,
                            message.url,
                            view.videoThumbnail,
                            null
                        )
                    } else {
                        ImageUtil.displayImageFromUrl(
                            mContext,
                            thumbnails[0].url,
                            view.videoThumbnail,
                            null
                        )
                    }
                }
            }

            if (isContinuousToNext.not()) {
                view.videoTime.text = DateUtils.formatTime(message.createdAt)
                view.videoTime.visibility = View.VISIBLE
                view.dividerVideoFile.visibility = View.VISIBLE
            } else {
                view.videoTime.visibility = View.GONE
                view.dividerVideoFile.visibility = View.GONE
            }

            view.dividerVideoFile.apply {
                when {
                    isContinuousFromPrevious.not() -> visibility = View.VISIBLE
                    else -> visibility = View.GONE
                }
            }

            if (isFailedMessage) {
                view.videoThumbnail.setOnClickListener {
                    clickListener?.onFileMessageItemClick(message)
                }
            } else {
                view.videoPlay.setOnClickListener {
                    clickListener?.onFileMessageItemClick(message)
                }
            }
        }
    }

    inner class FileVideoMessageAgentHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            message: FileMessage,
            isNewDay: Boolean,
            isContinuousToNext: Boolean,
            isContinuousFromPrevious: Boolean,
            isFailedMessage: Boolean,
            isTempMessage: Boolean,
            tempFileMessageUri: Uri?,
            clickListener: OnItemClickListener?
        ) {
            view.videoAgentDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            if (isTempMessage && tempFileMessageUri != null) {
                ImageUtil.displayImageFromUrl(
                    mContext,
                    tempFileMessageUri.toString(),
                    view.videoAgentThumbnail,
                    null
                )
            } else {
                // Get thumbnails from FileMessage
                val thumbnails = message.getThumbnails()

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size > 0) {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(
                            mContext,
                            message.url,
                            view.videoAgentThumbnail,
                            thumbnails[0].url
                        )
                    } else {
                        ImageUtil.displayImageFromUrl(
                            mContext,
                            thumbnails[0].url,
                            view.videoAgentThumbnail,
                            null
                        )
                    }
                } else {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtil.displayGifImageFromUrl(
                            mContext,
                            message.url,
                            view.videoAgentThumbnail,
                            null
                        )
                    } else {
                        ImageUtil.displayImageFromUrl(
                            mContext,
                            thumbnails[0].url,
                            view.videoAgentThumbnail,
                            null
                        )
                    }
                }
            }

            if (isContinuousToNext.not()) {
                view.videoAgentTime.visibility = View.VISIBLE
                view.dividerVideoAgent.visibility = View.VISIBLE

                Glide.with(mContext)
                        .load(message.sender.profileUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(view.videoAgentProfile)

                view.videoAgentTime.text = DateUtils.formatTime(message.createdAt)
            } else {
                view.videoAgentProfile.setBackgroundResource(R.drawable.profile_default)
                view.dividerVideoAgent.visibility = View.GONE
                view.videoAgentTime.visibility = View.GONE
            }

            view.videoAgentName.apply {
                when {
                    isContinuousFromPrevious.not() -> {
                        visibility = View.VISIBLE
                        text = message.sender.nickname
                    }
                    else -> visibility = View.GONE
                }
            }

            if (isFailedMessage) {
                view.videoAgentThumbnail.setOnClickListener {
                    clickListener?.onFileMessageItemClick(message)
                }
            } else {
                view.videoAgentPlay.setOnClickListener {
                    clickListener?.onFileMessageItemClick(message)
                }
            }
        }
    }

    inner class AdminMessageHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            message: AdminMessage,
            isNewDay: Boolean
        ) {
            view.adminDate.apply {
                when {
                    isNewDay == true -> {
                        visibility = View.VISIBLE
                        text = DateUtils.formatDate(message.createdAt)
                    }

                    else -> visibility = View.GONE
                }
            }

            view.adminMsg.text = message.message
        }
    }

    inner class UnSupportHolder(var view: View) : RecyclerView.ViewHolder(view), SBPetAdapter.Listener, SBCategoryAdapter.Listener {
        private var mAdapter:SBPetAdapter? = null
        private var mCategoryAdapter:SBCategoryAdapter? = null

        override fun onItemClicked(item: PetInfoItem) {
            view.stepPetLayer.visibility = View.VISIBLE
            view.selectPetTime.text = DateUtils.formatTimeWithMarker(System.currentTimeMillis())
            view.selectPetName.text = item.name
            view.secondMsgTime.text = DateUtils.formatTimeWithMarker(System.currentTimeMillis())
            view.secondMsgLayer.visibility = View.VISIBLE
            view.categoryList.visibility = View.VISIBLE

            mCategoryAdapter = SBCategoryAdapter().apply { setListener(this@UnSupportHolder) }
            view.categoryList.apply {
                layoutManager = FlexboxLayoutManager(mContext).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                }

                adapter = mCategoryAdapter
            }

            mCategoryAdapter!!.addAll(PetdocApplication.mSearchCategoryList)
        }

        override fun onCategoryClicked(item: ChatCategoryItem) {
            view.stepCategoryLayer.visibility = View.VISIBLE
            view.selectCategoryTime.text = DateUtils.formatTimeWithMarker(System.currentTimeMillis())
            view.selectCategory.text = item.name
            view.lastMsgLayer.visibility = View.VISIBLE
            view.lastMsgTime.text = DateUtils.formatTimeWithMarker(System.currentTimeMillis())

            view.btnYes.setOnClickListener {
                view.confirmLayer.visibility = View.VISIBLE
                view.confirmTime.text = DateUtils.formatTimeWithMarker(System.currentTimeMillis())
                mListener?.startChat()
            }

            view.btnNo.setOnClickListener { Logger.d("chat no clicked") }
        }

        fun bind() {
            view.date.text = DateUtils.formatDate(System.currentTimeMillis())
            view.firstMsgTime.text = DateUtils.formatTimeWithMarker(System.currentTimeMillis())

            mAdapter = SBPetAdapter(view.context).apply { setListener(this@UnSupportHolder) }
            view.petList.apply {
                layoutManager = LinearLayoutManager(mContext).apply {
                    orientation = LinearLayoutManager.HORIZONTAL }

                adapter = mAdapter

                addItemDecoration(HorizontalMarginItemDecoration(
                        marginStart = margin20,
                        marginBetween = margin4,
                        marginEnd = margin20
                ))
            }

            mAdapter!!.addAll(petInfoItems.toMutableList())

        }
    }

    //==============================================================================================
    private fun isContinuousToNext(currentMsg: BaseMessage?, nextMsg: BaseMessage?): Boolean {
        // null check
        if (currentMsg == null || nextMsg == null) {
            return false
        }

        if (currentMsg is AdminMessage && nextMsg is AdminMessage) {
            return true
        }

        val currentCustomType = if (currentMsg is UserMessage) currentMsg.customType else if (currentMsg is AdminMessage) currentMsg.customType else ""
        val nextCustomType = if (nextMsg is UserMessage) nextMsg.customType else if (nextMsg is AdminMessage) nextMsg.customType else ""
        if (currentCustomType != nextCustomType) {
            return false
        } else if (DeskUserRichMessage.isInquireCloserType(currentMsg)) {
            return true
        }

        val currentCreatedAt = currentMsg.createdAt
        val nextCreatedAt = nextMsg.createdAt

        // Greater than 1 minutes.
        if (nextCreatedAt - currentCreatedAt > 60 * 1000) {
            return false
        }

        var currentUser: User? = null
        var nextUser: User? = null
        if (currentMsg is UserMessage) {
            currentUser = currentMsg.sender
        } else if (currentMsg is FileMessage) {
            currentUser = currentMsg.sender
        }

        if (nextMsg is UserMessage) {
            nextUser = nextMsg.sender
        } else if (nextMsg is FileMessage) {
            nextUser = nextMsg.sender
        }

        // If admin message or
        return (!(currentUser == null || nextUser == null)
                && currentUser.userId == nextUser.userId)
    }

    private fun isContinuousFromPrevious(currentMsg: BaseMessage?, precedingMsg: BaseMessage?): Boolean {
        // null check
        if (currentMsg == null || precedingMsg == null) {
            return false
        }

        if (currentMsg is AdminMessage && precedingMsg is AdminMessage) {
            return true
        }

        val currentCustomType = if (currentMsg is UserMessage) currentMsg.customType else if (currentMsg is AdminMessage) currentMsg.customType else ""
        val precedingCustomType = if (precedingMsg is UserMessage) precedingMsg.customType else if (precedingMsg is AdminMessage) precedingMsg.customType else ""
        if (currentCustomType != precedingCustomType) {
            return false
        } else if (DeskUserRichMessage.isInquireCloserType(currentMsg)) {
            return true
        }

        val currentCreatedAt = currentMsg.createdAt
        val precedingCreatedAt = precedingMsg.createdAt

        // Greater than 1 minutes.
        if (currentCreatedAt - precedingCreatedAt > 60 * 1000) {
            return false
        }

        var currentUser: User? = null
        var precedingUser: User? = null
        if (currentMsg is UserMessage) {
            currentUser = currentMsg.sender
        } else if (currentMsg is FileMessage) {
            currentUser = currentMsg.sender
        }

        if (precedingMsg is UserMessage) {
            precedingUser = precedingMsg.sender
        } else if (precedingMsg is FileMessage) {
            precedingUser = precedingMsg.sender
        }

        // If admin message or
        return (!(currentUser == null || precedingUser == null)
                && currentUser.userId == precedingUser.userId)
    }

    //===============================================================================================
    interface OnItemClickListener {
        fun onUserMessageItemClick(message: UserMessage)
        fun onUserMessageUrlPreviewClicked(message: UserMessage)
        fun onFileMessageItemClick(message: FileMessage)
        fun startChat()
    }
}