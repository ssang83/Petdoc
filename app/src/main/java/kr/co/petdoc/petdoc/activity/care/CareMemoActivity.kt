package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.activity_care_memo.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.extensions.setReadOnly
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CareRecordData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: CareMemoActivity
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class CareMemoActivity : BaseActivity() {

    private val TAG = "CareMemoActivity"
    private val CARE_MEMO_UPLOAD_REQUEST = "$TAG.careMemoUploadRequest"
    private val CARE_MEMO_DELETE_REQUEST = "$TAG.careMemoDeleteRequest"

    private var petId = -1
    private var date = ""
    private var memoData: CareRecordData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_care_memo)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        petId = intent?.getIntExtra("petId", petId)!!
        date = intent?.getStringExtra("date")!!
        memoData = intent?.getParcelableExtra("item") ?: memoData

        //==========================================================================================
        btnClose.setOnClickListener { onBackPressed() }
        btnComplete.setOnClickListener {
            retryIfNetworkAbsent {
                if (memoData == null) {
                    FirebaseAPI(this).logEventFirebase("건강체크_메모", "Click Event", "메모 기록 완료")
                    mApiClient.uploadCareRecord(CARE_MEMO_UPLOAD_REQUEST, petId, editMemo.text.toString().trim(), date, "memo")
                } else {
                    mApiClient.deleteCareRecord(CARE_MEMO_DELETE_REQUEST, memoData!!.pk)
                }
            }
        }

        editMemo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.toString()?.trim()?.length!! > 0) {
                    btnComplete.apply {
                        isSelected = true
                        isEnabled = true
                    }
                } else {
                    btnComplete.apply {
                        isSelected = false
                        isEnabled = false
                    }
                }
            }
        })

        //==========================================================================================
        if (memoData != null) {
            btnComplete.apply {
                text = Helper.readStringRes(R.string.care_tooth_cancel)
                isSelected = true
                isEnabled = true
            }

            editMemo.setReadOnly(true)
            setMemoData(memoData!!)
        } else {
            btnComplete.apply {
                text = Helper.readStringRes(R.string.care_memo_complete)
                isEnabled = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CARE_MEMO_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_MEMO_UPLOAD_REQUEST)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun setMemoData(memoData: CareRecordData) {
        editMemo.setText(memoData.value!!)
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
            CARE_MEMO_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    Airbridge.trackEvent("care", "click", "메모", null, null, null)
                    onBackPressed()
                }
            }

            CARE_MEMO_DELETE_REQUEST -> {
                if ("ok" == event.status) {
                    onBackPressed()
                }
            }
        }
    }
}