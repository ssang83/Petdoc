package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.activity_action_tooth.*
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
 * Class: ActionToothActivity
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class ActionToothActivity : BaseActivity() {

    private val TAG = "ActionToothActivity"
    private val CARE_TOOTH_UPLOAD_REQUEST = "$TAG.careToothUploadRequest"
    private val CARE_TOOTH_DELETE_REQUEST = "$TAG.careToothDeleteRequest"

    private var petId = -1
    private var date = ""
    private var toothData:CareRecordData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_action_tooth)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        petId = intent?.getIntExtra("petId", petId)!!
        date = intent?.getStringExtra("date")!!
        toothData = intent?.getParcelableExtra("item") ?: toothData
        Logger.d("petId : $petId, date : $date, item : $toothData")

        btnClose.setOnClickListener { onBackPressed() }
        btnComplete.setOnClickListener {
            retryIfNetworkAbsent {
                if (toothData == null) {
                    FirebaseAPI(this).logEventFirebase("활동체크_양치", "Click Event", "해당 기록 완료 버튼 클릭")
                    mApiClient.uploadCareRecord(CARE_TOOTH_UPLOAD_REQUEST, petId, "완료", date, "teeth")
                } else {
                    mApiClient.deleteCareRecord(CARE_TOOTH_DELETE_REQUEST, toothData!!.pk)
                }
            }
        }

        if (toothData != null) {
            btnComplete.text = Helper.readStringRes(R.string.care_tooth_cancel)
        } else {
            btnComplete.text = Helper.readStringRes(R.string.care_tooth_complete)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CARE_TOOTH_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_TOOTH_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_TOOTH_DELETE_REQUEST)) {
            mApiClient.cancelRequest(CARE_TOOTH_DELETE_REQUEST)
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
            CARE_TOOTH_UPLOAD_REQUEST -> {
                if (event.status == "ok") {
                    Airbridge.trackEvent("care", "click", "양치", null, null, null)
                    onBackPressed()
                }
            }

            CARE_TOOTH_DELETE_REQUEST -> {
                if (event.status == "ok") {
                    onBackPressed()
                }
            }
        }
    }
}