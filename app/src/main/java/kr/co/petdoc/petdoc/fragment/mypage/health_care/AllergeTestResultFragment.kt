package kr.co.petdoc.petdoc.fragment.mypage.health_care

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_allerge_detail_item.view.*
import kotlinx.android.synthetic.main.fragment_allerge_test_result.*
import kotlinx.android.synthetic.main.layout_allerge_danger_0.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_1.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_2.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_3.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_4.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_5.view.*
import kotlinx.android.synthetic.main.layout_allerge_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.AllergyCommentActivity
import kr.co.petdoc.petdoc.activity.AllergeTestAboutActivity
import kr.co.petdoc.petdoc.activity.EXTRA_ALLERGY_COMMENT_CATEGORY
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.AllergeResultList
import kr.co.petdoc.petdoc.network.response.submodel.AllergeResultSubListItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.MyPageInformationModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * Petdoc
 * Class: AllergeTestResultFragment
 * Created by kimjoonsung on 2020/09/10.
 *
 * Description :
 */
class AllergeTestResultFragment : BaseFragment() {

    private val TAG = "AllergeTestResultFragment"
    private val HEALTH_CARE_RESULT_LIST = "$TAG.healthCareResultList"

    private lateinit var dataModel : MyPageInformationModel

    private lateinit var itemAdapter: ItemAdapter
    private lateinit var detailItemAdapter: DetailItemAdapter
    private var allergyItems: AllergeResultList? = null

    private var danger2List: List<String> = listOf()
    private var danger3List: List<String> = listOf()
    private var danger4List: List<String> = listOf()
    private var danger5List: List<String> = listOf()
    private var danger6List: List<String> = listOf()

    private var handler : Handler? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(MyPageInformationModel::class.java)
        return inflater.inflate(R.layout.fragment_allerge_test_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnTop.setOnClickListener { scrollView.scrollTo(0, 0) }
        btnBooking.setOnClickListener {
            startActivity(Intent(requireActivity(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("booking", "hospital")
            })

            requireActivity().finish()
        }
        btnAllergeTestDesc.setOnClickListener { requireContext().startActivity<AllergeTestAboutActivity>() }

        //==========================================================================================
        handler = Handler{
            fadeOutAndHideImage(guideImg)
            handler?.sendEmptyMessageDelayed(0, 3000)
            handler?.removeMessages(0)
            true
        }

        mApiClient.getHealthCareResultList(HEALTH_CARE_RESULT_LIST, dataModel.bookingId.value!!)
    }

    override fun onResume() {
        super.onResume()
        if(handler?.hasMessages(0)==false) handler?.sendEmptyMessageDelayed(0, 3000)
    }

    override fun onPause() {
        super.onPause()
        handler?.removeMessages(0)
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
            HEALTH_CARE_RESULT_LIST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    val resultData = JSONObject(event.response)["resultData"] as JSONObject
                    if ("SUCCESS" == code) {
                        try {
                            val dnaResult = resultData["dnsInspectionResult"] as JSONObject
                            val allergyResultList = dnaResult["allergyResultList"] as JSONArray
                            val danger2 = dnaResult["danger2"] as JSONArray
                            val danger3 = dnaResult["danger3"] as JSONArray
                            val danger4 = dnaResult["danger4"] as JSONArray
                            val danger5 = dnaResult["danger5"] as JSONArray
                            val danger6 = dnaResult["danger6"] as JSONArray

                            val items:AllergeResultList = Gson().fromJson(allergyResultList.toString(), object : TypeToken<AllergeResultList>() {}.type)
                            allergyItems = items

                            itemAdapter = ItemAdapter()
                            itemList.apply {
                                adapter = itemAdapter
                                isNestedScrollingEnabled = false
                                setHasFixedSize(false)
                            }

                            danger2List = List(danger2.length()) {
                                danger2.getString(it)
                            }

                            danger3List = List(danger3.length()) {
                                danger3.getString(it)
                            }

                            danger4List = List(danger4.length()) {
                                danger4.getString(it)
                            }

                            danger5List = List(danger5.length()) {
                                danger5.getString(it)
                            }

                            danger6List = List(danger6.length()) {
                                danger6.getString(it)
                            }

                            val count = danger3List.size + danger4List.size + danger5List.size + danger6List.size
                            SpannableStringBuilder(String.format(Helper.readStringRes(R.string.allerge_test_warning_count), count)).apply {
                                setSpan(ForegroundColorSpan(Helper.readColorRes(R.color.orange)), 20, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }.let {
                                warningCount.append(it)
                            }

                            setClass2Item()
                            if (danger3List.size > 0 || danger4List.size > 0 || danger5List.size > 0 || danger6List.size > 0) {
                                setWarningItem()
                            } else {
                                layoutWarningItem.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Logger.p(e)
                        }
                    } else {
                        Logger.d("error : $mesageKey")
                    }
                }
            }
        }
    }

    //===============================================================================================
    private fun fadeOutAndHideImage(imageView: AppCompatImageView) {
        val fadeOut: Animation = AlphaAnimation(1f, 0f).apply {
            interpolator = AccelerateInterpolator()
            duration = 1000
        }

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                imageView.setVisibility(View.GONE)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })

        imageView.startAnimation(fadeOut)
    }

    private fun setClass2Item() {
        if (danger2List.size > 0) {
            layClass2.visibility = View.VISIBLE

            tvClass2Count.text = danger2List.size.toString()
            tvClass2Title.text = "Class 2의 ${danger2List.size} 항목"
            tvClass2Items.apply {
                for (i in 0 until danger2List.size) {
                    append(danger2List[i])

                    if (i != danger2List.size - 1) {
                        append(", ")
                    }
                }
            }
        } else {
            layClass2.visibility = View.GONE
        }
    }

    private fun setWarningItem() {
        if(danger3List.size > 0) {
            layoutDanger3.visibility = View.VISIBLE

            dangerCount3.text = danger3List.size.toString()
            danger3.text = "Class 3의 ${danger3List.size} 항목"
            dangerItem3.apply {
                for (i in 0 until danger3List.size) {
                    append(danger3List[i])

                    if (i != danger3List.size - 1) {
                        append(", ")
                    }
                }
            }
        } else {
            layoutDanger3.visibility = View.GONE
        }

        if (danger4List.size > 0) {
            layoutDanger4.visibility = View.VISIBLE

            dangerCount.text = danger4List.size.toString()
            danger4.text = "Class 4의 ${danger4List.size} 항목"
            dangerItem.apply {
                for (i in 0 until danger4List.size) {
                    append(danger4List[i].replace(",", "/"))

                    if (i != danger4List.size - 1) {
                        append(", ")
                    }
                }
            }
        } else {
            layoutDanger4.visibility = View.GONE
        }

        if (danger5List.size > 0) {
            layoutDanger5.visibility = View.VISIBLE

            dangerCount1.text = danger5List.size.toString()
            danger5.text = "Class 5의 ${danger5List.size} 항목"
            dangerItem1.apply {
                for (i in 0 until danger5List.size) {
                    append(danger5List[i])

                    if (i != danger5List.size - 1) {
                        append(", ")
                    }
                }
            }
        } else {
            layoutDanger5.visibility = View.GONE
        }

        if (danger6List.size > 0) {
            layoutDanger6.visibility = View.VISIBLE

            dangerCount2.text = danger6List.size.toString()
            danger6.text = "Class 6의 ${danger6List.size} 항목"
            dangerItem2.apply {
                for (i in 0 until danger6List.size) {
                    append(danger6List[i])

                    if (i != danger6List.size - 1) {
                        append(", ")
                    }
                }
            }
        } else {
            layoutDanger6.visibility = View.GONE
        }
    }

    //===============================================================================================
    inner class ItemAdapter : RecyclerView.Adapter<ItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ItemHolder(layoutInflater.inflate(R.layout.layout_allerge_item, parent, false))

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            allergyItems?.let {
                holder.bind(it[position].get(0), position)
            }
        }

        override fun getItemCount() = allergyItems!!.size
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        var item: AllergeResultSubListItem? = null
        init {
            itemView.tvComment.setOnClickListener {
                requireContext().startActivity<AllergyCommentActivity> {
                    item?.let {
                        putExtra(EXTRA_ALLERGY_COMMENT_CATEGORY, it.smallCategory)
                    }
                }
            }
        }

        fun bind(item: AllergeResultSubListItem, position: Int) {
            this.item = item
            itemView.allergeItem.text = "${item.smallCategory} 항목"
            setDetail(item.smallCategory)
            setDetailList(position)
        }
        fun setDetail(_title: String) {
            itemView.allergeItem.text = "${_title} 항목"
        }

        fun setDetailList(position: Int) {
            detailItemAdapter = DetailItemAdapter(position)
            itemView.detailItemList.apply {
                adapter = detailItemAdapter
                isNestedScrollingEnabled = false
                setHasFixedSize(false)
            }
        }
    }

    //==================================================================================================
    inner class DetailItemAdapter(position: Int) : RecyclerView.Adapter<DetailItemHolder>() {
        private val itemPos = position

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DetailItemHolder(layoutInflater.inflate(R.layout.adapter_allerge_detail_item, parent, false))

        override fun onBindViewHolder(holder: DetailItemHolder, position: Int) {
            holder.itemView.root.apply {
                when {
                    position.rem(2) == 0 -> {
                        setBackgroundResource(R.color.white)
                    }

                    else -> setBackgroundResource(R.drawable.pale_grey_three_solid_round_rect_6)
                }
            }

            holder.setDetaiItem(allergyItems!![itemPos][position].itemNameKor)
            holder.setDetailDesc(allergyItems!![itemPos][position].itemNameEng)
            holder.setValue(allergyItems!![itemPos][position].resultRate)
            holder.setDanger(allergyItems!![itemPos][position].level.toInt(), allergyItems!![itemPos][position].percent)
        }

        override fun getItemCount() = allergyItems!![itemPos].size
    }

    inner class DetailItemHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setDetailDesc(_desc: String) {
            item.detailDesc.text = _desc
        }

        fun setDetaiItem(_item: String) {
            item.detailItem.text = _item
        }

        fun setValue(_value: String) {
            item.value.text = _value
        }

        fun setDanger(_step: Int, percent:Int) {
            item.dangerContainer.removeAllViews()
            when (_step) {
                0 -> {
                    val view = layoutInflater.inflate(R.layout.layout_allerge_danger_0, null)
                    item.dangerContainer.addView(view)
                    item.value.setTextColor(Helper.readColorRes(R.color.slateGrey))

                    view.with0.post {
                        val width = view.with0.width
                        view.layoutDanger0.post {
                            val margin = (view.layoutDanger0.width - width) * (percent / 100.0)
                            val param = (view.statusBar0.layoutParams as ViewGroup.MarginLayoutParams).apply {
                                leftMargin = margin.toInt()
                            }

                            view.statusBar0.layoutParams = param
                        }
                    }
                }

                1 -> {
                    val view = layoutInflater.inflate(R.layout.layout_allerge_danger_1, null)
                    item.dangerContainer.addView(view)
                    item.value.setTextColor(Helper.readColorRes(R.color.slateGrey))

                    view.width1.post {
                        val width = view.width1.width
                        view.layoutDanger1.post {
                            val margin = (view.layoutDanger1.width - width) * (percent / 100.0)
                            val param = (view.statusBar1.layoutParams as ViewGroup.MarginLayoutParams).apply {
                                leftMargin = margin.toInt()
                            }

                            view.statusBar1.layoutParams = param
                        }
                    }
                }

                2 -> {
                    val view = layoutInflater.inflate(R.layout.layout_allerge_danger_2, null)
                    item.dangerContainer.addView(view)
                    item.value.setTextColor(Helper.readColorRes(R.color.goldenYellow))

                    view.width2.post {
                        val width = view.width2.width
                        view.layoutDanger2.post {
                            val margin = (view.layoutDanger2.width - width) * (percent / 100.0)
                            val param = (view.statusBar2.layoutParams as ViewGroup.MarginLayoutParams).apply {
                                leftMargin = margin.toInt()
                            }

                            view.statusBar2.layoutParams = param
                        }
                    }
                }

                3 -> {
                    val view = layoutInflater.inflate(R.layout.layout_allerge_danger_3, null)
                    item.dangerContainer.addView(view)
                    item.value.setTextColor(Helper.readColorRes(R.color.orange))

                    view.width3.post {
                        val width = view.width3.width
                        view.layoutDanger3.post {
                            val margin = (view.layoutDanger3.width - width) * (percent / 100.0)
                            val param = (view.statusBar3.layoutParams as ViewGroup.MarginLayoutParams).apply {
                                leftMargin = margin.toInt()
                            }

                            view.statusBar3.layoutParams = param
                        }
                    }
                }

                4 -> {
                    val view = layoutInflater.inflate(R.layout.layout_allerge_danger_4, null)
                    item.dangerContainer.addView(view)
                    item.value.setTextColor(Helper.readColorRes(R.color.orange))

                    view.width4.post {
                        val width = view.width4.width
                        view.layoutDanger4.post {
                            val margin = (view.layoutDanger4.width - width) * (percent / 100.0)
                            val param = (view.statusBar4.layoutParams as ViewGroup.MarginLayoutParams).apply {
                                leftMargin = margin.toInt()
                            }

                            view.statusBar4.layoutParams = param
                        }
                    }
                }

                5 -> {
                    val view = layoutInflater.inflate(R.layout.layout_allerge_danger_5, null)
                    item.dangerContainer.addView(view)
                    item.value.setTextColor(Helper.readColorRes(R.color.orange))

                    view.width5.post {
                        val width = view.width5.width
                        view.layoutDagner5.post {
                            val margin = (view.layoutDagner5.width - width) * (percent / 100.0)
                            val param = (view.statusBar5.layoutParams as ViewGroup.MarginLayoutParams).apply {
                                leftMargin = margin.toInt()
                            }

                            view.statusBar5.layoutParams = param
                        }
                    }
                }

                6 -> {
                    val view = layoutInflater.inflate(R.layout.layout_allerge_danger_6, null)
                    item.dangerContainer.addView(view)
                    item.value.setTextColor(Helper.readColorRes(R.color.orange))
                }
            }
        }
    }
}