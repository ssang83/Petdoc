package kr.co.petdoc.petdoc.fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_advertisement.view.*
import kotlinx.android.synthetic.main.adapter_reference_item.view.*
import kotlinx.android.synthetic.main.fragment_pet_life.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.MagazineDetailActivity
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.activity.PetTalkActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.activity.life.LifeMagazineActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.*
import kr.co.petdoc.petdoc.network.response.submodel.BannerItem
import kr.co.petdoc.petdoc.network.response.submodel.MagazineItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: PetLifeFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 펫 라이프 화면
 */
// sungmin added on 2020/04/17
class PetLifeFragment : BaseFragment() {

    private val LOGTAG = "PetLifeFragment"
    private val REQUEST_TAG = "$LOGTAG.request"
    private val LIFE_BANNER_LIST_REQUEST_TAG = "$LOGTAG.life_banner_list"
    private val LIFE_MAGAGINE_LIST_REQUEST_TAG = "$LOGTAG.life_magagine_list"
    private val LIFE_FAMOUS_PET_TALK_TAG = "$LOGTAG.life_famous_pettalk"

    private var advertisementAdapter : PetLifeAdvertisementAdapter? = null
    private var referenceBooksAdapter : ReferenceBookAdapter? = null

    private var showRect = Rect()
    private var talkRect = Rect()
    private var bannerScrolling : Handler? = null
    private var petTalkChange : Handler? = null
    private var petTalkResponse : PetTalkResponse? = null
    private var petTalkPoint = 0

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_pet_life, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        life_reference_book_more.setOnClickListener {
            startActivity(Intent(requireActivity(), LifeMagazineActivity::class.java))
        }

        // --------------------------------------------------------------------------------------------
        advertisementAdapter = PetLifeAdvertisementAdapter()

        life_advertise_list.apply{

            var pageOffset = Helper.convertDPResourceToPx(requireContext(), R.dimen.life_top_col_side).toFloat()
            var pageMargin = Helper.convertDPResourceToPx(requireContext(), R.dimen.life_top_col_advertisement_gap).toFloat()

            layoutParams = layoutParams.apply{
                layoutParams.height = (Helper.screenSize(requireActivity())[0]-pageOffset.toInt()*2) * 150 / 343
            }

            adapter = advertisementAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL

            clipToPadding = false
            clipChildren = false
            setPadding(Helper.convertDPResourceToPx(context, R.dimen.life_top_col_side),0,Helper.convertDPResourceToPx(context, R.dimen.life_top_col_side),0)
            offscreenPageLimit = 4

            setPageTransformer { page, position ->
                when {
                    position >= 1 -> page.translationX = pageOffset - pageMargin
                    position <= -1 -> page.translationX = pageMargin - pageOffset
                    position >=0 && position<1 -> page.translationX = (pageOffset - pageMargin) * position
                    else -> page.translationX = (pageMargin-pageOffset) * (-position)
                }
            }
        }


        // --------------------------------------------------------------------------------------------
        referenceBooksAdapter = ReferenceBookAdapter()

        life_reference_book_list.apply{

            adapter = referenceBooksAdapter
            layoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(RefeneceBookDecoration())

            addOnScrollListener(object:RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(layoutManager is GridLayoutManager){
                        (layoutManager as GridLayoutManager).apply{
                            val lastItem = findLastVisibleItemPosition()
                            synchronized(pageTiggerPoint) {
                                if (lastItem > pageTiggerPoint) {
                                    pageTiggerPoint = Int.MAX_VALUE
                                    mApiClient.getMagazineList(LIFE_MAGAGINE_LIST_REQUEST_TAG, "createdAt", pageOffset, page*pageOffset)
                                }
                            }
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }
            })
        }

        life_coordinator_appbar_id.apply{
            addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, _ ->
                life_advertise_list.getGlobalVisibleRect(showRect)
                if(showRect.bottom<0) bannerScrolling?.removeMessages(0)
                else{
                    if(bannerScrolling?.hasMessages(0) == false) bannerScrolling?.sendEmptyMessageDelayed(0, 1000)
                }
                life_pettalk_panel.getGlobalVisibleRect(talkRect)
                if(talkRect.bottom<0) petTalkChange?.removeMessages(0)
                else{
                    if(petTalkChange?.hasMessages(0) == false) petTalkChange?.sendEmptyMessageDelayed(0, 1000)
                }
            })
        }


        btnMyPage.setOnClickListener {
            if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)){
                val mypageIntent = Intent(requireActivity(), MyPageActivity::class.java)
                requireActivity().startActivity(mypageIntent)
            }else{
                val loginIntent = Intent(requireActivity(), LoginAndRegisterActivity::class.java)
                requireActivity().startActivity(loginIntent)
            }
        }



        if(!mApiClient.isRequestRunning(LIFE_BANNER_LIST_REQUEST_TAG)) mApiClient.getBannerList(LIFE_BANNER_LIST_REQUEST_TAG, "life") // 현재 라이 배너가 나와지 않아서 메인배너로 호출
        if(!mApiClient.isRequestRunning(LIFE_MAGAGINE_LIST_REQUEST_TAG)) mApiClient.getMagazineList(LIFE_MAGAGINE_LIST_REQUEST_TAG, "createdAt", pageOffset, page*pageOffset)
        if(!mApiClient.isRequestRunning(LIFE_FAMOUS_PET_TALK_TAG)) mApiClient.getFamousPetTalk(LIFE_FAMOUS_PET_TALK_TAG)


        bannerScrolling = Handler{
                life_advertise_list.setCurrentItem(life_advertise_list.currentItem+1, true)
                bannerScrolling?.sendEmptyMessageDelayed(0, 3000)
            true
        }

        petTalkChange = Handler{

            ++petTalkPoint
            if(petTalkPoint>=petTalkResponse?.petTalkList?.size?:0) petTalkPoint = 0

            life_advertise_list_pettalk_tag_text.text = petTalkResponse?.petTalkList!![petTalkPoint].content
            life_advertise_list_pettalk_tag.text = String.format("#%s",
                when(petTalkResponse?.petTalkList!![petTalkPoint].type){
                    "" -> Helper.readStringRes(R.string.all)
                    "dog" -> Helper.readStringRes(R.string.dog)
                    "cat" -> Helper.readStringRes(R.string.cat)
                    "hamster" -> Helper.readStringRes(R.string.hamster)
                    "hedgehog" -> Helper.readStringRes(R.string.hedgehog)
                    "etc" -> Helper.readStringRes(R.string.special)
                    "flea" -> Helper.readStringRes(R.string.market)
                    "before" -> Helper.readStringRes(R.string.before)
                    else -> ""
                }
            )

            if(petTalkChange?.hasMessages(0)==false) petTalkChange?.sendEmptyMessageDelayed(0,3000)
            true
        }

        life_move_to_top_button.setOnClickListener {
            life_reference_book_list.scrollToPosition(0)
            life_coordinator_appbar_id.setExpanded(true, true)
        }


        life_advertise_list_pettalk_more.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "")}
            requireActivity().startActivity(pettalkIntent)
        }

        life_advertise_list_pettalk_all.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "")}
            requireActivity().startActivity(pettalkIntent)
        }

        life_advertise_list_pettalk_tag_1.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "dog")}
            requireActivity().startActivity(pettalkIntent)
        }

        life_advertise_list_pettalk_tag_2.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "cat")}
            requireActivity().startActivity(pettalkIntent)
        }

        life_advertise_list_pettalk_tag_3.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "hamster")}
            requireActivity().startActivity(pettalkIntent)
        }

        life_advertise_list_pettalk_tag_4.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "hedgehog")}
            requireActivity().startActivity(pettalkIntent)
        }

        life_advertise_list_pettalk_tag_5.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "etc")}
            requireActivity().startActivity(pettalkIntent)
        }

        life_advertise_list_pettalk_tag_6.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "flea")}
            requireActivity().startActivity(pettalkIntent)
        }

        life_advertise_list_pettalk_history.setOnClickListener {
            val pettalkIntent = Intent(requireActivity(), PetTalkActivity::class.java).apply{ putExtra("category", "before")}
            requireActivity().startActivity(pettalkIntent)
        }
    }


    override fun onResume() {
        super.onResume()
        if(bannerScrolling?.hasMessages(0)==false) bannerScrolling?.sendEmptyMessageDelayed(0, 3000)
        if(petTalkChange?.hasMessages(0)==false) petTalkChange?.sendEmptyMessageDelayed(0, 5000)
    }

    override fun onPause() {
        super.onPause()
        bannerScrolling?.removeMessages(0)
        petTalkChange?.removeMessages(0)
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
    fun onEventMainThread(response: AbstractApiResponse) {
        when(response.requestTag) {
            LIFE_MAGAGINE_LIST_REQUEST_TAG -> {
                if (response is MagazineListRespone) {
                    val magazineItem:MutableList<MagazineItem> = response.resultData
                    if(magazineItem.size>0) {
                        for(item in magazineItem){
                            magazineBox.add(item)
                            referenceBooksAdapter?.notifyItemInserted(magazineBox.size-1)
                        }
                        ++page
                        pageTiggerPoint = magazineBox.size - 4
                    }
                }
            }
            LIFE_BANNER_LIST_REQUEST_TAG -> {
                if( response is BannerListResponse ){
                    advertisementAdapter?.advertisementBag?.addAll(response.bannerList.toList())
                    advertisementAdapter?.notifyDataSetChanged()
                }
            }
            LIFE_FAMOUS_PET_TALK_TAG -> {
                if( response is PetTalkResponse ){
                    petTalkResponse = response
                    petTalkChange?.sendEmptyMessageDelayed(0,3000)
                }
            }
        }
    }

    /**
     * EventBus listener. An API call failed. No error message was returned.
     *
     * @param event ApiErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorEvent) {
        when(event.requestTag) {
            REQUEST_TAG -> {

            }
        }
    }

    /**
     * EventBus listener. An API call failed. An error message was returned.
     *
     * @param event ApiErrorWithMessageEvent Contains the error message.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorWithMessageEvent) {
        when(event.requestTag) {
            REQUEST_TAG -> {

            }
        }
    }

    private fun onItemClicked(item: MagazineItem) {
        startActivity(Intent(requireActivity(), MagazineDetailActivity::class.java).apply {
            putExtra("id", item.id)
        })

    }

    // ===========================================================================================================

    inner class PetLifeAdvertisementAdapter : RecyclerView.Adapter<PetLifeAdvertisementHolder>(){

        var advertisementBag = ArrayList<BannerItem>()

        override fun onCreateViewHolder( parent: ViewGroup, viewType: Int ): PetLifeAdvertisementHolder {
            return PetLifeAdvertisementHolder(layoutInflater.inflate(R.layout.adapter_advertisement, parent, false))
        }

        override fun getItemCount(): Int = advertisementBag.size * 1000

        override fun onBindViewHolder(holder: PetLifeAdvertisementHolder, position: Int) {
            holder.imagePrint(advertisementBag[position%advertisementBag.size].bannerImageUrl)
        }
    }


    inner class PetLifeAdvertisementHolder(var item:View) : RecyclerView.ViewHolder(item){
        fun imagePrint(imageUrl:String){
            Glide.with(requireContext()).load(imageUrl).apply(RequestOptions().transform(RoundedCorners(30))).into(item.advertisement_image_view)
        }
    }


    var magazineBox = ArrayList<MagazineItem>()
    var page = 0
    private val pageOffset = 20
    var pageTiggerPoint = Int.MAX_VALUE

    // ============================================================================================================
    inner class ReferenceBookAdapter : RecyclerView.Adapter<ReferenceBookHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferenceBookHolder {
            return ReferenceBookHolder(layoutInflater.inflate(R.layout.adapter_reference_item, parent, false))
        }

        override fun getItemCount(): Int = magazineBox.size

        override fun onBindViewHolder(holder: ReferenceBookHolder, position: Int) {
            holder.setCategoryText(magazineBox[position].categoryNm)
            holder.setContext(magazineBox[position].title)
            holder.setImage(magazineBox[position].titleImage)
            holder.setLikeStatus("false")

            holder.itemView.setOnClickListener { onItemClicked(magazineBox[position]) }
        }
    }

    inner class ReferenceBookHolder(var item:View) : RecyclerView.ViewHolder(item){

        fun setCategoryText(_text : String){
            item.recommend_life_type.text = _text
        }

        fun setContext(_text : String){
            item.recommend_life_title.text = _text
        }

        fun setImage(url:String){
            Glide.with(requireContext())
                .load(if(url.startsWith("http")) url else "https://s3.ap-northeast-2.amazonaws.com/elasticbeanstalk-ap-northeast-2-176213403491/media/$url")
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(item.recommend_life_image)
        }

        fun setLikeStatus(status:String) {
            item.recommend_life_like.apply {
                when {
                    "true" == status -> setBackgroundResource(R.drawable.like_image_on)
                    else -> setBackgroundResource(R.drawable.like_image_off)
                }
            }
        }
    }


    // =============================================================================================================
    inner class RefeneceBookDecoration : RecyclerView.ItemDecoration() {

        var space = 0

        init{
            space = Helper.convertDPResourceToPx(requireContext(),R.dimen.recommend_text_item_gaps).toInt()
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            //super.getItemOffsets(outRect, view, parent, state)
            outRect.left = space
            outRect.right = space
        }
    }
}