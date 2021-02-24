package kr.co.petdoc.petdoc.fragment.scanner.ear

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import kotlinx.android.synthetic.main.fragment_ear.*
import kotlinx.coroutines.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.ImageAlbumActivity
import kr.co.petdoc.petdoc.activity.care.ScannerGuideVideoActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImage
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImageDB
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.CustomRoundedCornersTransformation
import kr.co.petdoc.petdoc.viewmodel.CareScanDataModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: EarFragment
 * Created by kimjoonsung on 2020/05/18.
 *
 * Description :
 */
class EarFragment : Fragment() {

    private val REQUEST_ALBUM = 0x1002

    private lateinit var dataModel: CareScanDataModel
    private lateinit var earImageDB: EarImageDB

    val timeFormat = SimpleDateFormat("a KK:mm:ss", Locale.KOREAN)
    val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)
    val dayFormat = SimpleDateFormat("dd", Locale.KOREAN)

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    private var mYear = ""
    private var mMonth = ""
    private var mDay = ""

    private var isUpdate = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(CareScanDataModel::class.java)
        return inflater.inflate(R.layout.fragment_ear, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        earImageDB = EarImageDB.getInstance(requireActivity())!!

        btnVideoGuide.setOnClickListener {
            val uri = "android.resource://${requireContext().packageName}/raw/scanner_guide_ear"
            startActivity(Intent(requireActivity(), ScannerGuideVideoActivity::class.java).apply {
                putExtra("uri", uri)
            })
        }

        btnClose.setOnClickListener { requireActivity().onBackPressed() }

        btnSave.setOnClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("건강체크_귀", "Click Event", "귀보기 기록 완료")
            saveImage()
        }

        layoutLeftEar.setOnClickListener {
            dataModel.earType.value = "left"
            findNavController().navigate(EarFragmentDirections.actionEarFragmentToEarScanFragment())
        }

        layoutRightEar.setOnClickListener {
            dataModel.earType.value = "right"
            findNavController().navigate(EarFragmentDirections.actionEarFragmentToEarScanFragment())
        }

        //==========================================================================================

        if (dataModel.earType.value?.isNotEmpty()!!) {
            if (dataModel.leftEarImgPath.value.toString().isNotEmpty()) {
                Glide.with(requireContext())
                    .load(dataModel.leftEarImgPath.value.toString())
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            CustomRoundedCornersTransformation(
                                requireContext(),
                                Helper.convertDpToPx(requireContext(), 25f).toInt(),
                                0,
                                CustomRoundedCornersTransformation.CornerType.TOP
                            )
                        )
                    )
                    .into(leftScanImage)
            }

            if (dataModel.rightEarImgPath.value.toString().isNotEmpty()) {
                Glide.with(requireContext())
                    .load(dataModel.rightEarImgPath.value.toString())
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            CustomRoundedCornersTransformation(
                                requireContext(),
                                Helper.convertDpToPx(requireContext(), 25f).toInt(),
                                0,
                                CustomRoundedCornersTransformation.CornerType.TOP
                            )
                        )
                    )
                    .into(rightScanImage)
            }
        }

        btnSave.apply {
            if (dataModel.rightEarImgPath.value?.isNotEmpty()!!
                || dataModel.leftEarImgPath.value?.isNotEmpty()!!
            ) {
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.main_color_round_rect)

                btnSave.isEnabled = true
            } else {
                btnSave.isEnabled = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mYear = yearFormat.format(Date(System.currentTimeMillis()))
        mMonth = monthFormat.format(Date(System.currentTimeMillis()))
        mDay = dayFormat.format(Date(System.currentTimeMillis()))

        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            isUpdate = async { if (earImageDB.earImageDao().loadValueByDate(dataModel.petId.value!!, mYear, mMonth, mDay) != null) {
                true
            } else {
                false
            } }.await()

            Logger.d("Ear Image update : $isUpdate")
        }
    }

    override fun onDestroyView() {
        EarImageDB.destroyInstance()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    requireActivity().finish()
                }
            }

            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun saveImage() {
        if (dataModel.petId.value!! != 0) {
            lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                withContext(Dispatchers.Default) {
                    val item = EarImage().apply {
                        petId = dataModel.petId.value!!.toInt()
                        leftEarPath = dataModel.leftEarImgPath.value.toString()
                        leftEarImage = dataModel.leftEarImgByte.value
                        rightEarPath = dataModel.rightEarImgPath.value.toString()
                        rightEarImage = dataModel.rightEarImgByte.value
                        year = mYear
                        month = mMonth
                        day = mDay
                        time = timeFormat.format(Date(System.currentTimeMillis()))
                    }

                    if (isUpdate) {
                        earImageDB.earImageDao().updateEarImage(
                                dataModel.petId.value!!.toInt(),
                                dataModel.leftEarImgPath.value.toString(),
                                dataModel.leftEarImgByte.value,
                                dataModel.rightEarImgPath.value.toString(),
                                dataModel.rightEarImgByte.value,
                                mYear,
                                mMonth,
                                mDay,
                                timeFormat.format(Date(System.currentTimeMillis()))
                        )
                    } else {
                        earImageDB.earImageDao().insert(item)
                    }
                }
            }

            Airbridge.trackEvent("care", "click", "귀", null, null, null)

            startActivityForResult(Intent(requireActivity(), ImageAlbumActivity::class.java).apply {
                putExtra("fromMy", false)
                putExtra("petId", dataModel.petId.value!!)
                putExtra("petImage", dataModel.petImage.value!!)
            }, REQUEST_ALBUM)
        }
    }
}