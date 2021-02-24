package kr.co.petdoc.petdoc.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_upload.*
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.utils.PermissionResult
import kr.co.petdoc.petdoc.utils.PermissionUtils
import kr.co.petdoc.petdoc.utils.image.CreateTempImagesFinishedEvent
import kr.co.petdoc.petdoc.utils.image.CreateTempImagesTask
import kr.co.petdoc.petdoc.utils.image.ImageUtil
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * petdoc-android
 * Class: UploadFragment
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : Retrofit EventBus Test Code
 */
class UploadFragment : BaseFragment() {

    companion object {
        fun newInstance() = UploadFragment()
    }

    private val LOG = "UploadFragment"
    private val UPLOAD_FILE_REQUEST_TAG = "$LOG.uploadFileRequest"
    private val chooseFromGallery = "Gallery"
    private val chooseFromCamera = "Camera"
    private val chooseImageTitle = "Select Image From"

    private val CAMERA_PIC_CODE = 1002
    private val GALLERY_PIC_CODE = 1003

    private var selectedImagePath: String = ""

    private val minImageWidth = 256
    private val minImageHeight = 256

    private var cameraPicFilePath = ""
    protected lateinit var imageHelper: ImageUtil

    private val clickListener = View.OnClickListener {
        when(it?.id) {
            R.id.imgUpload -> showImageChooserDialog()
            R.id.btnUpload -> uploadImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageHelper = ImageUtil(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_upload, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog(requireContext())

        imgUpload.setOnClickListener(clickListener)
        btnUpload.setOnClickListener(clickListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                CAMERA_PIC_CODE     -> handleCameraPicResult(resultCode)
                GALLERY_PIC_CODE    -> handleGalleryPicResult(resultCode, data)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event:CreateTempImagesFinishedEvent) {
        dismissProgress()
        selectedImagePath = event.bitmapPaths[0]
        Glide.with(requireContext())
            .load(selectedImagePath)
            .apply(RequestOptions.centerCropTransform().placeholder(R.mipmap.ic_launcher))
            .into(imgUpload)
    }

    /**
     * Response of Uploaded File
     *
     * @param apiResponse UploadFileResponse
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when(response.requestTag) {
            UPLOAD_FILE_REQUEST_TAG -> {
                dismissProgress()
                showToast(response.message)
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
        when(event.requestTag) {
            UPLOAD_FILE_REQUEST_TAG -> {
                dismissProgress()
                showToast(getString(R.string.error_server_problem))
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
            UPLOAD_FILE_REQUEST_TAG -> {
                dismissProgress()
                showToast(event.resultMsgUser)
            }
        }
    }

    private fun uploadImage() {
        if(selectedImagePath.isEmpty()) {
            showToast(getString(R.string.str_plz_select_img))
        } else {
            val file = File(selectedImagePath)
            if(!file.exists()) {
                showToast(getString(R.string.str_plz_select_img))
                return
            }

            if(!mApiClient.isRequestRunning(UPLOAD_FILE_REQUEST_TAG)) {
                showProgress()
                mApiClient.uploadImage(UPLOAD_FILE_REQUEST_TAG, selectedImagePath)
            }
        }
    }

    private fun showImageChooserDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(chooseImageTitle)
            .setPositiveButton(chooseFromCamera, object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    startCamera()
                }
            })
            .setNegativeButton(chooseFromGallery, object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    openGallery()
                }
            })
            .show()
    }

    private fun startCamera() {
        if(updatePermission()) {
            val file = imageHelper.outputMediaFile

            if(file == null) {
                showToast(getString(R.string.str_file_not_found))
                return
            }

            cameraPicFilePath = file.path
            val photoURI = FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.provider", file)
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }, CAMERA_PIC_CODE)
        }
    }

    private fun openGallery() {
        startActivityForResult(Intent(Intent.ACTION_PICK).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
                .setAction(Intent.ACTION_GET_CONTENT)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                .setType("image/*")
        }, GALLERY_PIC_CODE)
    }

    private fun handleGalleryPicResult(resultCode: Int, resultData: Intent?) {
        if(resultCode == Activity.RESULT_OK && resultData != null) {
            showProgress()

            if(resultData.data != null) {
                addImages(resultData.data!!)
            } else {
                if(resultData.clipData != null
                    && resultData.clipData!!.itemCount > 0) {
                    for (i in 0 until resultData.clipData!!.itemCount) {
                        addImages(resultData.clipData!!.getItemAt(i).uri)
                    }
                }
            }
        } else if(resultCode != Activity.RESULT_CANCELED) {
            showToast(getString(R.string.error_unknown))
        }
    }

    private fun handleCameraPicResult(resultCode: Int) {
        if(resultCode == Activity.RESULT_OK) {
            try {
                val isValid = imageHelper.isPictureValidForUpload(cameraPicFilePath)
                if(isValid) {
                    // Send a broadcast to the MediaScanner to make the file show up in the gallery.
                    val uri = Uri.fromFile(File(cameraPicFilePath))
                    requireContext().sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                    addImages(uri)
                } else {
                    showToast(getString(R.string.error_invalid_image))
                }
            } catch (e: IOException) {
                showToast(getString(R.string.error_image_processing))
            }
        } else if(resultCode != Activity.RESULT_CANCELED) {
            showToast(getString(R.string.error_unknown))
        }
    }

    private fun addImages(uri: Uri) {
        try {
            val validateImages = imageHelper.isPictureValidForUpload(uri)
            val event = CreateTempImagesFinishedEvent()
            val uris:MutableList<Uri> = mutableListOf()
            uris.add(uri)

            CreateTempImagesTask(requireContext(), uris, event, validateImages, minImageWidth, minImageHeight).apply {
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            dismissProgress()
        }
    }

    private fun updatePermission():Boolean {
        val isGranted = isPermissionsGranted(
            requireContext(),
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
                        startCamera()
                    }

                    override fun permissionDenied() {
                        showSnackBar(
                            imgUpload.rootView,
                            getString(R.string.str_allow_permission_for_image)
                        )
                    }

                    override fun permissionForeverDenied() {
                        showSnackBar(
                            imgUpload.rootView,
                            getString(R.string.str_allow_permission_from_setting)
                        )
                    }
                })
        }
        return isGranted
    }
}