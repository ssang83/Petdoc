package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import android.view.View
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.activity_action_poop.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CareRecordData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: ActionPoopActivity
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class ActionPoopActivity : BaseActivity() {

    private val TAG = "ActionPoopActivity"
    private val CARE_PEE_UPLOAD_REQUEST = "$TAG.carePeeUploadRequest"
    private val CARE_FECES_UPLOAD_REQUEST = "$TAG.careFecesUploadRequest"
    private val CARE_PEE_DELETE_REQUEST = "$TAG.carePeeDeleteRequest"
    private val CARE_FECES_DELETE_REQUEST = "$TAG.careFecesDeleteRequest"

    private var petId = -1
    private var date = ""

    private var urineValue = ""
    private var fecesValue = ""

    private var flagBox = booleanArrayOf(false, false)

    private var fecesData: CareRecordData? = null
    private var urineData:CareRecordData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_poop)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        petId = intent?.getIntExtra("petId", petId)!!
        date = intent?.getStringExtra("date")!!
        fecesData = intent?.getParcelableExtra("feces") ?: fecesData
        urineData = intent?.getParcelableExtra("urine") ?: urineData
        Logger.d("petId : $petId, date : $date, feces : $fecesData, urine : $urineData")

        //========================================================================================

        btnClose.setOnClickListener { onBackPressed() }
        btnComplete.setOnClickListener {
            retryIfNetworkAbsent {
                when {
                    btnComplete.text.toString() == "대소변 기록 취소" -> {
                        if (urineData != null) {
                            mApiClient.deleteCareRecord(CARE_PEE_DELETE_REQUEST, urineData!!.pk)
                        }

                        if (fecesData != null) {
                            mApiClient.deleteCareRecord(CARE_FECES_DELETE_REQUEST, fecesData!!.pk)
                        }
                    }

                    else -> {
                        if (urineValue.isNotEmpty()) {
                            mApiClient.uploadCareRecord(CARE_PEE_UPLOAD_REQUEST, petId, urineValue, date, "urine")
                        }

                        if (fecesValue.isNotEmpty()) {
                            mApiClient.uploadCareRecord(CARE_FECES_UPLOAD_REQUEST, petId, fecesValue, date, "feces")
                        }

                        FirebaseAPI(this).logEventFirebase("활동체크_대소변", "Click Event", "해당 기록 완료 버튼 클릭")
                    }
                }
            }
        }
        //========================================================================================

        if (urineData != null || fecesData != null) {
            btnComplete.text = "대소변 기록 취소"
            btnComplete.isSelected = true

            setCareData(fecesData, urineData)
        } else {
            btnComplete.text = Helper.readStringRes(R.string.care_poop_complete)

            layoutPeeType1.setOnClickListener(peeTypeClickListener)
            layoutPeeType2.setOnClickListener(peeTypeClickListener)
            layoutPeeType3.setOnClickListener(peeTypeClickListener)
            layoutPeeType4.setOnClickListener(peeTypeClickListener)
            layoutPeeType5.setOnClickListener(peeTypeClickListener)
            layoutPeeType6.setOnClickListener(peeTypeClickListener)

            layoutFecesType1.setOnClickListener(fecesTypeClickListener)
            layoutFecesType2.setOnClickListener(fecesTypeClickListener)
            layoutFecesType3.setOnClickListener(fecesTypeClickListener)
            layoutFecesType4.setOnClickListener(fecesTypeClickListener)
            layoutFecesType5.setOnClickListener(fecesTypeClickListener)
            layoutFecesType6.setOnClickListener(fecesTypeClickListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CARE_FECES_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_FECES_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_PEE_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_PEE_UPLOAD_REQUEST)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun setCareData(fecesData: CareRecordData?, urineData:CareRecordData?) {
        if (urineData != null) {
            when (urineData.value) {
                "fffabe" -> handlePeeType(peeType1)
                "f8e71c" -> handlePeeType(peeType2)
                "f5a623" -> handlePeeType(peeType3)
                "ffc0de" -> handlePeeType(peeType4)
                "67401f" -> handlePeeType(peeType5)
                "9df5bf" -> handlePeeType(peeType6)
            }
        }

        if (fecesData != null) {
            when (fecesData.value) {
                Helper.readStringRes(R.string.care_poop_type1) -> handleFecesType(
                    layoutFecesType1,
                    fecesType1
                )
                Helper.readStringRes(R.string.care_poop_type2) -> handleFecesType(
                    layoutFecesType2,
                    fecesType2
                )
                Helper.readStringRes(R.string.care_poop_type3) -> handleFecesType(
                    layoutFecesType3,
                    fecesType3
                )
                Helper.readStringRes(R.string.care_poop_type4) -> handleFecesType(
                    layoutFecesType4,
                    fecesType4
                )
                Helper.readStringRes(R.string.care_poop_type5) -> handleFecesType(
                    layoutFecesType5,
                    fecesType5
                )
                Helper.readStringRes(R.string.care_poop_type6) -> handleFecesType(
                    layoutFecesType6,
                    fecesType6
                )
            }
        }

        layoutPeeType1.setOnClickListener(null)
        layoutPeeType2.setOnClickListener(null)
        layoutPeeType3.setOnClickListener(null)
        layoutPeeType4.setOnClickListener(null)
        layoutPeeType5.setOnClickListener(null)
        layoutPeeType6.setOnClickListener(null)

        layoutFecesType1.setOnClickListener(null)
        layoutFecesType2.setOnClickListener(null)
        layoutFecesType3.setOnClickListener(null)
        layoutFecesType4.setOnClickListener(null)
        layoutFecesType5.setOnClickListener(null)
        layoutFecesType6.setOnClickListener(null)
    }

    private fun handlePeeType(view: View) {
        peeType1.visibility = View.GONE
        peeType2.visibility = View.GONE
        peeType3.visibility = View.GONE
        peeType4.visibility = View.GONE
        peeType5.visibility = View.GONE
        peeType6.visibility = View.GONE

        view.visibility = View.VISIBLE
    }

    private fun handleFecesType(bgView: View, textView:View) {
        layoutFecesType1.isSelected = false
        fecesType1.isSelected = false
        layoutFecesType2.isSelected = false
        fecesType2.isSelected = false
        layoutFecesType3.isSelected = false
        fecesType3.isSelected = false
        layoutFecesType4.isSelected = false
        fecesType4.isSelected = false
        layoutFecesType5.isSelected = false
        fecesType5.isSelected = false
        layoutFecesType6.isSelected = false
        fecesType6.isSelected = false

        bgView.isSelected = true
        textView.isSelected = true

    }

    private val peeTypeClickListener = View.OnClickListener {
        when (it?.id) {
            R.id.layoutPeeType1 -> {
                handlePeeType(peeType1)
                urineValue = "fffabe"

                btnComplete.isSelected = true
            }

            R.id.layoutPeeType2 -> {
                handlePeeType(peeType2)
                urineValue = "f8e71c"

                btnComplete.isSelected = true
            }

            R.id.layoutPeeType3 -> {
                handlePeeType(peeType3)
                urineValue = "f5a623"

                btnComplete.isSelected = true
            }

            R.id.layoutPeeType4 -> {
                handlePeeType(peeType4)
                urineValue = "ffc0de"

                btnComplete.isSelected = true
            }

            R.id.layoutPeeType5 -> {
                handlePeeType(peeType5)
                urineValue = "67401f"

                btnComplete.isSelected = true
            }

            R.id.layoutPeeType6 -> {
                handlePeeType(peeType6)
                urineValue = "9df5bf"

                btnComplete.isSelected = true
            }
        }
    }

    private val fecesTypeClickListener = View.OnClickListener {
        when (it?.id) {
            R.id.layoutFecesType1 -> {
                handleFecesType(it, fecesType1)
                fecesValue = Helper.readStringRes(R.string.care_poop_type1)

                btnComplete.isSelected = true
            }

            R.id.layoutFecesType2 -> {
                handleFecesType(it, fecesType2)
                fecesValue = Helper.readStringRes(R.string.care_poop_type2)

                btnComplete.isSelected = true
            }

            R.id.layoutFecesType3 -> {
                handleFecesType(it, fecesType3)
                fecesValue = Helper.readStringRes(R.string.care_poop_type3)

                btnComplete.isSelected = true
            }

            R.id.layoutFecesType4 -> {
                handleFecesType(it, fecesType4)
                fecesValue = Helper.readStringRes(R.string.care_poop_type4)

                btnComplete.isSelected = true
            }

            R.id.layoutFecesType5 -> {
                handleFecesType(it, fecesType5)
                fecesValue = Helper.readStringRes(R.string.care_poop_type5)

                btnComplete.isSelected = true
            }

            R.id.layoutFecesType6 -> {
                handleFecesType(it, fecesType6)
                fecesValue = Helper.readStringRes(R.string.care_poop_type6)

                btnComplete.isSelected = true
            }
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
    fun onEventMainThread(event : NetworkBusResponse) {
        when (event.tag) {
            CARE_PEE_UPLOAD_REQUEST -> {
                if (event.status == "ok") {
                    if (urineValue.isNotEmpty() && fecesValue.isNotEmpty()) {
                        flagBox[0] = true
                        if (flagBox[0] && flagBox[1]) {
                            Airbridge.trackEvent("care", "click", "대소변", null, null, null)
                            onBackPressed()
                        }
                    } else {
                        onBackPressed()
                    }
                }
            }

            CARE_FECES_UPLOAD_REQUEST -> {
                 if(event.status == "ok") {
                     if (urineValue.isNotEmpty() && fecesValue.isNotEmpty()) {
                         flagBox[1] = true
                         if (flagBox[0] && flagBox[1]) {
                             Airbridge.trackEvent("care", "click", "대소변", null, null, null)
                             onBackPressed()
                         }
                     } else {
                         onBackPressed()
                     }
                }
            }

            CARE_PEE_DELETE_REQUEST -> {
                if(event.status == "ok") {
                    if (urineData != null && fecesData != null) {
                        flagBox[0] = true
                        if(flagBox[0] && flagBox[1]) {
                            onBackPressed()
                        }
                    } else {
                        onBackPressed()
                    }
                }
            }

            CARE_FECES_DELETE_REQUEST -> {
                if(event.status == "ok") {
                    if (urineData != null && fecesData != null) {
                        flagBox[1] = true
                        if(flagBox[0] && flagBox[1]) {
                            onBackPressed()
                        }
                    } else {
                        onBackPressed()
                    }
                }
            }
        }
    }
}