package kr.co.petdoc.petdoc.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.soundcloud.android.crop.Crop
import kotlinx.android.synthetic.main.dialog_image_pick.*
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper

import kr.co.petdoc.petdoc.viewmodel.MyPageInformationModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ImagePickDialogFragment
 * Created by kimjoonsung on 2020/04/07.
 *
 * Description : 사진 선택 시 사용하는 다이얼로그
 */
class ImagePickDialogFragment : DialogFragment() {

    companion object {
        private const val PICK_FROM_CAMERA = 0
        private const val PICK_FROM_GALLERY = 1

        fun getInstance() = ImagePickDialogFragment()
    }

    private var mListener:OnPickCompleteListener? = null
    private var profileImage:File? = null

    interface OnPickCompleteListener {
        fun onPickCompleted(profileImage:File)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return inflater.inflate(R.layout.dialog_image_pick, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonCamera.setOnClickListener { getImageFromCamera() }
        buttonGallery.setOnClickListener { getImageFromAlbum() }
        buttonCancel.setOnClickListener { dismiss() }

        //sungmin change permssion code routine  2020/04/08
        Helper.permissionCheck(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
            null,
            true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            AppToast(requireActivity()).showToastMessage(
                "취소 되었습니다.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM)

            if (profileImage != null) {
                if (profileImage!!.exists()) {
                    if (profileImage!!.delete()) {
                        Logger.d("${profileImage!!.absolutePath} 삭제 성공")
                    }
                }
            }

            return
        }

        if (requestCode == PICK_FROM_CAMERA) {
            if (profileImage != null) {
                val fileUri = FileProvider.getUriForFile(requireActivity(), "${BuildConfig.APPLICATION_ID}.fileProvider", profileImage!!)
                cropImage(fileUri)
            }
        } else if (requestCode == PICK_FROM_GALLERY && intent != null) {
            val photoUri = intent.data
            Logger.d("photUri : $photoUri")
            if (photoUri != null) {
                cropImage(photoUri)
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            Logger.d("profileImage : $profileImage")
            if (profileImage != null) {
                mListener?.onPickCompleted(profileImage!!)
                dismiss()
            }
        }
    }

    fun setListener(listener: OnPickCompleteListener) {
        this.mListener = listener
    }

    /**
     * 갤러리에서 사진을 가져온다.
     */
    private fun getImageFromAlbum() {
        startActivityForResult(Intent(Intent.ACTION_PICK).apply {
            type = MediaStore.Images.Media.CONTENT_TYPE
        }, PICK_FROM_GALLERY)
    }

    /**
     * 사진 촬영한다..
     */
    private fun getImageFromCamera() {
        try {
            profileImage = createImageFile()
        } catch (e: IOException) {
            AppToast(requireActivity()).showToastMessage(
                "이미지 처리 오류! 다시 시도해주세요.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )
            requireActivity().finish()

            Logger.p(e)
        }

        val fileUri = FileProvider.getUriForFile(requireActivity(), "${BuildConfig.APPLICATION_ID}.fileProvider", profileImage!!)
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        }, PICK_FROM_CAMERA)
    }

    /**
     * 사진파일 생성
     */
    private fun createImageFile(): File {
        // 이미지 파일 이름 ( temp_{시간}_ )
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "temp_${timeStamp}_"

        // 이미지가 저장될 폴더 이름 ( temp )
        val storgeDir = File("${requireActivity().externalCacheDir}/temp/")
        if(!storgeDir.exists()) storgeDir.mkdirs()

        // 빈 파일 생성
        return File.createTempFile(imageFileName, ".png", storgeDir)
    }

    /**
     * 이미지를 크롭한다.
     */
    private fun cropImage(photoUri: Uri) {
        Logger.d("profileImage : $profileImage")

        /**
         * 갤러리에서 선택한 경우에는 profileImge가 없으므로 새로 생성해야함.
         */
        if (profileImage == null) {
            try {
                profileImage = createImageFile()
            } catch (e: Exception) {
                AppToast(requireActivity()).showToastMessage(
                    "이미지 처리 오류! 다시 시도해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
                requireActivity().finish()

                Logger.p(e)
            }
        }

        // 크롭 후 저장할 Uri
        val uri = FileProvider.getUriForFile(requireActivity(), "${BuildConfig.APPLICATION_ID}.fileProvider", profileImage!!)
        Crop.of(photoUri, uri).asSquare().start(requireActivity(), this@ImagePickDialogFragment)
    }

}