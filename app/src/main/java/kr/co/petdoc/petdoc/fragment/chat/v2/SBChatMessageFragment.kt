package kr.co.petdoc.petdoc.fragment.chat.v2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import com.sendbird.android.BaseChannel.DeleteMessageHandler
import kotlinx.android.synthetic.main.fragment_sb_chat_message.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.chat.v2.SBChatAdapter
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.fragment.chat.v2.utils.MediaPlayerActivity
import kr.co.petdoc.petdoc.fragment.chat.v2.utils.PhotoViewerActivity
import kr.co.petdoc.petdoc.fragment.chat.v2.vo.ChatMediaInfo
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.utils.*
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

/**
 * Petdoc
 * Class: SBChatMessageFragment
 * Created by kimjoonsung on 12/3/20.
 *
 * Description :
 */
class SBChatMessageFragment : BaseFragment(), SBChatAdapter.OnItemClickListener, SBChatAdapter.OnItemLongClickListener {

    companion object {
        private const val CHANNEL_LIST_LIMIT = 30
        private const val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT"
        private const val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT"
        private const val STATE_NORMAL = 0
        private const val STATE_EDIT = 1
        private const val INTENT_REQUEST_CHOOSE_IMAGE = 300
    }

    private lateinit var chatAdapter: SBChatAdapter
    private var mediaList:MutableList<ChatMediaInfo> = mutableListOf()

    private var channelUrl = ""
    private var channel: OpenChannel? = null
    private var prevMessageListQuery: PreviousMessageListQuery? = null

    private var currentState = STATE_NORMAL
    private var editingMessage: BaseMessage? = null

    private var triggerPoint = Int.MAX_VALUE

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_sb_chat_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)
        retainInstance = true

        // 카메라 및 파일 접근 권한 추가
        Helper.permissionCheck(
                requireActivity(),
                arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                object : PermissionCallback {
                    override fun callback(status: PermissionStatus) {
                        when (status) {
                            PermissionStatus.ALL_GRANTED -> {
                            }
                            PermissionStatus.DENIED_SOME -> {
                                //finish()
                            }
                        }
                    }
                }, true
        )

        channelUrl = arguments?.getString("url") ?: ""
        Logger.d("channelUrl : $channelUrl")

        //==========================================================================================
        btnPhoto.setOnClickListener { requestImage() }
        btnSendMsg.setOnClickListener { sendMsg() }

        editTextChat.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editTextChat.apply {
                    when {
                        s?.length?.compareTo(0)!! > 0 -> {
                            btnSendMsg.apply {
                                isEnabled = true
                                setBackgroundResource(R.drawable.ic_send_enable)
                            }
                        }
                        else -> {
                            btnSendMsg.apply {
                                isEnabled = false
                                setBackgroundResource(R.drawable.ic_send_disable)
                            }
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}
        })

        //==========================================================================================
        chatAdapter = SBChatAdapter(context = requireContext()).apply {
            setClickListener(this@SBChatMessageFragment)
            setLongClickListener(this@SBChatMessageFragment)
        }

        messageList.apply {
            layoutManager = LinearLayoutManager(requireActivity()).apply {
                reverseLayout = true
            }

            adapter = chatAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    messageList.layoutManager?.let { lm ->
                        val last = (lm as LinearLayoutManager).findLastVisibleItemPosition()
                        if (last > triggerPoint) {
                            triggerPoint = Int.MAX_VALUE
                            loadNextMessageList(CHANNEL_LIST_LIMIT)
                        }
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()

        SBConnectionManager.addConnectionManagementHandler(
                requireContext(),
                CONNECTION_HANDLER_ID,
                object : SBConnectionManager.ConnectionManagementHandler {
                    override fun onConnected(reconnect: Boolean) {
                        if (reconnect) {
                            refresh()
                        } else {
                            refreshFirst()
                        }
                    }
                })

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                if (baseChannel.url == channelUrl) {
                    chatAdapter.addFirst(baseMessage)
                }
            }

            override fun onMessageDeleted(baseChannel: BaseChannel, msgId: Long) {
                super.onMessageDeleted(baseChannel, msgId)
                if (baseChannel.url == channelUrl) {
                    chatAdapter.deleteMessage(msgId)
                }
            }

            override fun onMessageUpdated(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                super.onMessageUpdated(baseChannel, baseMessage)
                if (baseChannel.url == channelUrl) {
                    chatAdapter.updateMessage(baseMessage)
                }
            }
        })
    }

    override fun onPause() {
        SBConnectionManager.removeConnectionManagementHandler(CHANNEL_HANDLER_ID)
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        super.onPause()
    }

    override fun onDestroyView() {
        if (channel != null) {
            channel!!.exit { e ->
                if (e != null) {
                    Logger.p(e)
                    return@exit
                }
            }
        }
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Set this as true to restart auto-background detection.
        // This means that you will be automatically disconnected from SendBird when your
        // app enters the background.
        SendBird.setAutoBackgroundDetection(true)

        when (requestCode) {
            INTENT_REQUEST_CHOOSE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.data != null) {
                        showUploadConfirmDialog(data?.data!!)
                    }
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onAdminMessageItemClick(message: AdminMessage) {
        //TODO : admin message click
    }

    override fun onAdminMessageItemLongClick(message: AdminMessage) {
        //TODO : admin message long click
    }

    override fun onFileMessageItemClick(message: FileMessage) {
        val type = message.type.toLowerCase()
        if (type.startsWith("image")) {
            startActivity(Intent(requireActivity(), PhotoViewerActivity::class.java).apply {
                putExtra("url", message.url)
                putExtra("type", message.type)
            })
        } else if (type.startsWith("video")) {
            startActivity(Intent(requireActivity(), MediaPlayerActivity::class.java).apply {
                putExtra("url", message.url)
            })
        } else {
            showDownloadConfirmDialog(message)
        }
    }

    override fun onFileMessageItemLongClick(message: FileMessage) {
        //TODO : file message long click
    }

    override fun onUserMessageItemClick(message: UserMessage) {
        //TODO : user message click
    }

    override fun onUserMessageItemLongClick(message: UserMessage, position: Int) {
        if (message.sender.userId == SendBird.getCurrentUser().userId) {
            showMessageOptionsDialog(message, position)
        }
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of request.
     *
     * @param response response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {

        }
    }

    /**
     * 소프트 키보드가 올라오면 이벤트가 넘어온다.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(bus: SoftKeyboardBus) {
        // 키보드가 올라가면 리스트 제일 끝으로 스크롤
        if (bus.height > 100) {
            emptyView?.layoutParams?.height = bus.height
            emptyView?.invalidate()
            emptyView?.requestLayout()

            if(editTextChat.text.toString().isNotEmpty()) {
                btnSendMsg.isEnabled = true
                btnSendMsg.setBackgroundResource(R.drawable.ic_send_enable)
            }

        } else {
            emptyView?.layoutParams?.height = 1
            emptyView?.invalidate()
            emptyView?.requestLayout()

            btnSendMsg.isEnabled = false
            btnSendMsg.setBackgroundResource(R.drawable.ic_send_disable)
        }
    }

    //==============================================================================================
    private fun requestImage() {
        val intent = Intent().apply {
            setType("image/* video/*")
            setAction(Intent.ACTION_GET_CONTENT)
        }

        startActivityForResult(
                Intent.createChooser(intent, "Select Media"),
                INTENT_REQUEST_CHOOSE_IMAGE
        )

        // Set this as false to maintain connection
        // even when an external Activity is started.
        SendBird.setAutoBackgroundDetection(false)
    }

    private fun sendMsg() {
        val userInput = editTextChat.text.toString()
        when (currentState) {
            STATE_EDIT -> {
                if (userInput.isNotEmpty()) {
                    if (editingMessage != null) {
                        editMessage(editingMessage!!, userInput)
                    }
                }

                setState(STATE_NORMAL, null, -1)
            }

            else -> {
                if (userInput.isNotEmpty()) {
                    sendUserMessage(userInput)
                    editTextChat.setText("")
                }
            }
        }
    }

    private fun refresh() {
        loadInitialMessageList(CHANNEL_LIST_LIMIT)
    }

    private fun refreshFirst() {
        enterChannel(channelUrl)
    }

    /**
     * Replaces current message list with new list.
     * Should be used only on initial load.
     */
    private fun loadInitialMessageList(numMessage: Int) {
        prevMessageListQuery = channel?.createPreviousMessageListQuery()
        prevMessageListQuery?.load(numMessage, true, { list, exception ->
            if (exception != null) {
                Logger.p(exception)
                return@load
            }

            chatAdapter.setMessageList(list)
        })
    }

    /**
     * Enters an Open Channel.
     * <p>
     * A user must successfully enter a channel before being able to load or send messages
     * within the channel.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    private fun enterChannel(channelUrl: String) {
        OpenChannel.getChannel(channelUrl, { openChannel, sendBirdException ->
            if (sendBirdException != null) {
                Logger.p(sendBirdException)
                return@getChannel
            }

            openChannel?.enter({ e ->
                if (e != null) {
                    Logger.p(e)
                    return@enter
                }

                channel = openChannel
                refresh()
            })
        })
    }

    private fun showMessageOptionsDialog(message: BaseMessage, position: Int) {
        val options = arrayOf("Edit message", "Delete message")
        val builder = AlertDialog.Builder(requireActivity())
        builder.setItems(options) { dialog, which ->
            if (which == 0) {
                setState(STATE_EDIT, message, position)
            } else if (which == 1) {
                deleteMessage(message)
            }
        }
        builder.create().show()
    }

    private fun setState(state: Int, message: BaseMessage?, position: Int) {
        when (state) {
            STATE_NORMAL -> {
                currentState = STATE_NORMAL
                editingMessage = null

                btnPhoto.visibility = View.VISIBLE
                editTextChat.setText("")
            }

            else -> {
                currentState = STATE_EDIT
                editingMessage = message

                btnPhoto.visibility = View.GONE

                var messageString = (editingMessage as UserMessage).message
                if (messageString == null) {
                    messageString = ""
                }

                editTextChat.apply {
                    setText(messageString)

                    if (messageString.length > 0) {
                        editTextChat.setSelection(0, messageString.length)
                    }

                    requestFocus()

                    postDelayed({
                        showKeyBoardOnView(this)
                        messageList.postDelayed({
                            messageList.scrollToPosition(position)
                        }, 500)
                    }, 100)
                }
            }
        }
    }

    /**
     * Deletes a message within the channel.
     * Note that users can only delete messages sent by oneself.
     *
     * @param message The message to delete.
     */
    private fun deleteMessage(message: BaseMessage) {
        channel?.deleteMessage(message, DeleteMessageHandler { e ->
            if (e != null) {
                // Error!
                Toast.makeText(activity, "Error " + e.code + ": " + e.message, Toast.LENGTH_SHORT)
                        .show()
                return@DeleteMessageHandler
            }
            refresh()
        })
    }

    /**
     * Loads messages and adds them to current message list.
     *
     *
     * A PreviousMessageListQuery must have been already initialized through [.loadInitialMessageList]
     */
    @Throws(NullPointerException::class)
    private fun loadNextMessageList(numMessages: Int) {
        if (channel == null) {
            throw NullPointerException("Current channel instance is null.")
        }
        if (prevMessageListQuery == null) {
            throw NullPointerException("Current query instance is null.")
        }

        prevMessageListQuery!!.load(numMessages, true,
                PreviousMessageListQuery.MessageListQueryResult { list, e ->
                    if (e != null) {
                        // Error!
                        e.printStackTrace()
                        return@MessageListQueryResult
                    }
                    for (message in list) {
                        chatAdapter.addLast(message)
                    }
                })
    }

    private fun showDownloadConfirmDialog(message: FileMessage) {
        OneBtnDialog(requireActivity()).apply {
            setTitle("")
            setMessage("Download file?")
            setCancelable(true)
            setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                requireContext().downloadFile(message.url, message.name)
            })
            show()
        }
    }

    private fun showUploadConfirmDialog(uri: Uri) {
        OneBtnDialog(requireActivity()).apply {
            setTitle("")
            setMessage("Upload file?")
            setCancelable(true)
            setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                val thumbnailSize = arrayListOf<FileMessage.ThumbnailSize>()
                thumbnailSize.add(FileMessage.ThumbnailSize(240, 240))
                thumbnailSize.add(FileMessage.ThumbnailSize(320, 320))

                sendImageWithThumbnail(uri, thumbnailSize)
            })
            show()
        }
    }

    private fun sendUserMessage(text: String) {
        channel?.sendUserMessage(text, { userMessage, sendBirdException ->
            if (sendBirdException != null) {
                Logger.p(sendBirdException)
                AppToast(requireContext()).showToastMessage(
                        "Send failed with error ${sendBirdException.code} : ${sendBirdException.message}",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                )

                return@sendUserMessage
            }

            chatAdapter.addFirst(userMessage)
        })
    }

    /**
     * Sends a File Message containing an image file.
     * Also requests thumbnails to be generated in specified sizes.
     *
     * @param uri : The URI of the image, which in this case is received through an Intent request.
     * @param thumbnailSize
     */
    private fun sendImageWithThumbnail(uri: Uri, thumbnailSize: List<FileMessage.ThumbnailSize>) {
        val info = requireContext().getFileInfo(uri)

        if (info == null || info.isEmpty) {
            Logger.d("Extracting file information faile.")
            return
        }

        var name = ""
        if (info.contains("name")) {
            name = info["name"].toString()
        } else {
            name = "SendBird File"
        }

        val path = info["path"].toString()
        val file = File(path)
        val mime = info["mime"].toString()
        val size = info["size"].toString().toInt()

        if (path.isEmpty()) {
            Logger.d("File must be located in local storage")
        } else {
            // Send image with thumbnails in the specified dimensions
            channel?.sendFileMessage(file, name, mime, size, "", null, thumbnailSize, { fileMessage, sendBirdException ->
                if (sendBirdException != null) {
                    Logger.d("Error code ${sendBirdException.code}, message ${sendBirdException.message}")
                    return@sendFileMessage
                }

                chatAdapter.addFirst(fileMessage)
            })
        }
    }

    private fun editMessage(message: BaseMessage, editedMsg: String) {
        channel?.updateUserMessage(message.messageId, editedMsg, null, null, { userMessage, sendBirdException ->
            if (sendBirdException != null) {
                Logger.d("Error ${sendBirdException.code}, ${sendBirdException.message}")
                return@updateUserMessage
            }

            refresh()
        })
    }

    private fun getMediaAll() {
        val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
        )

        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        val queryUri = MediaStore.Files.getContentUri("external")

        val cursorLoader = CursorLoader(
                requireActivity(),
                queryUri,
                projection,
                selection,
                null,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )

        val cursor = cursorLoader.loadInBackground()
        if (cursor != null) {
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)
                val dataColumnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                val path = cursor.getString(dataColumnIndex)
                val type = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val mediaType = cursor.getInt(type)

                val bmOptions = BitmapFactory.Options().apply {
                    inSampleSize = 4
                    inPurgeable = true
                }

                var thumnail: Bitmap? = null
                var duration = 0L
                if (mediaType == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        thumnail = ThumbnailUtils.createImageThumbnail(
                                File(path),
                                Size(96, 96),
                                CancellationSignal()
                        )
                    } else {
                        thumnail = MediaStore.Images.Thumbnails.getThumbnail(
                                requireActivity().contentResolver,
                                id,
                                MediaStore.Video.Thumbnails.MINI_KIND,
                                bmOptions
                        )
                    }
                } else if (mediaType == 3) {
                    val file = File(path)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        thumnail = ThumbnailUtils.createVideoThumbnail(
                                file,
                                Size(96, 96),
                                CancellationSignal()
                        )
                    } else {
                        thumnail = MediaStore.Video.Thumbnails.getThumbnail(
                                requireActivity().contentResolver,
                                id,
                                MediaStore.Video.Thumbnails.MINI_KIND,
                                bmOptions
                        )
                    }

                    if (file.getMediaDuration(requireContext(), path) > 0) {
                        duration = file.getMediaDuration(requireContext(), path)
                    }
                }

                val media = ChatMediaInfo(
                        id,
                        path,
                        mediaType,
                        duration,
                        thumnail!!
                )

                mediaList.add(media)
            }

            Logger.d("mediaList : $mediaList")
        }
    }
}