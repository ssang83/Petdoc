package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.activity_action_bath.*
import kotlinx.android.synthetic.main.activity_action_bath.btnClose
import kotlinx.android.synthetic.main.activity_action_bath.btnComplete
import kotlinx.android.synthetic.main.activity_action_bath.root
import kotlinx.android.synthetic.main.activity_action_walk.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.BannerDetailResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CareRecordData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: ActionBathActivity
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class ActionBathActivity : BaseActivity() {

    private val TAG = "ActionBathActivity"
    private val CARE_BATH_UPLOAD_REQUEST = "$TAG.careBathUploadRequest"
    private val CARE_BATH_DELETE_REQUEST = "$TAG.careBathDeleteRequest"

    private var petId = -1
    private var date = ""
    private var bathData:CareRecordData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_action_bath)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        petId = intent?.getIntExtra("petId", petId)!!
        date = intent?.getStringExtra("date")!!
        bathData = intent?.getParcelableExtra("item") ?: bathData
        Logger.d("petId : $petId, date : $date, item : $bathData")

        btnClose.setOnClickListener { onBackPressed() }
        btnComplete.setOnClickListener {
            retryIfNetworkAbsent {
                if (bathData == null) {
                    FirebaseAPI(this).logEventFirebase("활동체크_목욕", "Click Event", "해당 기록 완료 버튼 클릭")
                    mApiClient.uploadCareRecord(CARE_BATH_UPLOAD_REQUEST, petId, "완료", date, "bath")
                } else {
                    mApiClient.deleteCareRecord(CARE_BATH_DELETE_REQUEST, bathData!!.pk)
                }
            }
        }

        if (bathData != null) {
            btnComplete.text = Helper.readStringRes(R.string.care_tooth_cancel)
        } else {
            btnComplete.text = Helper.readStringRes(R.string.care_bath_complete)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CARE_BATH_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_BATH_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_BATH_DELETE_REQUEST)) {
            mApiClient.cancelRequest(CARE_BATH_DELETE_REQUEST)
        }
    }

    override fun onBackPressed() {
        finish()
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
            CARE_BATH_UPLOAD_REQUEST -> {
                if (event.status == "ok") {
                    Airbridge.trackEvent("care", "click", "목욕", null, null, null)
                    onBackPressed()
                }
            }

            CARE_BATH_DELETE_REQUEST -> {
                if (event.status == "ok") {
                    onBackPressed()
                }
            }
        }
    }
}