package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.activity_action_walk.*
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
 * Class: CareWalkActivity
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class ActionWalkActivity : BaseActivity() {

    private val TAG = "ActionWalkActivity"
    private val CARE_WALK_UPLOAD_REQUEST = "$TAG.careWalkUploadRequest"
    private val CARE_WALK_DELETE_REQUEST = "$TAG.careDeleteUploadRequest"

    private var petId = -1
    private var date = ""
    private var walkData:CareRecordData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_action_walk)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        petId = intent?.getIntExtra("petId", petId)!!
        date = intent?.getStringExtra("date")!!
        walkData = intent?.getParcelableExtra("item") ?: walkData
        Logger.d("petId : $petId, date : $date, item : $walkData")

        btnClose.setOnClickListener{ onBackPressed() }
        btnComplete.setOnClickListener {
            retryIfNetworkAbsent {
                if (walkData == null) {
                    FirebaseAPI(this).logEventFirebase("활동체크_산책", "Click Event", "해당 기록 완료 버튼 클릭")
                    mApiClient.uploadCareRecord(CARE_WALK_UPLOAD_REQUEST, petId, "완료", date, "walk")
                } else {
                    mApiClient.deleteCareRecord(CARE_WALK_DELETE_REQUEST, walkData!!.pk)
                }
            }
        }

        if (walkData != null) {
            btnComplete.text = Helper.readStringRes(R.string.care_tooth_cancel)
        } else {
            btnComplete.text = Helper.readStringRes(R.string.care_walk_complete)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CARE_WALK_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_WALK_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_WALK_DELETE_REQUEST)) {
            mApiClient.cancelRequest(CARE_WALK_DELETE_REQUEST)
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
            CARE_WALK_UPLOAD_REQUEST -> {
                if (event.status == "ok") {
                    Airbridge.trackEvent("care", "click", "산책", null, null, null)
                    onBackPressed()
                }
            }

            CARE_WALK_DELETE_REQUEST -> {
                if (event.status == "ok") {
                    onBackPressed()
                }
            }
        }
    }
}