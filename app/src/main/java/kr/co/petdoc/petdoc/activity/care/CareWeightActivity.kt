package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.activity_care_weight.*
import kotlinx.android.synthetic.main.activity_care_weight.btnClose
import kotlinx.android.synthetic.main.activity_care_weight.btnComplete
import kotlinx.android.synthetic.main.activity_care_weight.root
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CareRecordData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.ScrollRulerView
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: CareWeightActivity
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class CareWeightActivity : BaseActivity() {

    private val TAG = "CareWeightActivity"
    private val CARE_WEIGHT_UPLOAD_REQUEST = "$TAG.careWeightUploadRequest"
    private val CARE_WEIGHT_DELETE_REQUEST = "$TAG.careWeightDeleteRequest"

    private var weightData:CareRecordData? = null
    private var weightValue = ""

    private var petId = -1
    private var date = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_care_weight)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        petId = intent?.getIntExtra("petId", petId)!!
        date = intent?.getStringExtra("date")!!
        weightData = intent?.getParcelableExtra("item") ?: weightData
        Logger.d("petId : $petId, date : $date, weightData : $weightData")

        //==========================================================================================

        btnClose.setOnClickListener { onBackPressed() }
        btnComplete.setOnClickListener {
            retryIfNetworkAbsent {
                if (weightData == null) {
                    if (weightValue.isNotEmpty()) {
                        FirebaseAPI(this).logEventFirebase("건강체크_체중", "Click Event", "체중 기록 완료")
                        mApiClient.uploadCareRecord(
                            CARE_WEIGHT_UPLOAD_REQUEST,
                            petId,
                            weightValue,
                            date,
                            "weight"
                        )
                    } else {
                        AppToast(this).showToastMessage(
                            "체중을 입력 해주세요.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                } else {
                    mApiClient.deleteCareRecord(CARE_WEIGHT_DELETE_REQUEST, weightData!!.pk)
                }
            }
        }

        //=========================================================================================
        ruler_area.apply {
            max_range = 49
            callback = object : ScrollRulerView.PositionChangeCallback{
                override fun positionCallback(value: Float) {
                    weightValue = String.format("%.1f", value)
                    inputWeight.text = weightValue
                    Logger.d("weight value : $weightValue")
                }
            }
        }

        if (weightData != null) {
            btnComplete.text = Helper.readStringRes(R.string.care_tooth_cancel)

            setCareData(weightData!!)
        } else {
            btnComplete.text = Helper.readStringRes(R.string.care_weight_complete)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CARE_WEIGHT_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_WEIGHT_UPLOAD_REQUEST)
        }
    }

    private fun setCareData(weightData: CareRecordData) {
        ruler_area.moveToWeight(weightData.value!!.toFloat())
        inputWeight.text = weightData.value
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
            CARE_WEIGHT_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    Airbridge.trackEvent("care", "click", "체중", null, null, null)
                    onBackPressed()
                }
            }

            CARE_WEIGHT_DELETE_REQUEST ->{
                if ("ok" == event.status) {
                    onBackPressed()
                }
            }
        }
    }
}