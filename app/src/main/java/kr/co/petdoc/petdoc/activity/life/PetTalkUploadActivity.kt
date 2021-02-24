package kr.co.petdoc.petdoc.activity.life

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_pet_talk_upload.*
import kotlinx.android.synthetic.main.adapter_chat_photo_footer_item.view.*
import kotlinx.android.synthetic.main.adapter_chat_photo_item.view.*
import kotlinx.android.synthetic.main.pettalk_image_item.view.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetTalk
import kr.co.petdoc.petdoc.network.response.submodel.PetTalkDetailItem
import kr.co.petdoc.petdoc.utils.*
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: PetTalkUploadActivity
 * Created by kimjoonsung on 2020/06/04.
 *
 * Description :
 */
class PetTalkUploadActivity : BaseActivity() {

    private val TAG = "PetTalkUploadActivity"
    private val PET_TALK_UPLOAD_REQUEST = "${TAG}.petTalkUploadRequest"
    private val PET_TALK_DELETE_REQUEST = "${TAG}.petTalkDeleteRequest"
    private val PETTALK_DETAIL_REQUEST = "${TAG}.petTalkDetailRequest"

    private val RES_IMAGE = 100

    private lateinit var photoAdapter:PhotoAdapter
    private var photoItems:MutableList<String> = mutableListOf()

    private var queryImageUrl: String = ""
    private var imageUri: Uri? = null

    private var currentCategory = ""
    private var title = ""
    private var content = ""

    private var petTalkId = -1

    private var flagBox = booleanArrayOf(false, false)

    private lateinit var keyboardVisible: KeyboardVisibilityUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_talk_upload)

        petTalkId = intent?.getIntExtra("petTalkId", petTalkId)!!
        Logger.d("petTalkId : $petTalkId")

        btnBack.setOnSingleClickListener { onBackPressed() }
        btnDelete.setOnSingleClickListener {
            mApiClient.deletePetTalk(
                PET_TALK_DELETE_REQUEST,
                petTalkId
            )
        }

        btnComplete.setOnSingleClickListener {
            title = editTitle.text.toString().trim()
            content = editContent.text.toString().trim()
            if (currentCategory.isNotEmpty() && title.isNotEmpty() && content.isNotEmpty()) {
                mApiClient.modifyPetTalk(
                    PET_TALK_UPLOAD_REQUEST,
                    petTalkId,
                    title,
                    content,
                    currentCategory,
                    photoItems
                )
            } else {
                AppToast(this@PetTalkUploadActivity).showToastMessage(
                    "정보를 빠짐없이 입력해 주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }
        }

        btnUpload.setOnSingleClickListener {
            title = editTitle.text.toString().trim()
            content = editContent.text.toString().trim()
            if (currentCategory.isNotEmpty() && title.isNotEmpty() && content.isNotEmpty()) {
                mApiClient.uploadPetTalk(
                    PET_TALK_UPLOAD_REQUEST,
                    title,
                    content,
                    currentCategory,
                    photoItems
                )
            } else {
                AppToast(this@PetTalkUploadActivity).showToastMessage(
                    "정보를 빠짐없이 입력해 주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }
        }

        pettalk_tag_1_image.setOnSingleClickListener {
            currentCategory = "dog"
            updateCategoryToolbar()
        }
        pettalk_tag_2_image.setOnSingleClickListener {
            currentCategory = "cat"
            updateCategoryToolbar()
        }
        pettalk_tag_3_image.setOnSingleClickListener {
            currentCategory = "hamster"
            updateCategoryToolbar()
        }
        pettalk_tag_4_image.setOnSingleClickListener {
            currentCategory = "hedgehog"
            updateCategoryToolbar()
        }
        pettalk_tag_5_image.setOnSingleClickListener {
            currentCategory = "etc"
            updateCategoryToolbar()
        }
        pettalk_tag_6_image.setOnSingleClickListener {
            currentCategory = "flea"
            updateCategoryToolbar()
        }

        //==========================================================================================
        photoAdapter = PhotoAdapter()
        photoList.apply {
            layoutManager = LinearLayoutManager(this@PetTalkUploadActivity).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }

            adapter = photoAdapter
        }

        editTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    flagBox[0] = true
                } else {
                    flagBox[0] = false
                }

                checkBtnStatus()
            }
        })

        editContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    flagBox[1] = true
                } else {
                    flagBox[1] = false
                }

                checkBtnStatus()
            }
        })

        //==========================================================================================
        keyboardVisible = KeyboardVisibilityUtils(window,
            onShowKeyboard = { keyboardHeight ->
                Logger.d("keyboradHeight : $keyboardHeight")
                scrollView.run {
                    smoothScrollTo(scrollX, scrollY + keyboardHeight)
                }
        })

        //==========================================================================================
        if (petTalkId != -1) {
            layoutEditBtn.visibility = View.VISIBLE
            btnUpload.visibility = View.GONE

            mApiClient.getPetTalkDetail(PETTALK_DETAIL_REQUEST, petTalkId)
        } else {
            layoutEditBtn.visibility = View.GONE
            btnUpload.apply {
                isEnabled = false
                visibility = View.VISIBLE
            }

            updateCategoryToolbar()
        }

        // 카메라 및 파일 접근 권한 추가
        Helper.permissionCheck(this,
                arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE),
                object : PermissionCallback {
                    override fun callback(status: PermissionStatus) {
                        when(status){
                            PermissionStatus.ALL_GRANTED -> {}
                            PermissionStatus.DENIED_SOME -> {
                                //finish()
                            }
                        }
                    }
                },true )
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        keyboardVisible.detachKeyboardListeners()
        super.onDestroy()
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

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    /**
     * Response of request.
     *
     * @param data response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: NetworkBusResponse) {
        when (data.tag) {
            PET_TALK_UPLOAD_REQUEST -> {
                if ("ok" == data.status) {
                    if (petTalkId == -1) {
                        AppToast(this).showToastMessage(
                            "게시글이 등록 되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    } else {
                        AppToast(this).showToastMessage(
                            "게시글이 수정 되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }

                    finish()
                } else {
                    Logger.d("code : ${data.code}, response : ${data.response}")
                }
            }

            PET_TALK_DELETE_REQUEST -> {
                if ("ok" == data.status) {
                    AppToast(this).showToastMessage(
                        "게시글이 삭제 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Logger.d("code : ${data.code}, response : ${data.response}")
                }
            }

            PETTALK_DETAIL_REQUEST -> {
                if ("ok" == data.status) {
                    val petTalkDetailItem = Gson().fromJson(data.response, PetTalkDetailItem::class.java)
                    setPetTalkContent(petTalkDetailItem.petTalk)

                    for (i in 0 until petTalkDetailItem.petTalkImageList.size) {
                        photoItems.add(petTalkDetailItem.petTalkImageList[0].image)
                    }

                    if (photoItems.size > 0) {
                        photoAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun setPetTalkContent(item: PetTalk) {
        editTitle.setText(item.title)
        editContent.setText(item.content)

        currentCategory = item.type
        updateCategoryToolbar()
    }

    private fun onPhotoAdd() {
        if (checkPermission()) {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ), RES_IMAGE
            )
        }
    }

    private fun onPhotoDelete(position: Int) {
        photoItems.removeAt(position)
        photoAdapter.notifyDataSetChanged()
    }

    private fun handleImageRequest(data: Intent?) {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
            Toast.makeText(
                this@PetTalkUploadActivity,
                t.localizedMessage ?: "Some error occured, please try again later",
                Toast.LENGTH_SHORT
            ).show()
        }

        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            if (data?.data != null) {     //Photo from gallery
                imageUri = data.data
                queryImageUrl = imageUri?.path!!
                queryImageUrl = compressImageFile(queryImageUrl, false, imageUri!!)
            }

            // /data/user/0/kr.co.petdoc.petdoc/files/Images/IMG_20200522035311.png
            Logger.d("queryImageUrl : $queryImageUrl")
            if (queryImageUrl.isNotEmpty()) {
                if (photoAdapter.itemCount < 7) {
                    photoItems.add(queryImageUrl)
                    photoAdapter.notifyDataSetChanged()
                } else {
                    AppToast(this@PetTalkUploadActivity).showToastMessage(
                        "사진은 6장까지만 추가 할 수 있습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
            }
        }
    }

    private fun updateCategoryToolbar(){
        val darkGrey = Helper.readColorRes(R.color.dark_grey)

        pettalk_tag_1_image.setImageResource(R.drawable.pettalk_dog)
        pettalk_tag_1_text.setTextColor(darkGrey)
        pettalk_tag_2_image.setImageResource(R.drawable.pettalk_cat)
        pettalk_tag_2_text.setTextColor(darkGrey)
        pettalk_tag_3_image.setImageResource(R.drawable.pettalk_hamster)
        pettalk_tag_3_text.setTextColor(darkGrey)
        pettalk_tag_4_image.setImageResource(R.drawable.pettalk_hedgehog)
        pettalk_tag_4_text.setTextColor(darkGrey)
        pettalk_tag_5_image.setImageResource(R.drawable.pettalk_special)
        pettalk_tag_5_text.setTextColor(darkGrey)
        pettalk_tag_6_image.setImageResource(R.drawable.pettalk_market)
        pettalk_tag_6_text.setTextColor(darkGrey)

        when(currentCategory){
            "dog" -> {
                pettalk_tag_1_image.setImageResource(R.drawable.pettalk_dog_on)
                pettalk_tag_1_text.setTextColor(Helper.readColorRes(R.color.dog))
            }
            "cat" -> {
                pettalk_tag_2_image.setImageResource(R.drawable.pettalk_cat_on)
                pettalk_tag_2_text.setTextColor(Helper.readColorRes(R.color.cat))
            }
            "hamster" -> {
                pettalk_tag_3_image.setImageResource(R.drawable.pettalk_hamster_on)
                pettalk_tag_3_text.setTextColor(Helper.readColorRes(R.color.hasmter))
            }
            "hedgehog" -> {
                pettalk_tag_4_image.setImageResource(R.drawable.pettalk_hedgehog_on)
                pettalk_tag_4_text.setTextColor(Helper.readColorRes(R.color.hedgehog))
            }
            "etc" -> {
                pettalk_tag_5_image.setImageResource(R.drawable.pettalk_special_on)
                pettalk_tag_5_text.setTextColor(Helper.readColorRes(R.color.special))
            }
            "flea" -> {
                pettalk_tag_6_image.setImageResource(R.drawable.pettalk_market_on)
                pettalk_tag_6_text.setTextColor(Helper.readColorRes(R.color.market))
            }
        }
    }

    private fun checkBtnStatus() {
        if (flagBox[0] && flagBox[1]) {
            btnUpload.apply {
                setTextColor(Helper.readColorRes(R.color.white))
                isEnabled = true
            }
        } else {
            btnUpload.apply {
                setTextColor(Helper.readColorRes(R.color.white_alpha))
                isEnabled = false
            }
        }
    }

    private fun checkPermission(): Boolean {
        val isGranted = isPermissionsGranted(
            this,
            arrayOf(
                PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE,
                PermissionUtils.Manifest_READ_EXTERNAL_STORAGE,
                PermissionUtils.Manifest_CAMERA
            )
        )

        if (!isGranted) {
            askCompactPermissions(arrayOf(
                PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE,
                PermissionUtils.Manifest_READ_EXTERNAL_STORAGE,
                PermissionUtils.Manifest_CAMERA
            ),
                object : PermissionResult {
                    override fun permissionGranted() {
                        onPhotoAdd()
                    }

                    override fun permissionDenied() {
                        AppToast(this@PetTalkUploadActivity).showToastMessage(
                            "프로필 사진을 수정하려면 권한이 필요합니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }

                    override fun permissionForeverDenied() {
                        AppToast(this@PetTalkUploadActivity).showToastMessage(
                            "프로필 사진을 수정하려면 권한이 필요합니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                })
        }
        return isGranted
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

                    holder.itemView.photoDel.setOnClickListener {
                        onPhotoDelete(position)
                    }
                }

                else -> {
                    holder.itemView.btnPhotoAdd.setOnClickListener { onPhotoAdd() }
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
            if (petTalkId != -1) {
                if(_url.startsWith("/data")) {
                    Glide.with(this@PetTalkUploadActivity)
                            .asBitmap()
                            .load(_url)
                            .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                            .into(item.chatPhoto)
                } else {
                    Glide.with(this@PetTalkUploadActivity)
                            .load( if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                            .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                            .into(item.chatPhoto)
                }
            } else {
                Glide.with(this@PetTalkUploadActivity)
                    .asBitmap()
                    .load(_url)
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                    .into(item.chatPhoto)
            }
        }
    }

    inner class PhotoFooterHolder(view: View) : RecyclerView.ViewHolder(view)
}