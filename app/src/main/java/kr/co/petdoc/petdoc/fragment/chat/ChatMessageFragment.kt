package kr.co.petdoc.petdoc.fragment.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_message.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.chat.MyChatAdapter
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatDetailResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.Message
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.KeyboardHeightProvider
import kr.co.petdoc.petdoc.utils.compressImageFile
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ChatMessageFragment
 * Created by kimjoonsung on 2020/05/21.
 *
 * Description :
 */
class ChatMessageFragment : BaseFragment() {

    private val RES_IMAGE = 100

    private val LOGTAG = "ChatMessageFragment"
    private val CHAT_MESSAGE_LIST_REQUEST = "${LOGTAG}.chatMessageListRequest"
    private val CHAT_ADD_MESSAGE_REQUEST = "${LOGTAG}.chatAddMessageRequest"
    private val CHAT_ADD_IMAGE_REQUEST = "${LOGTAG}.chatAddImageRequest"
    private val CHAT_QUIT_REQUEST = "${LOGTAG}.chatQuitRequest"

    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null
    private lateinit var messageAdapter:MyChatAdapter

    private var queryImageUrl: String = ""
    private var imageUri: Uri? = null

    private var chatPk = -1
    private var status = ""
    private var doctorName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_chat_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        FirebaseAPI(requireActivity()).logEventFirebase("상담_상담내역_상세", "Page View", "상담내역 상세페이지뷰")

        btnClose.setOnClickListener { requireActivity().finish() }
        btnPhoto.setOnClickListener { photoAdd() }
        btnSendMsg.setOnClickListener { sendMsg() }
        btnWarningExpand.setOnClickListener {
            textViewGuide.apply {
                when {
                    visibility == View.VISIBLE -> {
                        visibility = View.GONE
                        btnWarningExpand.setBackgroundResource(R.drawable.ic_arrow_open)
                    }

                    else -> {
                        visibility = View.VISIBLE
                        btnWarningExpand.setBackgroundResource(R.drawable.ic_arrow_close)
                    }
                }
            }
        }

        //==========================================================================================
        chatPk = arguments?.getInt("chatPk")?:chatPk
        status = arguments?.getString("status") ?: status
        doctorName = arguments?.getString("name") ?: doctorName
        Logger.d("chatPk : $chatPk, status : $status, doctorName : $doctorName")

        editTextChat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    btnSendMsg.isEnabled = true
                    btnSendMsg.setBackgroundResource(R.drawable.ic_send_enable)
                } else {
                    btnSendMsg.isEnabled = false
                    btnSendMsg.setBackgroundResource(R.drawable.ic_send_disable)
                }
            }
        })

        btnWarningExpand.setBackgroundResource(R.drawable.ic_arrow_close)

        messageAdapter = MyChatAdapter(requireContext())
        messageList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messageAdapter
        }

        mApiClient.getChatDetail(CHAT_MESSAGE_LIST_REQUEST, chatPk)
        if (status == "should_end") {
            TwoBtnDialog(requireContext()).apply {
                setTitle("${doctorName}원장님이 상담종료를\n요청하셨습니다.")
                setMessage("상담을 계속하고 싶다면 추가상담을\n그만한다면 상담종료를 눌러주세요!")
                setConfirmButton("상담종료", View.OnClickListener {
                    dismiss()
                    mApiClient.putChatRoomQuit(CHAT_QUIT_REQUEST, chatPk)
                })
                setCancelButton("추가상담", View.OnClickListener {
                    dismiss()
                })
            }.show()
        } else {
            // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
            mKeyboardHeightProvider = KeyboardHeightProvider(requireActivity())
            Handler().postDelayed( { mKeyboardHeightProvider!!.start() }, 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            RES_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == RES_IMAGE) {
                        handleImageRequest(intent)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider!!.close()
        }

        if (mApiClient.isRequestRunning(CHAT_MESSAGE_LIST_REQUEST)) {
            mApiClient.cancelRequest(CHAT_MESSAGE_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(CHAT_ADD_MESSAGE_REQUEST)) {
            mApiClient.cancelRequest(CHAT_ADD_MESSAGE_REQUEST)
        }

        if (mApiClient.isRequestRunning(CHAT_ADD_IMAGE_REQUEST)) {
            mApiClient.cancelRequest(CHAT_ADD_IMAGE_REQUEST)
        }

        if (mApiClient.isRequestRunning(CHAT_QUIT_REQUEST)) {
            mApiClient.cancelRequest(CHAT_QUIT_REQUEST)
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
        when(response.requestTag) {
            CHAT_MESSAGE_LIST_REQUEST -> {
                if (response is ChatDetailResponse) {
                    messageAdapter.setItems(response.chatDetail.messages.reversed())
                    messageList.smoothScrollToPosition(messageAdapter.itemCount)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            CHAT_ADD_MESSAGE_REQUEST -> {
                if (event.status == "ok") {
                    val item = Message(
                        getCurrentTime(),
                        0,
                        "",
                        editTextChat.text.toString(),
                        null
                    )

                    messageAdapter.addItems(item)
                    messageList.smoothScrollToPosition(messageAdapter.itemCount)

                    editTextChat.setText("")
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }

            CHAT_ADD_IMAGE_REQUEST -> {
                if (event.status == "ok") {
                    val item = Message(
                        getCurrentTime(),
                        0,
                        queryImageUrl,
                        "",
                        null
                    )

                    messageAdapter.addItems(item)
                    messageList.smoothScrollToPosition(messageAdapter.itemCount)

                    queryImageUrl = ""
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }

            CHAT_QUIT_REQUEST -> {
                if ("ok" == event.status) {
                    AppToast(requireContext()).showToastMessage(
                        "정상적으로 종료 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    requireActivity().finish()
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }
        }
    }

    /**
     * EventBus listener. An API call failed. An error message was returned.
     *
     * @param event ApiErrorWithMessageEvent Contains the error message.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorWithMessageEvent) {
        when(event.requestTag) {
            CHAT_MESSAGE_LIST_REQUEST -> { Logger.d("message : ${event.resultMsgUser}") }
        }
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

    //==============================================================================================

    private fun sendMsg() {
        Logger.d("send messge : ${editTextChat.text.toString()}")
        mApiClient.postChatAddMessage(
            CHAT_ADD_MESSAGE_REQUEST,
            chatPk,
            editTextChat.text.toString()
        )

        hideKeyboard(editTextChat)
    }

    private fun photoAdd() {
        startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ), RES_IMAGE
        )
    }

    private fun handleImageRequest(data: Intent?) {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
            Toast.makeText(
                requireContext(),
                t.localizedMessage ?: "Some error occured, please try again later",
                Toast.LENGTH_SHORT
            ).show()
        }

        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            if (data?.data != null) {     //Photo from gallery
                imageUri = data.data
                queryImageUrl = imageUri?.path!!
                queryImageUrl = requireActivity().compressImageFile(queryImageUrl, false, imageUri!!)
            }

            // /data/user/0/kr.co.petdoc.petdoc/files/Images/IMG_20200522035311.png
            Logger.d("queryImageUrl : $queryImageUrl")
            if (queryImageUrl.isNotEmpty()) {
                mApiClient.postChatAddImage(CHAT_ADD_IMAGE_REQUEST, chatPk, queryImageUrl)
            }
        }
    }

    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

        return sdf.format(date)
    }
}