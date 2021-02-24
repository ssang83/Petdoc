package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_action_communion.*
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
 * Class: ActionCommunionActivity
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class ActionCommunionActivity : BaseActivity() {

    private val TAG = "ActionCommunionActivity"
    private val CARE_LOVE_UPLOAD_REQUEST = "$TAG.careLoveUploadRequest"
    private val CARE_LOVE_DELETE_REQUEST = "$TAG.careLoveDeleteRequest"

    private var petId = -1
    private var date = ""
    private var loveData:CareRecordData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_action_communion)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        petId = intent?.getIntExtra("petId", petId)!!
        date = intent?.getStringExtra("date")!!
        loveData = intent?.getParcelableExtra("item") ?: loveData
        Logger.d("petId : $petId, date : $date, item :$loveData")

        btnClose.setOnClickListener { onBackPressed() }
        btnComplete.setOnClickListener {
            if (loveData == null) {
                FirebaseAPI(this).logEventFirebase("활동체크_교감", "Click Event", "해당 기록 완료 버튼 클릭")
                mApiClient.uploadCareRecord(CARE_LOVE_UPLOAD_REQUEST, petId, "완료", date, "communion")
            } else {
                mApiClient.deleteCareRecord(CARE_LOVE_DELETE_REQUEST, loveData!!.pk)
            }
        }

        if (loveData != null) {
            btnComplete.text = Helper.readStringRes(R.string.care_tooth_cancel)
        } else {
            btnComplete.text = Helper.readStringRes(R.string.care_communion_complete)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CARE_LOVE_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_LOVE_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_LOVE_DELETE_REQUEST)) {
            mApiClient.cancelRequest(CARE_LOVE_DELETE_REQUEST)
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
            CARE_LOVE_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    onBackPressed()
                }
            }

            CARE_LOVE_DELETE_REQUEST -> {
                if ("ok" == event.status) {
                    onBackPressed()
                }
            }
        }
    }
}