package kr.co.petdoc.petdoc.fragment.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_chat_photo_footer_item.view.*
import kotlinx.android.synthetic.main.adapter_chat_photo_item.view.*
import kotlinx.android.synthetic.main.fragment_request_chat.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.KeyboardVisibilityUtils
import kr.co.petdoc.petdoc.utils.compressImageFile
import kr.co.petdoc.petdoc.viewmodel.ChatDataModel
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: ChatRequestFragment
 * Created by kimjoonsung on 2020/05/21.
 *
 * Description :
 */
class ChatRequestFragment : BaseFragment() {

    private val LOG_TAG = "ChatRequestFragment"
    private val CHAT_NO_PICTURE_REQUEST = "${LOG_TAG}.chatNoPictureRequest"
    private val CHAT_ADD_IMAGE_REQUEST = "${LOG_TAG}.chatMultipartsRequest"

    private val RES_IMAGE = 100

    private lateinit var dataModel: ChatDataModel
    private lateinit var photoAdapter:PhotoAdapter
    private var photoItems:MutableList<String> = mutableListOf()

    private var queryImageUrl: String = ""
    private var imageUri: Uri? = null

    private var isWarningOpen = false
    private var warningCheckValid = booleanArrayOf(false, false, false, false, false)

    private var chatPk = -1
    private var petId = -1
    private var imageIndex = 0

    private lateinit var keyboardVisible: KeyboardVisibilityUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(ChatDataModel::class.java)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_request_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutWarning.setOnSingleClickListener {
            if (!isWarningOpen) {
                layoutWarningDetail.visibility = View.VISIBLE
                arrow.setBackgroundResource(R.drawable.ic_arrow_close)
                isWarningOpen = true
            } else {
                layoutWarningDetail.visibility = View.GONE
                arrow.setBackgroundResource(R.drawable.ic_arrow_open)
                isWarningOpen = false
            }
        }

        btnComplete.setOnSingleClickListener {
            if (warningCheckValid[0] && warningCheckValid[1] && warningCheckValid[2] && warningCheckValid[3] && editChatContent.text.toString().isNotEmpty()) {
                Airbridge.trackEvent("counsel", "click", "counsel_done", null, null, null)
                FirebaseAPI(requireActivity()).logEventFirebase("상담_신청완료", "Click Event", "상담신청 등록완료 클릭")
                mApiClient.postChatRoomsNoPicture(CHAT_NO_PICTURE_REQUEST, petId, editChatContent.text.toString(), if(checkAgreee5.isSelected) "true" else "false")
            } else {
                AppToast(requireContext()).showToastMessage(
                    "필수 주의사항을 확인 바랍니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
            }
        }

        btnClose.setOnSingleClickListener { requireActivity().onBackPressed() }

        checkAllAgree.setOnClickListener(clickListener)
        checkAgreee1.setOnClickListener(clickListener)
        checkAgreee2.setOnClickListener(clickListener)
        checkAgreee3.setOnClickListener(clickListener)
        checkAgreee4.setOnClickListener(clickListener)
        checkAgreee5.setOnClickListener(clickListener)

        //=========================================================================================
        photoAdapter = PhotoAdapter()
        photoList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }

            adapter = photoAdapter
        }

        editChatContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnComplete.apply {
                    if (s?.isNotBlank() == true) {
                        setTextColor(Helper.readColorRes(R.color.white))
                        btnComplete.isEnabled = true
                    } else {
                        setTextColor(Helper.readColorRes(R.color.white_alpha))
                        btnComplete.isEnabled = false
                    }
                }
            }
        })

        //==========================================================================================
        keyboardVisible = KeyboardVisibilityUtils(requireActivity().window,
                onShowKeyboard = { keyboardHeight ->
                    Logger.d("keyboradHeight : $keyboardHeight")
                    scrollView.run {
                        smoothScrollTo(scrollX, scrollY + keyboardHeight)
                    }
                })

        //==========================================================================================
        arrow.setBackgroundResource(R.drawable.ic_arrow_open)

        petId = if (dataModel.petInfo.value?.id != null) {
            dataModel.petInfo.value?.id!!
        } else {
            -1
        }

        btnComplete.isEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RES_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == RES_IMAGE) {
                        handleImageRequest(data)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(CHAT_NO_PICTURE_REQUEST)) {
            mApiClient.cancelRequest(CHAT_NO_PICTURE_REQUEST)
        }

        if (mApiClient.isRequestRunning(CHAT_ADD_IMAGE_REQUEST)) {
            mApiClient.cancelRequest(CHAT_ADD_IMAGE_REQUEST)
        }

        keyboardVisible.detachKeyboardListeners()
        super.onDestroyView()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Chat Request Response
     *
     * @param response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when(event.tag) {
            CHAT_NO_PICTURE_REQUEST -> {
                if (event.status == "ok") {
                    chatPk = JSONObject(event.response)["pk"] as Int

                    if (photoItems.size == 0) {
                        OneBtnDialog(requireContext()).apply {
                            setTitle(Helper.readStringRes(R.string.chat_request_complete_title))
                            setMessage(Helper.readStringRes(R.string.chat_request_complete_desc))
                            setCanceledOnTouchOutside(false)
                            setCancelable(false)
                            setConfirmButton(
                                Helper.readStringRes(R.string.confirm),
                                View.OnClickListener {
                                    dismiss()
                                    requireActivity().finish()
                                })
                        }.show()
                    } else {
                        mApiClient.postChatAddImage(CHAT_ADD_IMAGE_REQUEST, chatPk, photoItems[imageIndex])
                    }
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }

            CHAT_ADD_IMAGE_REQUEST -> {
                if ("ok" == event.status) {
                    imageIndex = imageIndex.inc()
                    if(photoItems.size != imageIndex) {
                        mApiClient.postChatAddImage(CHAT_ADD_IMAGE_REQUEST, chatPk, photoItems[imageIndex])
                    } else {
                        imageIndex = 0

                        OneBtnDialog(requireContext()).apply {
                            setTitle(Helper.readStringRes(R.string.chat_request_complete_title))
                            setMessage(Helper.readStringRes(R.string.chat_request_complete_desc))
                            setCanceledOnTouchOutside(false)
                            setCancelable(false)
                            setConfirmButton(
                                Helper.readStringRes(R.string.confirm),
                                View.OnClickListener {
                                    dismiss()
                                    requireActivity().finish()
                                })
                        }.show()
                    }
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }
        }
    }

    /**
     * EventBus listener. An API call failed. No error message was returned.
     *
     * @param event ApiErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorEvent) {
        when (event.requestTag) {
            CHAT_NO_PICTURE_REQUEST -> {
                AppToast(requireContext()).showToastMessage(
                    "상담 신청이 완료되지 않았습니다. 다시 시도해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }

            CHAT_ADD_IMAGE_REQUEST -> {
                AppToast(requireContext()).showToastMessage(
                    "상담 이미지가 등록 되지 않았습니다. 다시 시도해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
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
        Logger.d("error : ${event.resultMsgUser}")
    }

    private fun warningCheckAll(status: Boolean) {
        if (status) {
            checkAgreee1.isSelected = true
            checkAgreee2.isSelected = true
            checkAgreee3.isSelected = true
            checkAgreee4.isSelected = true
            checkAgreee5.isSelected = true

            warningCheckValid[0] = true
            warningCheckValid[1] = true
            warningCheckValid[2] = true
            warningCheckValid[3] = true
            warningCheckValid[4] = true
        } else {
            checkAgreee1.isSelected = false
            checkAgreee2.isSelected = false
            checkAgreee3.isSelected = false
            checkAgreee4.isSelected = false
            checkAgreee5.isSelected = false

            warningCheckValid[0] = false
            warningCheckValid[1] = false
            warningCheckValid[2] = false
            warningCheckValid[3] = false
            warningCheckValid[4] = false
        }
    }

    private fun agreeCheck() {
        if (warningCheckValid[0] && warningCheckValid[1] && warningCheckValid[2] && warningCheckValid[3] && warningCheckValid[4]) {
            checkAllAgree.isSelected = true
        } else {
            checkAllAgree.isSelected = false
        }
    }

    private fun onPhotoAdd() {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RES_IMAGE)
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
                if (photoAdapter.itemCount < 7) {
                    photoItems.add(queryImageUrl)
                    photoAdapter.notifyDataSetChanged()
                } else {
                    AppToast(requireContext()).showToastMessage(
                        "사진은 6장까지만 추가 할 수 있습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
            }
        }
    }

    private fun onPhotoDelete(position: Int) {
        photoItems.removeAt(position)
        photoAdapter.notifyDataSetChanged()
    }

    //==============================================================================================
    private val clickListener = View.OnClickListener {
        when(it?.id) {
            R.id.checkAllAgree -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckAll(true)
                } else {
                    warningCheckAll(false)
                }
            }

            R.id.checkAgreee1 -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckValid[0] = true
                } else {
                    warningCheckValid[0] = false
                }

                agreeCheck()
            }

            R.id.checkAgreee2 -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckValid[1] = true
                } else {
                    warningCheckValid[1] = false
                }

                agreeCheck()
            }

            R.id.checkAgreee3 -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckValid[2] = true
                } else {
                    warningCheckValid[2] = false
                }

                agreeCheck()
            }

            R.id.checkAgreee4 -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckValid[3] = true
                } else {
                    warningCheckValid[3] = false
                }

                agreeCheck()
            }

            R.id.checkAgreee5 -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckValid[4] = true
                } else {
                    warningCheckValid[4] = false
                }

                agreeCheck()
            }
        }
    }

    //==============================================================================================
    inner class PhotoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_ITEM -> PhotoHolder(layoutInflater.inflate(R.layout.adapter_chat_photo_item, parent, false))
                else -> PhotoFooterHolder(layoutInflater.inflate(R.layout.adapter_chat_photo_footer_item, parent, false))
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                TYPE_ITEM -> {
                    (holder as PhotoHolder).setImage(photoItems[position])

                    holder.itemView.photoDel.setOnSingleClickListener {
                        onPhotoDelete(position)
                    }
                }

                else -> {
                    holder.itemView.btnPhotoAdd.setOnSingleClickListener { onPhotoAdd() }
                }
            }
        }

        override fun getItemCount() =
            if (photoItems.size == 0) {
                1
            } else {
                photoItems.size + 1
            }

        override fun getItemViewType(position: Int): Int {
            if (photoItems.size == 0) {
                return TYPE_FOOTER
            } else {
                if (position == photoItems.size) {
                    return TYPE_FOOTER
                } else {
                    return TYPE_ITEM
                }
            }
        }
    }

    inner class PhotoHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(_url:String) {
            Glide.with(requireContext())
                .asBitmap()
                .load(_url)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(item.chatPhoto)
        }
    }

    inner class PhotoFooterHolder(view:View) : RecyclerView.ViewHolder(view)
}