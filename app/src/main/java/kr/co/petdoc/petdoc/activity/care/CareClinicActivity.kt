package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.activity_care_clinic.*
import kotlinx.android.synthetic.main.activity_care_clinic.btnClose
import kotlinx.android.synthetic.main.activity_care_clinic.btnComplete
import kotlinx.android.synthetic.main.activity_care_clinic.root
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.extensions.setReadOnly
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CareRecordData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: CareClinicActivity
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class CareClinicActivity : BaseActivity() {

    private val TAG = "CareClinicActivity"
    private val CARE_CLINIC_UPLOAD_REQUEST = "$TAG.careClinicUploadRequest"
    private val CARE_CLINIC_DELETE_REQUEST = "$TAG.careClinicDeleteRequest"
    private val CARE_CLINIC_CONTENT_UPLOAD_REQUEST = "$TAG.careClinicContentUploadRequest"
    private val CARE_CLINIC_CONTENT_DELETE_REQUEST = "$TAG.careClinicContentDeleteRequest"

    private var petId = -1
    private var date = ""
    private var clinicData: CareRecordData? = null
    private var clinicContent: CareRecordData? = null

    private var clinicType = ""

    private var flagBox = booleanArrayOf(false, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_care_clinic)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        petId = intent?.getIntExtra("petId", petId)!!
        date = intent?.getStringExtra("date")!!
        clinicData = intent?.getParcelableExtra("item") ?: clinicData
        clinicContent = intent?.getParcelableExtra("content") ?: clinicContent
        Logger.d("petId : $petId, date : $date, clinicData : $clinicData, clinicContent : $clinicContent")

        //=========================================================================================

        btnClose.setOnClickListener { onBackPressed() }
        btnComplete.setOnClickListener {
            retryIfNetworkAbsent {
                when {
                    btnComplete.text.toString() == Helper.readStringRes(R.string.care_tooth_cancel) -> {
                        if (clinicContent != null) {
                            mApiClient.deleteCareRecord(CARE_CLINIC_CONTENT_DELETE_REQUEST, clinicContent!!.pk)
                        }

                        if (clinicData != null) {
                            mApiClient.deleteCareRecord(CARE_CLINIC_DELETE_REQUEST, clinicData!!.pk)
                        }
                    }
                    else -> {
                        if (clinicType.isEmpty() && editClinic.text.toString().isEmpty()) {
                            AppToast(this).showToastMessage(
                                "진료 종류와 진료 내용을 입력해주세요.",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                            )

                            return@retryIfNetworkAbsent
                        }

                        if (clinicType.isEmpty()) {
                            AppToast(this).showToastMessage(
                                "진료 종류를 입력해주세요.",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                            )

                            return@retryIfNetworkAbsent
                        }

                        if (clinicType.isNotEmpty()) {
                            mApiClient.uploadCareRecord(CARE_CLINIC_UPLOAD_REQUEST, petId, clinicType, date, "clinic")
                        }

                        if (editClinic.text.toString().isNotEmpty()) {
                            mApiClient.uploadCareRecord(CARE_CLINIC_CONTENT_UPLOAD_REQUEST, petId, editClinic.text.toString(), date, "clinic_content")
                        }
                    }
                }
            }
        }

        editClinic.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    btnComplete.isSelected = true
                } else {
                    btnComplete.isSelected = false
                }
            }
        })

        //==============================================================================================

        if (clinicData != null || clinicContent != null) {
            btnComplete.text = Helper.readStringRes(R.string.care_tooth_cancel)

            setCareData(clinicData, clinicContent)

            editClinic.setReadOnly(true)

            clinicType1.setOnClickListener(null)
            clinicType2.setOnClickListener(null)
            clinicType3.setOnClickListener(null)
            clinicType4.setOnClickListener(null)

            btnComplete.isSelected = true
        } else {
            btnComplete.text = Helper.readStringRes(R.string.care_clinic_complete)

            editClinic.isEnabled = true

            clinicType1.setOnClickListener(clickListener)
            clinicType2.setOnClickListener(clickListener)
            clinicType3.setOnClickListener(clickListener)
            clinicType4.setOnClickListener(clickListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CARE_CLINIC_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_CLINIC_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_CLINIC_CONTENT_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_CLINIC_CONTENT_UPLOAD_REQUEST)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun setCareData(clinicData: CareRecordData?, content:CareRecordData?) {
        if (clinicData != null) {
            val clinic = clinicData.value!!.split("#")

            for (i in 0 until clinic.size) {
                when (clinic[i]) {
                    "스케일링" -> {
                        clinicType1.setTextColor(Helper.readColorRes(R.color.orange))
                        clinicType1.setBackgroundResource(R.drawable.bg_care_clinic_enable)
                    }

                    "건강검진" -> {
                        clinicType2.setTextColor(Helper.readColorRes(R.color.orange))
                        clinicType2.setBackgroundResource(R.drawable.bg_care_clinic_enable)
                    }

                    "필수접종" -> {
                        clinicType3.setTextColor(Helper.readColorRes(R.color.orange))
                        clinicType3.setBackgroundResource(R.drawable.bg_care_clinic_enable)
                    }

                    "심장사상충" -> {
                        clinicType4.setTextColor(Helper.readColorRes(R.color.orange))
                        clinicType4.setBackgroundResource(R.drawable.bg_care_clinic_enable)
                    }
                }
            }
        }

        if (content != null) {
            editClinic.setText(content!!.value)
            //editClinic.isEnabled = false
            editClinic.setReadOnly(true)
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
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            CARE_CLINIC_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    if (clinicType.isNotEmpty() && editClinic.text.toString().isNotEmpty()) {
                        flagBox[0] = true
                        Logger.d("flagBox[0] : ${flagBox[0]}, flagBox[1] :${flagBox[1]}")
                        if (flagBox[0] && flagBox[1]) {
                            Airbridge.trackEvent("care", "click", "진료", null, null, null)
                            FirebaseAPI(this).logEventFirebase("건강체크_진료", "Click Event", "진료 기록 완료")
                            onBackPressed()
                        }
                    } else {
                        onBackPressed()
                    }
                }
            }

            CARE_CLINIC_CONTENT_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    if (clinicType.isNotEmpty() && editClinic.text.toString().isNotEmpty()) {
                        flagBox[1] = true
                        Logger.d("flagBox[0] : ${flagBox[0]}, flagBox[1] :${flagBox[1]}")
                        if (flagBox[0] && flagBox[1]) {
                            Airbridge.trackEvent("care", "click", "진료", null, null, null)
                            FirebaseAPI(this).logEventFirebase("건강체크_진료", "Click Event", "진료 기록 완료")
                            onBackPressed()
                        }
                    } else {
                        onBackPressed()
                    }
                }
            }

            CARE_CLINIC_DELETE_REQUEST -> {
                if ("ok" == event.status) {
                    if (clinicData != null && clinicContent != null) {
                        flagBox[0] = true
                        Logger.d("flagBox[0] : ${flagBox[0]}, flagBox[1] :${flagBox[1]}")
                        if (flagBox[0] && flagBox[1]) {
                            onBackPressed()
                        }
                    } else {
                        onBackPressed()
                    }
                }
            }

            CARE_CLINIC_CONTENT_DELETE_REQUEST -> {
                if ("ok" == event.status) {
                    if (clinicData != null && clinicContent != null) {
                        flagBox[1] = true
                        Logger.d("flagBox[0] : ${flagBox[0]}, flagBox[1] :${flagBox[1]}")
                        if (flagBox[0] && flagBox[1]) {
                            onBackPressed()
                        }
                    } else {
                        onBackPressed()
                    }
                }
            }
        }
    }

    //==============================================================================================
    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.clinicType1 -> {
                clinicType1.setTextColor(Helper.readColorRes(R.color.orange))
                clinicType1.setBackgroundResource(R.drawable.bg_care_clinic_enable)

                btnComplete.isSelected = true

                clinicType = "${clinicType}#스케일링"
            }

            R.id.clinicType2 -> {
                clinicType2.setTextColor(Helper.readColorRes(R.color.orange))
                clinicType2.setBackgroundResource(R.drawable.bg_care_clinic_enable)

                btnComplete.isSelected = true

                clinicType = "${clinicType}#건강검진"
            }

            R.id.clinicType3 -> {
                clinicType3.setTextColor(Helper.readColorRes(R.color.orange))
                clinicType3.setBackgroundResource(R.drawable.bg_care_clinic_enable)

                btnComplete.isSelected = true

                clinicType = "${clinicType}#필수접종"
            }

            R.id.clinicType4 -> {
                clinicType4.setTextColor(Helper.readColorRes(R.color.orange))
                clinicType4.setBackgroundResource(R.drawable.bg_care_clinic_enable)

                btnComplete.isSelected = true

                clinicType = "${clinicType}#심장사상충"
            }
        }
    }
}