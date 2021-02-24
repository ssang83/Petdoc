package kr.co.petdoc.petdoc.fragment.mypage

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_pet_information.*
import kotlinx.coroutines.Dispatchers
import kr.co.petdoc.petdoc.PetdocApplication
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MatchFoodActivity
import kr.co.petdoc.petdoc.activity.MatchFoodModifyActivity
import kr.co.petdoc.petdoc.activity.MatchFoodResultActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.adapter.mypage.PetInfoAdapter
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.recyclerview.ItemActionListener
import kr.co.petdoc.petdoc.recyclerview.ItemDragListener
import kr.co.petdoc.petdoc.recyclerview.RecyclerViewSwipeDecorator
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*


/**
 * Petdoc
 * Class: PetInformationFragment
 * Created by kimjoonsung on 2020/04/09.
 *
 * Description : 반려동물 정보 화면
 */
class PetInformationFragment : BaseFragment(), PetInfoAdapter.AdapterListener, ItemDragListener {

    private val LOGTAG = "PetInformationFragment"
    private val PET_DELETE_REQUEST = "$LOGTAG.petDeleteRequest"
    private val PET_CHANGE_LIST_REQUEST = "$LOGTAG.petChangeListRequest"

    private val REQUEST_AUTH = 1001

    private lateinit var petInfoModel:PetAddInfoDataModel
    private lateinit var foodRecommendDataModel : FoodRecommentDataModel

    private lateinit var mAdapter: PetInfoAdapter

    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)

    private var currentYear = ""
    private var currentMonth = ""

    private var deletePosition = -1

    private var itemTouchHelper:ItemTouchHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_pet_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("mypage", "click", "pet_list", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("동물정보", "Page View", "반려동물 정보 페이지뷰")

        currentYear = yearFormat.format(Date(System.currentTimeMillis()))
        currentMonth = monthFormat.format(Date(System.currentTimeMillis()))

        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)
        petInfoModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)

        //===========================================================================================

        mAdapter = PetInfoAdapter().apply {
            setListener(this@PetInformationFragment)
        }

        petInformationList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = mAdapter
        }

        //==========================================================================================

        btnBack.setOnClickListener {
            if (mAdapter.getEditMode()) {
                Logger.d("petId : ${mAdapter.getChangePetId()}")
                if(mAdapter.getChangePetId().size > 0) {
                    mApiClient.changePetList(PET_CHANGE_LIST_REQUEST, mAdapter.getChangePetId())
                } else {
                    mAdapter.setEditMode(false)
                }
            } else {
                requireActivity().onBackPressed()
            }
        }
        btnAdd.setOnClickListener {
            if (StorageUtils.loadIntValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_MOBILE_CONFIRM
                ) == 1
            ) {
                Airbridge.trackEvent("mypage", "click", "pet_add", null, null, null)
                FirebaseAPI(requireActivity()).logEventFirebase("동물정보_추가", "Click Event", "반려동물 정보 페이지 내 추가하기 버튼 클릭")
                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 1)
                })
            } else {
                startActivityForResult(Intent(requireActivity(), MobileAuthActivity::class.java),REQUEST_AUTH)
            }
        }

        petImage.setOnClickListener {
            if (StorageUtils.loadIntValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_MOBILE_CONFIRM
                ) == 1
            ) {
                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 1)
                })
            } else {
                startActivityForResult(Intent(requireActivity(), MobileAuthActivity::class.java), REQUEST_AUTH)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        retryIfNetworkAbsent { loadPetInfo() }
    }

    override fun onItemClicked(item: PetInfoItem) {
        Airbridge.trackEvent("mypage", "click", "pet_select", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("동물정보_동물선택", "Click Event", "반려동물 리스트 클릭")

        petInfoModel.petId.value = item.id
        petInfoModel.petName.value = item.name
        petInfoModel.petKind.value = item.kind
        petInfoModel.petKindKey.value = getKindKey(item.kind!!)
        petInfoModel.petBreed.value = item.species
        petInfoModel.petSex.value = item.gender
        petInfoModel.petNeutralization.value = item.isNeutralized
        petInfoModel.petVaccine.value = item.inoculationStatus
        petInfoModel.petProfile.value = item.imageUrl
        petInfoModel.petBirth.value = item.birthday
        petInfoModel.petAgeType.value = item.ageType
        petInfoModel.petAge.value = calculateBirthday(item)

        if (item.regPetStep == 6) {
            petInfoModel.type.value = PetAddType.EDIT.ordinal
            if (item.regInfoAllStep == item.regInfoStep && item.cmInfoId != null) {
                startActivity(Intent(requireActivity(), MatchFoodModifyActivity::class.java).apply {
                    putExtra("item", item)
                    putExtra("editType", "matchFood")
                })
            } else {
                startActivity(Intent(requireActivity(), MatchFoodModifyActivity::class.java).apply {
                    putExtra("item", item)
                    putExtra("editType", "pet")
                })
            }
        } else {
            when (item.regPetStep.plus(1)) {
                6 -> {
                    startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                        putExtra("type", PetAddType.ADD.ordinal)
                        putExtra("step", 6)
                        putExtra("item", item)
                    })
                }

                5 -> {
                    startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                        putExtra("type", PetAddType.ADD.ordinal)
                        putExtra("step", 5)
                        putExtra("item", item)
                    })
                }

                4 -> {
                    startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                        putExtra("type", PetAddType.ADD.ordinal)
                        putExtra("step", 4)
                        putExtra("item", item)
                    })
                }

                3 -> {
                    startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                        putExtra("type", PetAddType.ADD.ordinal)
                        putExtra("step", 3)
                        putExtra("item", item)
                    })
                }

                2 -> {
                    startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                        putExtra("type", PetAddType.ADD.ordinal)
                        putExtra("step", 2)
                        putExtra("item", item)
                    })
                }

                1 -> {
                    startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                        putExtra("type", PetAddType.ADD.ordinal)
                        putExtra("step", 1)
                        putExtra("item", item)
                    })
                }
            }
        }
    }

    override fun onMatchFood(item: PetInfoItem) {
        Airbridge.trackEvent("mypage", "click", "customeal_start", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("동물정보_진단받기", "Click Event", "반려동물 정보 페이지 내 진단받기 클릭")
        startActivity(Intent(requireActivity(), MatchFoodActivity::class.java).apply {
            putExtra("item", item)
        })
    }

    override fun onMatchFoodResult(item: PetInfoItem) {
        Airbridge.trackEvent("mypage", "click", "customeal_result", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("동물정보_진단결과", "Click Event", "반려동물 정보 페이지 내 맞춤식 진단결과 클릭")
        foodRecommendDataModel.petInfo.value = item
        try {
            findNavController().navigate(PetInformationFragmentDirections.actionPetInformationFragmentToMyPetFoodRecommendResult())
        } catch (ise: IllegalStateException) {
            context?.startActivity<MatchFoodResultActivity> {
                putExtras(bundleOf("type" to "activity", "item" to item))
            }
        }
    }

    override fun onPurchase(item: PetInfoItem) {
        foodRecommendDataModel.petInfo.value = item
        foodRecommendDataModel.isPurchase.value = true
        try {
            findNavController().navigate(PetInformationFragmentDirections.actionPetInformationFragmentToMyPetFoodRecommendResult())
        } catch (ise: IllegalStateException) {
            context?.startActivity<MatchFoodResultActivity>() {
                putExtras(bundleOf("type" to "activity", "item" to item, "isPurchase" to true))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (mApiClient.isRequestRunning(PET_DELETE_REQUEST)) {
            mApiClient.cancelRequest(PET_DELETE_REQUEST)
        }
    }

    override fun onEditMode(isMode: Boolean) {
        btnAdd.apply {
            when {
                isMode == true -> {
                    visibility = View.GONE

                    itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(mAdapter))
                    itemTouchHelper!!.attachToRecyclerView(petInformationList)
                    mAdapter.setDragListener(this@PetInformationFragment)
                }
                else -> visibility = View.VISIBLE
            }
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun onItemDelete(position: Int) {
        Logger.d("delete position : $position")
        deletePosition = position
        val item = mAdapter.getItem(position)

        TwoBtnDialog(requireContext()).apply {
            setTitle("반려동물 삭제")
            setMessage("나의 반려동물 기록이 모두 삭제되며, 복구가 불가능합니다. 정말 삭제하시겠습니까?")
            setConfirmButton("삭제", View.OnClickListener {
                deletePosition = position
                mApiClient.deletePet(PET_DELETE_REQUEST, item.id)
                dismiss()
            })
            setCancelButton("취소", View.OnClickListener {
                mAdapter.notifyItemChanged(position)
                dismiss()
            })
        }.show()

//        val petName = Helper.getCompleteWordByJongsung(item.name, "이가", "가")
//        OneBtnDialog(requireContext()).apply {
//            setTitle("반려동물 삭제")
//            setMessage("${petName} 이용 중인 서비스가\n있습니다. 이용완료 후 삭제가\n가능합니다.")
//            setConfirmButton("확인", View.OnClickListener {
//                dismiss()
//            })
//        }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_AUTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    requireActivity().finish()
                }
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
    fun onEventMainThread(event: NetworkBusResponse) {
        when(event.tag) {
            PET_DELETE_REQUEST -> {
                if ("ok" == event.status) {
                    Logger.d("response : ${event.response}")
                    mAdapter.removeItem(deletePosition)

                    if (mAdapter.itemCount < 1) {
                        updateViewVisibility(isEmpty = true)
                    }
                }
            }

            PET_CHANGE_LIST_REQUEST -> {
                if ("ok" == event.status) {
                    mAdapter.setEditMode(false)
                    loadPetInfo()
                }
            }
        }
    }

    private fun loadPetInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository by inject<PetdocRepository>()
            val oldUserId = if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
            } else {
                0
            }
            val items = repository.retrieveMyPets(oldUserId)
            PetdocApplication.mPetInfoList.clear()
            PetdocApplication.mPetInfoList.addAll(items)
            if (items.isNotEmpty()) {
                updateViewVisibility(isEmpty = false)
                mAdapter.updateData(items)
                mAdapter.setEditMode(false)
            } else {
                updateViewVisibility(isEmpty = true)
            }
        }
    }

    private fun updateViewVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            petInformationEmpty.visibility = View.VISIBLE
            petInformationList.visibility = View.GONE
            btnAdd.visibility = View.VISIBLE
        } else {
            petInformationEmpty.visibility = View.GONE
            petInformationList.visibility = View.VISIBLE
        }
    }

    //=============================================================================================

    private fun calculateBirthday(item:PetInfoItem) : String {
        val year = item.birthday!!.split("-")[0]
        val month = item.birthday!!.split("-")[1]
        val day = item.birthday!!.split("-")[2]
        val period = LocalDate.of(year.toInt(), month.toInt(), day.toInt()).until(LocalDate.now())

        if (year != "1900") {
            petInfoModel.petYear.value = period.years.toString()
            petInfoModel.petMonth.value = period.months.toString()

            return "${period.years}세${period.months}개월"
        }

        return ""
    }

    private fun getKindKey(kind: String) =
        when (kind) {
            "강아지" -> "dog"
            "고양이" -> "cat"
            "햄스터" -> "hamster"
            "토끼" -> "rabbit"
            "고슴도치" -> "hedgehog"
            "거북이" -> "turtle"
            "기니피그" -> "guinea_pig"
            "조류" -> "bird"
            "패릿" -> "ferret"
            else -> ""
        }

    //==============================================================================================
    inner class ItemTouchHelperCallback(val listener: ItemActionListener) : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.DOWN or ItemTouchHelper.UP
            val swipeFlags = ItemTouchHelper.LEFT
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            listener.onItemMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            listener.onItemSwiped(viewHolder.bindingAdapterPosition)
        }

        override fun isLongPressDragEnabled(): Boolean = false

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {

            RecyclerViewSwipeDecorator.Builder(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
                .addSwipeLeftBackgroundColor(Helper.readColorRes(R.color.orange))
                .addSwipeLeftLabel("삭제")
                .setSwipeLeftLabelColor(Helper.readColorRes(R.color.white))
                .setSwipeLeftLabelTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f)
                .setSwipeLeftLabelTypeface(Typeface.DEFAULT_BOLD)
                .addSwipeRightBackgroundColor(Helper.readColorRes(R.color.periwinkleBlueTwo))
                .create()
                .decorate()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}