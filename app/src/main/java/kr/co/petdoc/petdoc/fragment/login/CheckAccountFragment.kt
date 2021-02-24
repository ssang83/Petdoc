package kr.co.petdoc.petdoc.fragment.login

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_check_account_item.view.*
import kotlinx.android.synthetic.main.dialog_clinic_price.*
import kotlinx.android.synthetic.main.fragment_check_account.*
import kotlinx.android.synthetic.main.fragment_check_account.animationArea
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.AccountData
import kr.co.petdoc.petdoc.network.response.submodel.UserInfoData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.AuthorizationDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: CheckAccountFragment
 * Created by kimjoonsung on 2020/07/29.
 *
 * Description :
 */
class CheckAccountFragment : BaseFragment() {

    private val TAG = "CheckAccountFragment"
    private val ACCOUNT_INTEGRATION_REQUEST = "$TAG.accountIntegration"

    private lateinit var accountAdapter:AccountAdapter
    private var userItems:MutableList<AccountData> = mutableListOf()

    lateinit var authorizationDataModel : AuthorizationDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        authorizationDataModel = ViewModelProvider(requireActivity()).get(AuthorizationDataModel::class.java)
        return inflater.inflate(R.layout.fragment_check_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)

        userItems = authorizationDataModel.userInfoData.value!!

        //===========================================================================================
        btnCancel.setOnClickListener { requireActivity().onBackPressed() }
        btnConfirm.setOnClickListener {
            if (selectPosition == -1) {
                AppToast(requireContext()).showToastMessage(
                    "통합하실 대표계정을 선택해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            } else {
                progressLayout.visibility = View.VISIBLE
                animationArea.playAnimation()
                Handler().postDelayed({
                    mApiClient.combineUser(ACCOUNT_INTEGRATION_REQUEST, userItems[selectPosition].userId, userItems[selectPosition].phone)
                }, 3000)
            }
        }

        //=========================================================================================
        accountAdapter = AccountAdapter()
        accountList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = accountAdapter
        }

        animationArea.apply{
            setAnimation(R.raw.loading_list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(ACCOUNT_INTEGRATION_REQUEST)) {
            mApiClient.cancelRequest(ACCOUNT_INTEGRATION_REQUEST)
        }

        animationArea.cancelAnimation()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            ACCOUNT_INTEGRATION_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        authorizationDataModel.combineFlag.value = true
                        authorizationDataModel.combineUserEmail.value = userItems[selectPosition].userEmail
                        requireActivity().onBackPressed()
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            loginErrorType(mesageKey),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )

                        progressLayout.visibility = View.GONE
                        animationArea.cancelAnimation()
                    }
                }
            }
        }
    }

    private fun loginErrorType(code: String) =
        when (code) {
            "101" -> "입력하신 아이디가 존재하지 않습니다."
            "102" -> "입력하신 아이디가 존재하지 않습니다."
            "103" -> "입력하신 비밀번호가 맞지 않습니다."
            "104" -> "user ID 에 대한 토큰이 존재하지 않습니다."
            "105" -> "user ID 에 대한 토큰이 만료되었습니다."
            "106" -> "user ID 에 대한 토큰이 만료되었습니다."
            "107" -> "입력하신 비밀번호 변경 코드가 맞지 않습니다."
            "108" -> "소셜 계정으로 가입하신 경우, 비밀번호 찾기를 이용할 수 없습니다."
            "109" -> "브이케어 유료회원의 탈퇴는 브이케어 매장을 통해 가능합니다."
            "110" -> "이미 확인이 완료된 email 정보입니다."
            "201" -> "이미 동일한 Email이 존재합니다."
            "202" -> "이미 동일한 휴대폰 번호가 존재합니다."
            "203" -> "Social 로그인이 실패하였습니다. 다시 시도해주세요."
            "204" -> "로그인을 지원하지 않는 Social 서비스입니다."
            "205" -> "입력하신 정보가 포함된 복수의 계정이 존재합니다. 계정 통합 절차를 진행해주세요."
            "206" -> "통합회원으로 이전하여야 합니다."
            "207" -> "이미 동일한 닉네임이 존재합니다."
            "501" -> "조회 조건이 존재하지 않습니다."
            "999" -> "기타 에러"
            "err" -> "알수 없는 에러가 발생했습니다. 잠시 후 다시 시도해주세요."
            "cancel" -> "로그인이 취소되었습니다."
            "err-인증 실패" -> "간편 로그인 인증에 실패했습니다."
            else -> ""
        }

    //==============================================================================================
    private var selectPosition = -1
    inner class AccountAdapter : RecyclerView.Adapter<AccountHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AccountHolder(layoutInflater.inflate(R.layout.adapter_check_account_item, parent, false))

        override fun onBindViewHolder(holder: AccountHolder, position: Int) {
            if (selectPosition == position) {
                holder.itemView.email.setTypeface(null, Typeface.BOLD)
                holder.itemView.check.visibility = View.VISIBLE
            } else {
                holder.itemView.email.setTypeface(null, Typeface.NORMAL)
                holder.itemView.check.visibility = View.GONE
            }

            holder.setLoginTypeImg(userItems[position].socialType)
            holder.setEmail(userItems[position].userEmail)

            val nickName = "${userItems[position].nickName} / ${accountType(userItems[position].socialType)}"
            holder.setNickname(nickName)

            holder.itemView.setOnClickListener {
                selectPosition = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = userItems.size

        private fun accountType(type: String) =
            when (type) {
                "kakao" -> "카카오 계정"
                "facebook" -> "페이스북 계정"
                "naver" -> "네이버 계정"
                "google" -> "구글 계정"
                "apple" -> "애플 계정"
                else  -> "이메일 회원가입"
            }
    }

    inner class AccountHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setLoginTypeImg(type: String) {
            when (type) {
                "kakao" -> item.loginTypeImg.setBackgroundResource(R.drawable.ic_login_kakao)
                "facebook" -> item.loginTypeImg.setBackgroundResource(R.drawable.ic_login_facebook)
                "naver" -> item.loginTypeImg.setBackgroundResource(R.drawable.ic_login_naver)
                "google" -> item.loginTypeImg.setBackgroundResource(R.drawable.ic_login_google)
                "apple" -> item.loginTypeImg.setBackgroundResource(R.drawable.social_apple)
                else -> item.loginTypeImg.setBackgroundResource(R.drawable.ic_login_email)
            }
        }

        fun setEmail(email: String) {
            item.email.text = email
        }

        fun setNickname(nickName: String) {
            item.nickName.text = nickName
        }
    }
}