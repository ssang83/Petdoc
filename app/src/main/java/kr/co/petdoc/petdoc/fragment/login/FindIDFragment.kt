package kr.co.petdoc.petdoc.fragment.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_find_id.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: FindIDFragment
 * Created by kimjoonsung on 2020/07/30.
 *
 * Description :
 */
class FindIDFragment : BaseFragment() {

    private val TAG = "FindIDFragment"
    private val FIND_ID_REQUEST = "$TAG.findIdRequest"

    private var flag = booleanArrayOf(false,false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_find_id, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("find", "click", "find_id", null, null, null)

        btnFind.setOnClickListener {
            mApiClient.findID(FIND_ID_REQUEST, editName.text.toString(), editPhoneNumber.text.toString())
        }

        //==========================================================================================
        editName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.trim()!!.length > 0) {
                    flag[0] = true
                } else {
                    flag[0] = false
                }

                checkButton()
            }
        })

        editPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! >= 9) {
                    flag[1] = true
                } else {
                    flag[1] = false
                }

                checkButton()
            }
        })

        //===========================================================================================

        btnFind.isEnabled = false
    }

    override fun onPause() {
        super.onPause()

        if (editName.isFocusable) {
            hideKeyboard(editName)
        }

        if (editPhoneNumber.isFocusable) {
            hideKeyboard(editPhoneNumber)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(FIND_ID_REQUEST)) {
            mApiClient.cancelRequest(FIND_ID_REQUEST)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            FIND_ID_REQUEST -> {
                if ("ok" == event.status) {
                    if (event.response.startsWith("입력하신 정보")) {
                        AppToast(requireContext()).showToastMessage(
                            event.response,
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    } else {
                        FindIDResultDialogFragment(null).apply {
                            bundleOf("message" to event.response).let {
                                arguments = it
                            }
                        }.show(childFragmentManager, "FindID")
                    }
                }
            }
        }
    }

    private fun checkButton() {
        btnFind.apply {
            if (flag[0] && flag[1]) {
                btnFind.isEnabled = true
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.orange_round_rect_25)
            } else {
                btnFind.isEnabled = false
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.palegrey_round_rect)
            }
        }
    }
}