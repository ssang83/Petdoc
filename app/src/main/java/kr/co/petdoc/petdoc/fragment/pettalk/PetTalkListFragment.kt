package kr.co.petdoc.petdoc.fragment.pettalk

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_pettalk_list_item.view.*
import kotlinx.android.synthetic.main.fragment_pettalk_detail.*
import kotlinx.android.synthetic.main.fragment_pettalk_list.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.PetTalkLiveModel
import kr.co.petdoc.petdoc.activity.life.PetTalkUploadActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetTalkItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PetTalkListFragment(var currentCategory:String="") : BaseFragment() {

    val PETTALK_LIST_REQUEST_TAG = "pettalk.list.call"

    val DAY =  24 * 1000 * 60 * 60
    val HOUR = 1000 * 60 * 60
    val MIN = 1000 * 60
    val SEC = 1000

    private var yearFormat = SimpleDateFormat("yyyy", Locale.KOREA)

    private var pettalkViewModel : PetTalkLiveModel? = null

    private var page = 0
    private val offset = 20
    private var triggerPoint = Int.MAX_VALUE
    private var isReload = false

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_pettalk_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        pettalkViewModel = ViewModelProvider(requireActivity()).get(PetTalkLiveModel::class.java)
        currentCategory = pettalkViewModel?.choice_category?.value ?: ""

        when(pettalkViewModel?.jump_mode?.value){
            "detail" -> {
                // 현재 프래그먼트가 네비게이션 설정상 기본 호스트이지만, 여기서 세부 화면으로 연결할 수 있음.
                findNavController().navigate(PetTalkListFragmentDirections.actionListToDetailWithNostack())
                return
            }
            else -> {}
        }

        pettalk_list_id.apply{
            adapter = ReferenceBookAdapter()
            layoutManager = LinearLayoutManager(requireContext())

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    pettalk_list_id.layoutManager?.let { lm ->
                        val last = (lm as LinearLayoutManager).findLastVisibleItemPosition()
                        if (last > triggerPoint) {
                            isReload = true
                            triggerPoint = Int.MAX_VALUE
                            mApiClient.getPetTalkList(PETTALK_LIST_REQUEST_TAG, currentCategory, offset, page * offset)
                        }

                        val visibleCount = lm.findFirstVisibleItemPosition()
                        if (visibleCount > 10) {
                            pettalk_move_to_top_button.visibility = View.VISIBLE
                        } else {
                            pettalk_move_to_top_button.visibility = View.GONE
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }
            })
        }

        pettalk_move_to_top_button.setOnClickListener {
            pettalk_list_id.smoothScrollToPosition(0)
        }

        btnMyPage.setOnClickListener { requireActivity().onBackPressed() }
        updateCategoryToolbar()


        pettalk_all_image.setOnClickListener {
            currentCategory = ""
            updateCategoryToolbar(true)
        }
        pettalk_tag_1_image.setOnClickListener {
            currentCategory = "dog"
            updateCategoryToolbar(true)
        }
        pettalk_tag_2_image.setOnClickListener {
            currentCategory = "cat"
            updateCategoryToolbar(true)
        }
        pettalk_tag_3_image.setOnClickListener {
            currentCategory = "hamster"
            updateCategoryToolbar(true)
        }
        pettalk_tag_4_image.setOnClickListener {
            currentCategory = "hedgehog"
            updateCategoryToolbar(true)
        }
        pettalk_tag_5_image.setOnClickListener {
            currentCategory = "etc"
            updateCategoryToolbar(true)
        }
        pettalk_tag_6_image.setOnClickListener {
            currentCategory = "flea"
            updateCategoryToolbar(true)
        }
        pettalk_history_image.setOnClickListener {
            currentCategory = "before"
            updateCategoryToolbar(true)
        }

        pettalk_write_button.setOnClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_LOGIN_STATUS
                )
            ) {
                startActivity(Intent(requireActivity(), PetTalkUploadActivity::class.java))
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        retryIfNetworkAbsent {
            page = 0
            isReload = false
            mApiClient.getPetTalkList(PETTALK_LIST_REQUEST_TAG, currentCategory, offset, page*offset)
        }
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(PETTALK_LIST_REQUEST_TAG)) {
            mApiClient.cancelRequest(PETTALK_LIST_REQUEST_TAG)
        }
        super.onDestroyView()
    }


    // =============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: NetworkBusResponse) {
        when(data.tag) {
            PETTALK_LIST_REQUEST_TAG -> {
                if (data.status == "ok") {
                    val petTalkItems: MutableList<PetTalkItem> = Gson().fromJson(data.response, object : TypeToken<MutableList<PetTalkItem>>() {}.type)
                    if (petTalkItems.size > 0) {
                        if (!isReload) {
                            petTalkList.clear()
                            petTalkList.addAll(petTalkItems)
                            pettalk_list_id.adapter?.notifyDataSetChanged()
                        } else {
                            for (item in petTalkItems) {
                                petTalkList.add(item)
                                pettalk_list_id.adapter?.notifyItemInserted(petTalkList.size - 1)
                            }
                        }

                        ++page
                        triggerPoint = petTalkList.size - 6
                    }

                    updateCategoryToolbar(false)
                } else {
                    Logger.d("code : ${data.code}, response : ${data.response}")
                }
            }

        }

    }

    private fun updateCategoryToolbar(reload:Boolean = false){
        if(reload){
            page = 0
            triggerPoint = offset - 6
            petTalkList.clear()
            pettalk_list_id.adapter?.notifyDataSetChanged()

            mApiClient.getPetTalkList(PETTALK_LIST_REQUEST_TAG, currentCategory, offset, page*offset)
        }


        val darkGrey = Helper.readColorRes(R.color.dark_grey)

        pettalk_all_image.setImageResource(R.drawable.pettalk_all)
        pettalk_all_text.setTextColor(darkGrey)
        pettalk_tag_1_image.setImageResource(R.drawable.pettalk_dog)
        pettalk_tag_1_text.setTextColor(darkGrey)
        pettalk_tag_2_image.setImageResource(R.drawable.pettalk_cat)
        pettalk_tag_2_text.setTextColor(darkGrey)
        pettalk_tag_3_image.setImageResource(R.drawable.pettalk_hamster)
        pettalk_tag_3_text.setTextColor(darkGrey)
        pettalk_tag_4_image.setImageResource(R.drawable.pettalk_hedgehog)
        pettalk_tag_4_text.setTextColor(darkGrey)
        pettalk_tag_5_image.setImageResource(R.drawable.pettalk_special)
        pettalk_tag_5_text.setTextColor(darkGrey)
        pettalk_tag_6_image.setImageResource(R.drawable.pettalk_market)
        pettalk_tag_6_text.setTextColor(darkGrey)
        pettalk_history_image.setImageResource(R.drawable.pettalk_history)
        pettalk_history_text.setTextColor(darkGrey)

        when(currentCategory){
            "dog" -> {
                pettalk_tag_1_image.setImageResource(R.drawable.pettalk_dog_on)
                pettalk_tag_1_text.setTextColor(Helper.readColorRes(R.color.dog))
            }
            "cat" -> {
                pettalk_tag_2_image.setImageResource(R.drawable.pettalk_cat_on)
                pettalk_tag_2_text.setTextColor(Helper.readColorRes(R.color.cat))
            }
            "hamster" -> {
                pettalk_tag_3_image.setImageResource(R.drawable.pettalk_hamster_on)
                pettalk_tag_3_text.setTextColor(Helper.readColorRes(R.color.hasmter))
            }
            "hedgehog" -> {
                pettalk_tag_4_image.setImageResource(R.drawable.pettalk_hedgehog_on)
                pettalk_tag_4_text.setTextColor(Helper.readColorRes(R.color.hedgehog))
            }
            "etc" -> {
                pettalk_tag_5_image.setImageResource(R.drawable.pettalk_special_on)
                pettalk_tag_5_text.setTextColor(Helper.readColorRes(R.color.special))
            }
            "flea" -> {
                pettalk_tag_6_image.setImageResource(R.drawable.pettalk_market_on)
                pettalk_tag_6_text.setTextColor(Helper.readColorRes(R.color.market))
            }
            "before" -> {
                pettalk_history_image.setImageResource(R.drawable.pettalk_history_on)
                pettalk_history_text.setTextColor(Helper.readColorRes(R.color.before))
            }
            else -> {
                pettalk_all_image.setImageResource(R.drawable.pettalk_all_on)
                pettalk_all_text.setTextColor(Helper.readColorRes(R.color.all))
            }
        }
    }

    // =============================================================================================
    var petTalkList = ArrayList<PetTalkItem>()
    inner class ReferenceBookAdapter : RecyclerView.Adapter<PettalkListViewHolder>() {
        override fun onCreateViewHolder( parent: ViewGroup, viewType: Int ): PettalkListViewHolder {
            return PettalkListViewHolder(layoutInflater.inflate(R.layout.adapter_pettalk_list_item, parent, false))
        }

        override fun getItemCount(): Int = petTalkList.size

        override fun onBindViewHolder(holder: PettalkListViewHolder, position: Int) {
            holder.binding(petTalkList[position])
        }
    }

    inner class PettalkListViewHolder(var _view:View) : RecyclerView.ViewHolder(_view){
        fun binding(item:PetTalkItem){

            _view.setOnClickListener {
                bundleOf("petTalkId" to item.id).let {
                    FirebaseAPI(requireActivity()).logEventFirebase("펫톡_게시글상세", "Click Event", "펫톡 더보기 화면에서 상세 페이지를 클릭")
                    findNavController().navigate(R.id.action_list_to_detail, it)
                }
            }

            if(item.titleImage.isNullOrBlank()){
                _view.pettalk_list_item_image.visibility = View.GONE
            }else{
                _view.pettalk_list_item_image.visibility = View.VISIBLE
                _view.pettalk_list_item_image.layoutParams = _view.pettalk_list_item_image.layoutParams.apply{ width = Helper.convertDPResourceToPx(requireContext(), R.dimen.pettalk_list_item_image_size) }
                Glide.with(requireContext()).load( if(item.titleImage?.startsWith("http")) item.titleImage else "${AppConstants.IMAGE_URL}${item.titleImage}")
                        .transform(CenterCrop(), RoundedCorners(20))
                        .into(_view.pettalk_list_item_image)
            }

            _view.pettalk_list_item_title.text = item.title
            _view.pettalk_list_item_writer.apply {
                when {
                    item.nickName.isNullOrEmpty() -> {
                        visibility = View.GONE
                    }

                    else -> {
                        visibility = View.VISIBLE
                        text = item.nickName.trim()
                    }
                }
            }

            val createdAt = item.createdAt.replace("T", " ")
            val createdDay = item.createdAt.split("T")
            val currentYear = yearFormat.format(System.currentTimeMillis())
            val date = createdDay[0].split("-")
            val duration = calculateTime(createdAt)
            _view.pettalk_list_item_date.apply {
                if (currentYear == date[0]) {
                    if(duration / DAY > 0) {
                        text = calculateDate(createdDay[0])
                    } else if (duration / HOUR > 0) {
                        text = "${duration/HOUR}시간전"
                    } else if (duration / MIN > 0) {
                        text = "${duration/MIN}분전"
                    } else {
                        text = "${duration/SEC}초전"
                    }
                } else {
                    text = calculateDateForYear(createdDay[0])
                }
            }
            _view.pettalk_list_item_reply_count.text = String.format("%s %d", Helper.readStringRes(R.string.reply), item.commentCount)
        }

        private fun calculateDate(date: String) : String {
            val regDate = date.split("-")
            return String.format("%d월%d일", regDate[1].toInt(), regDate[2].toInt())
        }

        private fun calculateDateForYear(date: String) : String {
            val regDate = date.split("-")
            return String.format("%d년%d월%d일", regDate[0].toInt(), regDate[1].toInt(), regDate[2].toInt())
        }

        private fun calculateTime(createdAt: String) : Long {
            val dateNow = Date(System.currentTimeMillis())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val fromDate = dateFormat.parse(createdAt)

            return dateNow.time - fromDate.time
        }
    }
}