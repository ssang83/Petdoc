package kr.co.petdoc.petdoc.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_pet_chat.*
import kotlinx.android.synthetic.main.fragment_shopping.*
import kotlinx.android.synthetic.main.fragment_shopping.btnMyPage
import kotlinx.android.synthetic.main.fragment_shopping.btnNoti
import kotlinx.android.synthetic.main.fragment_shopping.notiUpdate
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.*
import kr.co.petdoc.petdoc.adapter.shopping.ShopAdapter
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.event.LoggedInEvent
import kr.co.petdoc.petdoc.event.LogoutEvent
import kr.co.petdoc.petdoc.event.PetInfoRefreshEvent
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ConnectionLiveData
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.repository.network.ShoppingPagingSource
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject

/**
 * Petdoc
 * Class: PetShoppingFragment
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : 펫 쇼핑 화면
 */
class PetShoppingFragment : Fragment() {
    private val eventBus = EventBus.getDefault()

    private lateinit var connectionLiveData: ConnectionLiveData
    private val isNetworkAvailable = MutableLiveData(true)
    private val scanner: Scanner by inject()
    private val apiService:PetdocApiService by inject()

    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("tab", "click", "shopping", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("펫닥_쇼핑_홈", "Page View", "쇼핑탭 선택 화면 page view")

        connectionLiveData = ConnectionLiveData(requireContext())
        connectionLiveData.observe(viewLifecycleOwner, Observer {
            isNetworkAvailable.postValue(it)
        })
        isNetworkAvailable.value = requireContext().isInternetConnected()

        if (!eventBus.isRegistered(this)) {
            eventBus.register(this)
        }
        setUpViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this)
        }
    }

    private fun setUpViews() {
        setUpToolBar()
        setUpRecyclerView()
        checkPushStatus()
    }

    private fun setUpToolBar() {
        btnMyPage.setOnSingleClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                val mypageIntent = Intent(requireActivity(), MyPageActivity::class.java)
                requireActivity().startActivity(mypageIntent)
            } else {
                val loginIntent = Intent(requireActivity(), LoginAndRegisterActivity::class.java)
                requireActivity().startActivity(loginIntent)
            }
        }

        btnNoti.setOnSingleClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                val mypageIntent = Intent(requireActivity(), PushNotiActivity::class.java)
                requireActivity().startActivity(mypageIntent)
            } else {
                val loginIntent = Intent(requireActivity(), LoginAndRegisterActivity::class.java)
                requireActivity().startActivity(loginIntent)
            }
        }
    }

    private fun setUpRecyclerView() {
        retryIfNetworkAbsent {
            recyclerView.adapter = ShopAdapter()
            lifecycleScope.launch {
                val preferences: PersistentPrefs by inject()
                val apiService: PetdocApiService by inject()
                val repository: PetdocRepository by inject()
                val source = ShoppingPagingSource(repository, apiService, preferences)
                val pagingData = Pager(
                    config = PagingConfig(enablePlaceholders = false, pageSize = 20),
                    pagingSourceFactory = { source }
                ).flow
                pagingData.collectLatest {
                    (recyclerView.adapter as ShopAdapter).submitData(it)
                }
            }
        }
    }

    private fun checkPushStatus() {
        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            val response = withContext(Dispatchers.IO) { apiService.getPushUnRead() }
            if (response.code == "SUCCESS") {
                val status = response.resultData as Boolean
                notiUpdate.visibility = if (status) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoggedInEvent(event: LoggedInEvent) {
        setUpRecyclerView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        setUpRecyclerView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPetInfoRefreshEvent(event: PetInfoRefreshEvent) {
        setUpRecyclerView()
    }

    private fun refreshCustoMealPetInfo() {
        lifecycleScope.launch {
            val preferences: PersistentPrefs by inject()
            val apiService: PetdocApiService by inject()
            val oldUserId = preferences.getValue(AppConstants.PREF_KEY_OLD_USER_ID, "")
            val userId = if(oldUserId.isNotEmpty()) {
                oldUserId.toInt()
            } else {
                0
            }
            val petInfos = apiService.getPetInfoListWithCoroutine(userId)
            recyclerView.adapter?.notifyItemChanged(1, petInfos)
        }
    }

    fun retryIfNetworkAbsent(invokeIfNetworkExist: Boolean = true, callback: () -> Unit) {
        val isScannerConnected = scanner.isConnected()
        val hasInternet = isNetworkAvailable.value?:true
        if (isScannerConnected && hasInternet.not()) {
            // 스캐너 연결 해제 및 네트웍 연결 확인 팝업
            TwoBtnDialog(requireContext()).apply {
                setTitle("펫닥 스캐너 연결 해제")
                setMessage("이용 가능한 네트워크가 없습니다.\n연결을 해제하시겠습니까?")
                setCancelable(false)
                setConfirmButton("해제", View.OnClickListener {
                    scanner.disconnect()
                    retryIfNetworkAbsent {
                        callback.invoke()
                    }
                    dismiss()
                })
                setCancelButton("취소", View.OnClickListener {
                    dismiss()
                })
            }.show()
        } else {
            if (hasInternet) {
                if (invokeIfNetworkExist) {
                    callback.invoke()
                }
            } else {
                // 네트웍 연결 확인 팝업
                AlertDialog.Builder(requireContext(), R.style.DefaultAlertDialogStyle)
                    .setMessage("네트워크가 연결되어 있지 않습니다.\n확인 후 다시 시도해주세요.")
                    .setCancelable(false)
                    .setPositiveButton(R.string.caption_retry) { _, _ ->
                        retryIfNetworkAbsent {
                            callback.invoke()
                        }
                    }.also {
                        it.show()
                    }
            }
        }
    }

}