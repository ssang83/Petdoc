package kr.co.petdoc.petdoc.fragment.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_care_pet_footer.view.*
import kotlinx.android.synthetic.main.adapter_care_pet_item.view.*
import kotlinx.android.synthetic.main.fragment_chat_register.*
import kotlinx.android.synthetic.main.view_chat_category.view.*
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.activity.chat.ChatInfoActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.ChatCategoryItem
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject

/**
 * Petdoc
 * Class: ChatFragment
 * Created by kimjoonsung on 2020/05/19.
 *
 * Description :
 */
class ChatRegisterFragment : BaseFragment() {

    private val LOGTAG = "ChatRegisterFragment"

    private var margin20 = 0
    private var margin4 = 0

    private lateinit var petAdapter:PetAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private var chatCategoryItems:MutableList<ChatCategoryItem> = mutableListOf()

    private var selectedPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseAPI(requireActivity()).logEventFirebase("상담_상담정보등록", "Page View", "상담정보등록 페이지뷰")

        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin4 = Helper.convertDpToPx(requireContext(), 4f).toInt()

        if (PetdocApplication.mChatCategoryItem != null) {
            PetdocApplication.mChatCategoryItem = null
        }

        //============================================================================================
        btnLogin.setOnSingleClickListener {
            startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
        }

        btnCompleted.setOnSingleClickListener {
            if (PetdocApplication.mChatCategoryItem != null && PetdocApplication.mPetInfoItem != null) {
                Airbridge.trackEvent("counsel", "click", "counsel_start", null, null, null)
                startActivity(Intent(requireActivity(), ChatInfoActivity::class.java).apply {
                    putExtra("petItem", PetdocApplication.mPetInfoItem)
                })
            } else if (PetdocApplication.mPetInfoItem == null) {
                AppToast(requireContext()).showToastMessage(
                    "반려동물을 등록해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            } else {
                AppToast(requireContext()).showToastMessage(
                    "상담 분류를 선택해주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }
        }

        //==========================================================================================
        petAdapter = PetAdapter()
        petList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            addItemDecoration(HorizontalMarginItemDecoration(
                    marginStart = margin20,
                    marginBetween = margin4,
                    marginEnd = margin20
            ))

            adapter = petAdapter
        }

        categoryAdapter = CategoryAdapter()
        categoryList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }

            adapter = categoryAdapter
        }

    }

    override fun onResume() {
        super.onResume()
        if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
            layoutLogin.visibility = View.GONE
            layoutChatRequest.visibility = View.VISIBLE

            loadPetInfo()

            chatCategoryItems.clear()
            chatCategoryItems.addAll(PetdocApplication.mSearchCategoryList)
            categoryAdapter.notifyDataSetChanged()
        } else {
            layoutLogin.visibility = View.VISIBLE
            layoutChatRequest.visibility = View.GONE
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
            mPetInfoItems = items
            petAdapter.notifyDataSetChanged()

            if (mPetInfoItems.isNotEmpty()) {
                PetdocApplication.mPetInfoItem = mPetInfoItems[selectedPosition]
            } else {
                PetdocApplication.mPetInfoItem = null
            }
            updateCompleteBtnStateIfNeeded()
        }
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of Image list.
     *
     * @param imageListResponse ImageListResponse
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event:NetworkBusResponse) {}

    private fun onItemClicked(item: PetInfoItem) {
        PetdocApplication.mPetInfoItem = item
    }

    private fun onCategoryClicked(item: ChatCategoryItem) {
        PetdocApplication.mChatCategoryItem = item
        updateCompleteBtnStateIfNeeded()
    }

    private fun updateCompleteBtnStateIfNeeded() {
        if (PetdocApplication.mPetInfoItem != null &&
                PetdocApplication.mChatCategoryItem != null) {
            btnCompleted.apply {
                setTextColor(Helper.readColorRes(R.color.white))
            }
        }
    }

    //==============================================================================================
    private var mPetInfoItems: List<PetInfoItem> = listOf()

    inner class PetAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_ITEM -> PetHolder(layoutInflater.inflate(R.layout.adapter_care_pet_item, parent, false))
                else -> FooterHolder(layoutInflater.inflate(R.layout.adapter_chat_pet_footer, parent, false))
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                TYPE_ITEM -> {
                    holder.setIsRecyclable(false)

                    if (position == 0) {
                        holder.itemView.petCrown.visibility = View.VISIBLE
                    } else {
                        holder.itemView.petCrown.visibility = View.GONE
                    }

                    if (selectedPosition == position) {
                        holder.itemView.border.setBackgroundResource(R.drawable.orange_circle_stroke)
                    } else {
                        holder.itemView.border.setBackgroundResource(R.drawable.grey_full_round_stroke_rect)
                    }

                    (holder as PetHolder).setName(mPetInfoItems[position].name!!)
                    holder.setImage(mPetInfoItems[position].imageUrl!!)

                    holder.itemView.setOnClickListener {
                        onItemClicked(mPetInfoItems[position])
                        selectedPosition = position
                        notifyDataSetChanged()
                    }
                }
                else -> {
                    (holder as FooterHolder).itemView.layoutAdd.setOnSingleClickListener {
                        if (StorageUtils.loadBooleanValueFromPreference(
                                requireContext(),
                                AppConstants.PREF_KEY_LOGIN_STATUS
                            )
                        ) {
                            if (StorageUtils.loadIntValueFromPreference(
                                    requireContext(),
                                    AppConstants.PREF_KEY_MOBILE_CONFIRM
                                ) == 1
                            ) {
                                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                                    putExtra("type", PetAddType.ADD.ordinal)
                                })
                            } else {
                                startActivity(Intent(requireActivity(), MobileAuthActivity::class.java))
                            }
                        } else {
                            startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
                        }
                    }
                }
            }
        }

        override fun getItemCount() =
            if (mPetInfoItems.size == 0) {
                1
            } else {
                mPetInfoItems.size + 1
            }

        override fun getItemViewType(position: Int): Int {
            if (mPetInfoItems.size == 0) {
                return TYPE_FOOTER
            } else {
                if (position == mPetInfoItems.size) {
                    return TYPE_FOOTER
                } else {
                    return TYPE_ITEM
                }
            }
        }
    }

    inner class PetHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setName(_name: String) {
            item.petName.text = _name
        }

        fun setImage(_url: String) {
            item.petImage.apply {
                when {
                    _url.isNotEmpty() -> {
                        Glide.with(requireContext())
                            .load( if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                            .apply(RequestOptions.circleCropTransform())
                            .into(item.petImage)
                    }

                    else -> setBackgroundResource(R.drawable.img_pet_profile_default)
                }
            }
        }
    }

    inner class FooterHolder(view:View) : RecyclerView.ViewHolder(view)

    //==============================================================================================
    inner class CategoryAdapter : RecyclerView.Adapter<CategoryHolder>() {
        var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CategoryHolder(layoutInflater.inflate(R.layout.view_chat_category, parent, false))

        override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
            if (selectedPosition == position) {
                holder.itemView.category.setTextColor(Helper.readColorRes(R.color.orange))
                holder.itemView.category.setBackgroundResource(R.drawable.orange_round_rect_6)
            } else {
                holder.itemView.category.setTextColor(Helper.readColorRes(R.color.dark_grey))
                holder.itemView.category.setBackgroundResource(R.drawable.lightbluegrey_round_rect_6)
            }

            holder.setCategory(chatCategoryItems[position].name)

            holder.itemView.setOnClickListener {
                Logger.d("position : $position")
                onCategoryClicked(chatCategoryItems[position])
                selectedPosition = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = chatCategoryItems.size
    }

    inner class CategoryHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setCategory(_category: String) {
            item.category.text = _category
        }
    }
}