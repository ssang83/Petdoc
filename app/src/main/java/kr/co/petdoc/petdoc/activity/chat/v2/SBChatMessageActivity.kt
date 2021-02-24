package kr.co.petdoc.petdoc.activity.chat.v2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sendbird.android.*
import com.sendbird.android.FileMessage.ThumbnailSize
import com.sendbird.desk.android.Ticket
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_sb_chat_message.*
import kotlinx.android.synthetic.main.dialog_content_chooser.*
import kotlinx.android.synthetic.main.dialog_content_chooser.view.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.adapter.chat.v2.SBMessageAdapter
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.fragment.chat.v2.SBConnectionManager
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskAdminMessage
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskManager
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskManager.Companion.CONNECTION_HANDLER_ID_CHAT
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskManager.Companion.TICKET_HANDLER_ID_CHAT
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskUserRichMessage
import kr.co.petdoc.petdoc.fragment.chat.v2.utils.MediaPlayerActivity
import kr.co.petdoc.petdoc.fragment.chat.v2.utils.PhotoViewerActivity
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.*
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.web.WebUtils
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import java.io.*
import java.util.*

/**
 * Petdoc
 * Class: SBChatMessageActivity
 * Created by kimjoonsung on 12/9/20.
 *
 * Description :
 */
class SBChatMessageActivity : BaseActivity(), SBMessageAdapter.OnItemClickListener {

    companion object {
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_USER_NAME = "EXTRA_USER_NAME"
        const val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"

        private const val INTENT_REQUEST_CHOOSE_MEDIA = 0xf0
        private const val INTENT_REQUEST_CAMERA = 0xf1
        private const val INTENT_REQUEST_TO_UPLOAD_VIDEO = 0xf2
        private const val INTENT_REQUEST_TO_RECORD_VIDEO = 0xf3
        private const val INTENT_REQUEST_CHOOSE_IMAGE = 300

        private const val FILE_TYPE_ALL = 0
        private const val FILE_TYPE_IMAGE = 1
        private const val FILE_TYPE_VIDEO = 2
    }

    private var messageAdapter:SBMessageAdapter? = null
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    private var isScrolling = false

    private var ticket: Ticket? = null
    private var channelUrl = ""

    private var channel: GroupChannel? = null

    private var minMessageTimestamp = 0L
    private var loading = false
    private var hasPrev = false

    private var requestingCamera = false
    private var tempPhotoUri: Uri? = null
    private var tempPhotoPreviewUri: Uri? = null
    private var tempVideoUri: Uri? = null
    private var tempFileUri: Uri? = null

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var mPetInfoItems = listOf<PetInfoItem>()

    private var title = ""
    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sb_chat_message)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        // 카메라 및 파일 접근 권한 추가
        Helper.permissionCheck(
                this,
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

        //==========================================================================================
        // Starts from startChat.
        title = intent?.getStringExtra(EXTRA_TITLE) ?: ""
        userName = intent?.getStringExtra(EXTRA_USER_NAME) ?: ""

        // Starts from inbox or push. Title is also passed by EXTRA_TITLE.
        channelUrl = intent?.getStringExtra(EXTRA_CHANNEL_URL) ?: ""

        if (savedInstanceState != null) {
            channelUrl = savedInstanceState.getString(EXTRA_CHANNEL_URL) ?: ""
        }

        Logger.d("title : $title, userName : $userName, channelUrl : $channelUrl")
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
        messageList.apply {
            layoutManager = LinearLayoutManager(this@SBChatMessageActivity)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        isScrolling = false

                        messageList.layoutManager?.let { lm ->
                            val first = (lm as LinearLayoutManager).findFirstVisibleItemPosition()
                            val childCount = (recyclerView as ViewGroup).childCount
                            val top = (recyclerView as ViewGroup).getChildAt(0).top
                            if (first == 0 && childCount > 0 && top == 0) {
                                loadPreviousMessage(false, null)
                            }
                        }
                    } else {
                        isScrolling = true
                    }
                }
            })
        }

        //===========================================================================================
        initBottomsheetDialog()

        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        Handler().postDelayed( { mKeyboardHeightProvider!!.start() }, 1000)
    }

    override fun onResume() {
        super.onResume()

        loadPetInfo()

        if (messageAdapter == null) {
            messageAdapter = SBMessageAdapter(this, mPetInfoItems).apply { setListener(this@SBChatMessageActivity) }
            messageList.apply {
                adapter = messageAdapter
            }
        }

        DeskManager.addTicketHandler(TICKET_HANDLER_ID_CHAT, object : DeskManager.TicketHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                if (channelUrl == baseChannel.url) {
                    // If the first message comes (This must be welcome or away message).
                    if (channel != null) {
                        channel!!.markAsRead()
                    }

                    if (DeskAdminMessage.isMessage(baseMessage)) {
                        // If ticket close event is triggered, disables user interaction on reply view.
                        if (DeskAdminMessage.isCloseType(baseMessage)) {
                            layoutChatInput.visibility = View.GONE
                        }
                    } else {
                        messageAdapter?.appendMessage(baseMessage)
                        messageAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onMessageUpdated(baseChannel: BaseChannel, message: BaseMessage) {
                if (baseChannel.url == channelUrl) {
                    messageAdapter?.replaceMessage(message)
                    messageAdapter?.notifyDataSetChanged()
                }
            }

            override fun onChannelChanged(baseChannel: BaseChannel) {}
        })

        SBConnectionManager.addConnectionManagementHandler(this, CONNECTION_HANDLER_ID_CHAT, object : SBConnectionManager.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                refresh()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        DeskManager.removeTicketHandler(TICKET_HANDLER_ID_CHAT)
        SBConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID_CHAT)

        if (channel != null) {
            channel!!.endTyping()
        }

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider!!.close()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_CHANNEL_URL, channelUrl)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            INTENT_REQUEST_CHOOSE_MEDIA -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = data?.data
                    if (uri != null) {
                        val info = getFileInfo(uri)
                        if (info != null) {
                            val mime = info["mime"].toString()
                            if (mime.toLowerCase().contains("gif")) {
                                sendFileMessage(uri, FILE_TYPE_ALL)
                                return
                            }
                        }
                    }

                    previewPhoto(uri)
                }
            }

            INTENT_REQUEST_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (requestingCamera.not()) return

                    previewPhoto(tempPhotoUri)
                    requestingCamera = false
                }
            }

            UCrop.REQUEST_CROP -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        sendFileMessage(UCrop.getOutput(data), FILE_TYPE_IMAGE)
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    if (data != null) {
                        Logger.p(UCrop.getError(data))
                    }
                }
            }

            INTENT_REQUEST_TO_UPLOAD_VIDEO -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return

                    sendFileMessage(data.data, FILE_TYPE_VIDEO)
                }
            }

            INTENT_REQUEST_TO_RECORD_VIDEO -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (requestingCamera.not()) return

                    sendFileMessage(tempVideoUri, FILE_TYPE_VIDEO)
                    requestingCamera = false
                }
            }

            INTENT_REQUEST_CHOOSE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if(data == null) return

                    val thumbnailSize = mutableListOf<ThumbnailSize>()
                    thumbnailSize.add(ThumbnailSize(240, 240))
                    thumbnailSize.add(ThumbnailSize(320, 320))

                    sendImageWithThumbnail(data.data, thumbnailSize)
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onFileMessageItemClick(message: FileMessage) {
        if (messageAdapter?.isFailedMessage(message)!!) {
            retryFailedMessage(message)
        } else if (messageAdapter?.isTempMessage(message)!!.not()) {
            val fileType = messageAdapter?.getFileType(message)!!
            when (fileType) {
                FILE_TYPE_IMAGE -> {
                    startActivity(Intent(this, PhotoViewerActivity::class.java).apply {
                        putExtra("url", message.url)
                        putExtra("type", message.type)
                        putExtra("name", message.name)
                    })
                }

                FILE_TYPE_VIDEO -> {
                    startActivity(Intent(this, MediaPlayerActivity::class.java).apply {
                        putExtra("url", message.url)
                        putExtra("type", message.type)
                        putExtra("name", message.name)
                    })
                }

                else -> { showDownloadConfirmDialog(message) }
            }
        }
    }

    override fun onUserMessageItemClick(message: UserMessage) {
        if (messageAdapter?.isFailedMessage(message)!!) {
            retryFailedMessage(message)
        }
    }

    override fun onUserMessageUrlPreviewClicked(message: UserMessage) {
        if (DeskUserRichMessage.isUrlPreviewType(message)) {
            try {
                val info = DeskUserRichMessage.getUrlPreviewInfo(message)
                Logger.d("url : ${info!!.mUrl}")
                //TODO : 웹뷰로 이동
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    override fun startChat() {
        createTicket(title, userName)
    }

    /**
     * 소프트 키보드가 올라오면 이벤트가 넘어온다.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(bus: SoftKeyboardBus) {
        Logger.e("height:" + bus.height)

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

    //===============================================================================================
    private fun initBottomsheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_content_chooser, null)

        view.layout_gallery.setOnClickListener {
            requestMedia()
            bottomSheetDialog?.dismiss()
        }

        view.layout_camera.setOnClickListener {
            requestCamera()
            bottomSheetDialog?.dismiss()
        }

        view.layout_upload_video.setOnClickListener {
            requestToUploadVideo()
            bottomSheetDialog?.dismiss()
        }

        view.layout_take_video.setOnClickListener {
            requestToRecordVideo()
            bottomSheetDialog?.dismiss()
        }

        bottomSheetDialog?.setContentView(view)
    }

    private fun requestImage() {
        val intent = Intent().apply {
            setType("image/* video/*")
            setAction(Intent.ACTION_GET_CONTENT)
        }

        startActivityForResult(
                Intent.createChooser(intent, "Select Media"),
                INTENT_REQUEST_CHOOSE_IMAGE
        )
    }

    private fun createTicket(title: String, nickName: String) {
        Ticket.create(title, nickName, { ticket, exception ->
            if (exception != null) {
                AppToast(this).showToastMessage(
                        "채팅방 생성에 실패하였습니다. 다시 시도해 주세요.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                )

                return@create
            }

            this.ticket = ticket
            this.channel = ticket.channel
            this.channelUrl = ticket.channel.url

            Logger.d("ticket : ${this.ticket}, channel : ${this.channel}, channelUrl : ${this.channelUrl}")
        })
    }

    private fun sendMsg() {
        val userInput = editTextChat.text.toString()
        if(userInput.isNullOrEmpty()) return

        sendUserMessage(userInput)
        editTextChat.setText("")

        if (channel != null) {
            channel!!.endTyping()
        }
    }

    private fun sendUserMessage(msg: String) {
        if (channel != null) {
            val tempMessage = channel!!.sendUserMessage(msg, { userMessage, exception ->
                if (exception != null) {
                    messageAdapter?.markMessageFailed(userMessage.requestId)
                    return@sendUserMessage
                }

                messageAdapter?.markMessageSent(userMessage)

                val urls = WebUtils.extractUrls(msg)
                if (urls.size > 0) {
                    updateUserMessageWithUrl(userMessage, msg, urls[0])
                }
            })

            addTempMessage(tempMessage)
        }
    }

    private fun sendFileMessage(uri: Uri?, fileType: Int) {
        if (channel != null) {
            if(uri == null) {
                Logger.d("Uri is null...")
                return
            }

            val info = getFileInfo(uri)
            if (info == null) {
                Logger.d("Extracting file information failed.")
                return
            }

            var path = info["path"].toString()
            var mime = info["mime"].toString()
            var file:File
            var name = ""
            var size = 0
            var tempUri = uri

            if (path.isEmpty()) {
                if (tempFileUri != null) {
                    tempUri = tempFileUri as Uri
                    path = tempUri.path!!
                }
            }

            if (fileType == FILE_TYPE_IMAGE || mime.startsWith("image").not()) {
                mime = "image/jpeg"
            } else if (fileType == FILE_TYPE_VIDEO || mime.startsWith("video").not()) {
                mime = "video/mp4"
            } else if (fileType == FILE_TYPE_ALL) {
                if (path.isNotEmpty()) {
                    if (path.endsWith(".mp4")) {
                        mime = "video/mp4"
                    } else if (path.endsWith(".jpg")) {
                        mime = "image/jpg"
                    } else if (path.endsWith(".gif")) {
                        mime = "image/gif"
                    } else if (path.endsWith(".png")) {
                        mime = "image/png"
                    }
                }
            }

            if (path.isEmpty()) {
                AppToast(this).showToastMessage(
                        "파일 정보 가져오기 실패하였습니다. 다시 시도해주세요.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                )
            } else {
                try {
                    if (mime.toLowerCase().contains("gif")) {
                        file = File(path)
                        name = file.name
                        size = info["size"] as Int
                    } else if (mime.startsWith("image")) {
                        file = File.createTempFile("desk", ".jpg")
                        name = file.name
                        size = file.length().toInt()

                        getResizedBitmap(tempUri!!).apply {
                            this!!.compress(Bitmap.CompressFormat.JPEG, 80, BufferedOutputStream(FileOutputStream(file)))
                        }

                        val oriExif = ExifInterface(path)
                        val orientation = oriExif.getAttribute(ExifInterface.TAG_ORIENTATION)

                        ExifInterface(file.absolutePath).apply {
                            setAttribute(ExifInterface.TAG_ORIENTATION, orientation)
                            saveAttributes()
                        }
                    } else if (mime.startsWith("video")) {
                        file = File(path)
                        name = file.name
                        size = info["size"] as Int
                    } else {
                        file = File(path)
                        name = file.name
                        size = info["size"] as Int
                    }

                    // Specify two dimensions of thumbnails to generate
                    val thumbnailSizes: MutableList<ThumbnailSize> = mutableListOf()
                    thumbnailSizes.add(ThumbnailSize(240, 240))
                    thumbnailSizes.add(ThumbnailSize(320, 320))

                    // Send image with thumbnails in the specified dimensions
                    val tempMessage = channel!!.sendFileMessage(file, name, mime, size, "", null, thumbnailSizes, { fileMessage, exception ->
                        if (exception != null) {
                            messageAdapter?.markMessageFailed(fileMessage.requestId)
                            return@sendFileMessage
                        }

                        messageAdapter?.markMessageSent(fileMessage)
                    })

                    messageAdapter?.addTempFileMessageInfo(tempMessage, tempUri!!)
                    addTempMessage(tempMessage)
                } catch (e: Exception) {
                    Logger.p(e)
                }
            }
        }
    }

    /**
     * Sends a File Message containing an image file.
     * Also requests thumbnails to be generated in specified sizes.
     *
     * @param uri : The URI of the image, which in this case is received through an Intent request.
     * @param thumbnailSize
     */
    private fun sendImageWithThumbnail(uri: Uri?, thumbnailSize: List<ThumbnailSize>) {
        if (channel != null) {
            if(uri == null) return

            val info = getFileInfo(uri)

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

            if (path.isNotEmpty()) {
                // Send image with thumbnails in the specified dimensions
                val tempMessage = channel!!.sendFileMessage(file, name, mime, size, "", null, thumbnailSize, { fileMessage, sendBirdException ->
                    if (sendBirdException != null) {
                        Logger.d("Error code ${sendBirdException.code}, message ${sendBirdException.message}")
                        messageAdapter?.markMessageFailed(fileMessage.requestId)
                        return@sendFileMessage
                    }
                })

                messageAdapter?.addTempFileMessageInfo(tempMessage, uri)
                addTempMessage(tempMessage)
            }
        }
    }

    private fun addTempMessage(tempMessage: BaseMessage) {
        messageAdapter?.addMessage(tempMessage)
        messageAdapter?.notifyDataSetChanged()

        messageList.scrollToPosition(messageAdapter?.itemCount?.minus(1)!!)
    }

    private fun updateUserMessageWithUrl(message: BaseMessage, msg: String, url: String) {
        if (channel != null) {
            messageAdapter?.setUrlPrevTempMessage(message)

            DeskUserRichMessage.updateUserMessageWithUrl(channel!!, message, msg, url, object : DeskUserRichMessage.UpdateUserMessageWithUrlHandler {
                override fun onResult(userMessage: UserMessage?, e: SendBirdException?) {
                    messageAdapter?.setUrlPrevTempMessage(null)

                    if (e != null) {
                        messageAdapter?.notifyDataSetChanged()
                        return
                    }

                    messageAdapter?.replaceMessage(userMessage as BaseMessage)
                    messageAdapter?.notifyDataSetChanged()

                    channel!!.markAsRead()
                }
            })
        }
    }

    private fun refresh() {
        if (ticket != null) {
            ticket!!.refresh({ ticket, exception ->
                if (exception != null) {
                    Logger.p(exception)
                    return@refresh
                }

                doRefresh()
            })
        } else if (channelUrl.isNotEmpty()) {
            Ticket.getByChannelUrl(channelUrl, { ticket, exception ->
                if (exception != null) {
                    Logger.p(exception)
                    return@getByChannelUrl
                }

                this.ticket = ticket
                this.channel = ticket.channel
                this.channelUrl = ticket.channel.url

                doRefresh()
            })
        }
    }

    private fun doRefresh() {
        if (ticket != null) {
            layoutChatInput.apply {
                when {
                    DeskManager.isTicketClosed(ticket) == true -> visibility = View.GONE
                    else -> visibility = View.VISIBLE
                }
            }
        }

        if (channel != null) {
            channel!!.markAsRead()
        }

        loadPreviousMessage(true, object : LoadPreviousMessagesHandler {
            override fun onResult(e: SendBirdException?) {
                if (e != null) {
                    Logger.p(e)
                    return
                }
            }
        })
    }

    private fun requestMedia() {
        Intent().apply {
            // Show only images, no videos or anything else
            setType("image/*")
            setAction(Intent.ACTION_PICK)
        }.let {
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(it, "Select Image"), INTENT_REQUEST_CHOOSE_MEDIA)
        }
    }

    private fun requestCamera() {
        requestingCamera = true
        tempPhotoUri = getTempFileUri(true, false)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri)
        }

        val resInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(packageName, tempPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivityForResult(intent, INTENT_REQUEST_CAMERA)
    }

    private fun requestToUploadVideo() {
        Intent().apply {
            // Show only videos
            setType("video/*")
            setAction(Intent.ACTION_PICK)
        }.let {
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(it, "Select Video"), INTENT_REQUEST_TO_UPLOAD_VIDEO)
        }
    }

    private fun requestToRecordVideo() {
        requestingCamera = true
        tempVideoUri = getTempFileUri(false, false)

        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, tempVideoUri)
        }

        val resInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(packageName, tempPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivityForResult(intent, INTENT_REQUEST_TO_RECORD_VIDEO)
    }

    private fun previewPhoto(uri: Uri?) {
        if (uri != null) {
            tempPhotoPreviewUri = getTempFileUri(true, true)
            UCrop.of(uri, tempPhotoPreviewUri!!).start(this)
        }
    }

    private fun getTempFileUri(isPhoto: Boolean, doNotUseFileProvider: Boolean): Uri? {
        var uri: Uri? = null
        try {
            val tempFile: File
            tempFile = if (isPhoto) {
                val path = "${externalCacheDir!!.absolutePath}${Environment.DIRECTORY_PICTURES}"
                File.createTempFile("desk_" + System.currentTimeMillis(), ".jpg", File(path))
            } else {
                val path = "${externalCacheDir!!.absolutePath}${Environment.DIRECTORY_MOVIES}"
                File.createTempFile("desk_" + System.currentTimeMillis(), ".mp4", File(path))
            }

            if (Build.VERSION.SDK_INT >= 24 && !doNotUseFileProvider) {
                uri = FileProvider.getUriForFile(this, "com.sendbird.desk.android.provider", tempFile)
                tempFileUri = Uri.fromFile(tempFile)
            } else {
                uri = Uri.fromFile(tempFile)
                tempFileUri = uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return uri
    }

    private fun loadPreviousMessage(refresh: Boolean, handler: LoadPreviousMessagesHandler?) {
        if (refresh) {
            minMessageTimestamp = Long.MAX_VALUE
            loading = false
            hasPrev = true
        }

        if (loading || hasPrev.not()) return

        loading = true

        if (channel != null) {
            channel!!.getPreviousMessagesByTimestamp(
                    minMessageTimestamp,
                    false,
                    30,
                    false,
                    BaseChannel.MessageTypeFilter.ALL,
                    null,
                    { list, exception ->
                        loading = false

                        if (exception != null) {
                            AppToast(this@SBChatMessageActivity).showToastMessage(
                                    "메시지 불러오기를 실패 하였습니다. 다시 시도해주세요.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM
                            )

                            if (handler != null) {
                                handler.onResult(exception)
                            }

                            return@getPreviousMessagesByTimestamp
                        }

                        if (list.size == 0) {
                            hasPrev = false
                        }

                        if (refresh) {
                            for (message in messageAdapter?.getMessageList()!!) {
                                if (messageAdapter?.isTempMessage(message)!! || messageAdapter?.isFailedMessage(message)!!) {
                                    list.add(message)
                                }
                            }

                            messageAdapter?.clear()
                        }

                        var count = 0
                        for (i in list.indices.reversed()) {
                            val message = list[i]
                            if (DeskAdminMessage.isMessage(message)) {
                                continue
                            }

                            messageAdapter?.insertMessage(list[i])
                            count++
                        }

                        if (count > 0) {
                            minMessageTimestamp = list[0].createdAt
                            messageList.scrollToPosition(count - 1)
                        }

                        if (handler != null) {
                            handler.onResult(null)
                        }
                    })

        }
    }

    @Throws(IOException::class)
    private fun getResizedBitmap(uri: Uri): Bitmap? {
        var input: InputStream?
        input = contentResolver.openInputStream(uri)
        val onlyBoundsOptions = BitmapFactory.Options()
        onlyBoundsOptions.inJustDecodeBounds = true
        onlyBoundsOptions.inDither = true //optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
        input!!.close()

        if (onlyBoundsOptions.outWidth == -1 || onlyBoundsOptions.outHeight == -1) {
            return null
        }

        val originalSize = if (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) onlyBoundsOptions.outHeight else onlyBoundsOptions.outWidth
        val ratio = if (originalSize > 1280) (originalSize / 1280).toDouble() else 1.0

        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio)
        bitmapOptions.inDither = true //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
        input = contentResolver.openInputStream(uri)

        val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
        input!!.close()

        return bitmap
    }

    private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
        val k = Integer.highestOneBit(Math.floor(ratio).toInt())
        return if (k == 0) 1 else k
    }

    private fun retryFailedMessage(message: BaseMessage) {
        TwoBtnDialog(this).apply {
            setTitle("메시지 전송 실패")
            setMessage("메시지 전송이 실패하였습니다.\n다시 전송 하시겠습니까?")
            setConfirmButton("확인", View.OnClickListener {
                if (message is UserMessage) {
                    val userInput = message.message
                    sendUserMessage(userInput)
                } else if (message is FileMessage) {
                    val uri = messageAdapter?.getTempFileMessageUri(message)!!
                    sendFileMessage(uri, FILE_TYPE_ALL)
                }

                messageAdapter?.removeFailedMessage(message)
                dismiss()
            })
            setCancelButton("취소", View.OnClickListener {
                messageAdapter?.removeFailedMessage(message)
                dismiss()
            })
        }.show()
    }

    private fun showDownloadConfirmDialog(message: FileMessage) {
        OneBtnDialog(this).apply {
            setTitle("")
            setMessage("Download file?")
            setCancelable(true)
            setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                downloadFile(message.url, message.name)
            })
            show()
        }
    }

    private fun loadPetInfo() {
        lifecycleScope.launch {
            mPetInfoItems = async {
                val repository by inject<PetdocRepository>()
                val oldUserId = if (StorageUtils.loadValueFromPreference(this@SBChatMessageActivity, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                    StorageUtils.loadValueFromPreference(this@SBChatMessageActivity, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
                } else {
                    0
                }

                repository.retrieveMyPets(oldUserId)
            }.await()
        }
    }

    internal interface LoadPreviousMessagesHandler {
        fun onResult(e: SendBirdException?)
    }
}