package kr.co.petdoc.petdoc.fragment.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_album_pet_item.view.*
import kotlinx.android.synthetic.main.adapter_image_item.view.*
import kotlinx.android.synthetic.main.adapter_thumbnail_item.view.*
import kotlinx.android.synthetic.main.fragment_image_album.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.ImageDetailActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImage
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImageDB
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarItem
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.BitmapManager
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: ImageAlbumFragment
 * Created by kimjoonsung on 2020/06/25.
 *
 * Description :
 */
class ImageAlbumFragment : BaseFragment() {

    private val TAG = "ImageAlbumFragment"
    private val PET_INFO_LIST_REQUEST = "$TAG.petInfoListRequest"

    private lateinit var petAdapter: PetAdapter
    private var mPetInfoItems: MutableList<PetInfoItem> = mutableListOf()

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var thumbnialAdapter: ThumbnailAdapter
    private var earMap: MutableMap<String, MutableList<EarItem>> = mutableMapOf()
    private var earItems: MutableList<EarItem> = mutableListOf()

    private lateinit var earImageDB: EarImageDB

    private var fromMy = false

    private var margin20 = 0
    private var margin11 = 0

    private var petId = -1
    private var petImageUrl = ""

    private var deleteId = arrayListOf<Long>()

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fromMy = arguments?.getBoolean("fromMy") ?: fromMy
        petId = arguments?.getInt("petId") ?: petId
        petImageUrl = arguments?.getString("petImage") ?: petImageUrl
        Logger.d("fromMy : $fromMy, petId : $petId, petImage : $petImageUrl")

        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin11 = Helper.convertDpToPx(requireContext(), 11f).toInt()

        earImageDB = EarImageDB.getInstance(requireActivity())!!

        //==========================================================================================
        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnTrash.setOnClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle("사진 삭제")
                setMessage("촬영된 그룹 이미지를\n삭제하시겠습니까?")
                setConfirmButton("삭제", View.OnClickListener {
                    deleteImage()
                    dismiss()
                })
                setCancelButton("취소", View.OnClickListener {
                    dismiss()
                })
            }.show()
        }
        btnTop.setOnClickListener { imageList.scrollToPosition(0) }

        petAdapter = PetAdapter()
        petList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }

            adapter = petAdapter
        }

    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EarImageDB.destroyInstance()
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
            PET_INFO_LIST_REQUEST -> {
                if (event.status == "ok") {
                    mPetInfoItems = Gson().fromJson(event.response, object : TypeToken<MutableList<PetInfoItem>>() {}.type)
                    petAdapter.notifyDataSetChanged()

                    petId = mPetInfoItems[0].id
                    petImageUrl = mPetInfoItems[0].imageUrl!!

                    earMap.clear()
                    lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                        val items = earImageDB.earImageDao().loadAll(petId)
                        if (items.size > 0) {
                            async { convertDBData(items) }.await()
                        }

                        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                            if (items.size > 0) {
                                imageList.visibility = View.VISIBLE
                                emptyLayer.visibility = View.GONE

                                imageAdapter = ImageAdapter()
                                imageList.apply {
                                    adapter = imageAdapter

                                    layoutManager = LinearLayoutManager(requireContext())
                                }
                            } else {
                                imageList.visibility = View.GONE
                                emptyLayer.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }
    //==============================================================================================

    private fun onItemClicked(item: PetInfoItem) {
        petId = item.id
        petImageUrl = item.imageUrl!!

        earMap.clear()
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            val items = earImageDB.earImageDao().loadAll(petId)
            if (items.size > 0) {
                async { convertDBData(items) }.await()
            }

            lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                imageAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun onThumbnailClicked() {
        Logger.d("delete id : $deleteId")

        if (deleteId.size == 0) {
            btnTrash.visibility = View.GONE
            btnTop.visibility = View.VISIBLE
            thumbnialAdapter.setEditMode(false)
        } else {
            btnTrash.visibility = View.VISIBLE
            btnTop.visibility = View.GONE
        }
    }

    private fun deleteImage() {
        for (index in deleteId) {
            lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                async { earImageDB.earImageDao().deleteValueById(index) }.await()

                earMap.clear()
                val items = earImageDB.earImageDao().loadAll(petId)
                if (items.size > 0) {
                    async { convertDBData(items) }.await()
                }

                lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                    imageAdapter.notifyDataSetChanged()
                    thumbnialAdapter.setEditMode(false)
                    btnTrash.visibility = View.GONE
                    btnTop.visibility = View.VISIBLE

                    deleteId.clear()
                }
            }
        }
    }

    private fun convertDBData(items: List<EarImage>) {
        val earItems: MutableList<EarItem> = mutableListOf()
        for (i in 0 until items.size) {
            val item = EarItem()
            val key = "${items[i].year}년${items[i].month}월"

            if (i != 0) {
                val prevKey = "${items[i - 1].year}년${items[i - 1].month}월"
                if (key != prevKey) {
                    earItems.clear()
                }
            }

            item.id = items[i].id
            item.petId = items[i].petId
            item.year = items[i].year
            item.month = items[i].month
            item.day = items[i].day
            item.time = items[i].time
            item.leftImagePath = items[i].leftEarPath
            item.leftImage = items[i].leftEarImage
            item.rightImagePath = items[i].rightEarPath
            item.rightImage = items[i].rightEarImage

            earItems.add(item)

            earMap.put(key, earItems)
        }

        Logger.d("earMap : ${earMap.size}")
    }

    private fun onThumbnailDetail(item: EarItem) {
        if(fromMy) {
            val bundle = Bundle().apply {
                putString("petImage", petImageUrl)
                putParcelable("item", item)
            }

            findNavController().navigate(R.id.action_imageAlbumFragment_to_imageDetailFragment, bundle)
        } else {
            startActivity(Intent(requireActivity(), ImageDetailActivity::class.java).apply {
                putExtra("petImage", petImageUrl)
                putExtra("item", item)
            })
        }
    }

    private fun loadData() {
        if (fromMy) {
            petList.visibility = View.VISIBLE
            title.text = Helper.readStringRes(R.string.my_album)
            btnBack.setImageResource(R.drawable.ic_back)

            mApiClient.getPetInfoList(PET_INFO_LIST_REQUEST)
        } else {
            title.text = Helper.readStringRes(R.string.scanner_ear_image)
            petList.visibility = View.GONE
            btnBack.setImageResource(R.drawable.x_button)

            earMap.clear()
            lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                val items = earImageDB.earImageDao().loadAll(petId)
                if (items.size > 0) {
                    async { convertDBData(items) }.await()
                }

                lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                    if (items.size > 0) {
                        imageList.visibility = View.VISIBLE
                        emptyLayer.visibility = View.GONE

                        imageAdapter = ImageAdapter()
                        imageList.apply {
                            adapter = imageAdapter

                            layoutManager = LinearLayoutManager(requireContext())
                        }
                    } else {
                        imageList.visibility = View.GONE
                        emptyLayer.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    //==============================================================================================
    inner class PetAdapter : RecyclerView.Adapter<PetHolder>() {
        var selectedPosition = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PetHolder(layoutInflater.inflate(R.layout.adapter_album_pet_item, parent, false))

        override fun onBindViewHolder(holder: PetHolder, position: Int) {
            holder.setIsRecyclable(false)

            if (selectedPosition == position) {
                holder.itemView.border.visibility = View.VISIBLE
            } else {
                holder.itemView.border.visibility = View.GONE
            }

            holder.setName(mPetInfoItems[position].name!!)
            holder.setImage(mPetInfoItems[position].imageUrl!!)

            if (position == 0) {
                (holder.itemView.itemRoot.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin20
                }
            } else if (position == itemCount - 1) {
                (holder.itemView.itemRoot.layoutParams as RecyclerView.LayoutParams).apply {
                    rightMargin = margin20
                    leftMargin = margin11
                }
            } else {
                (holder.itemView.itemRoot.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin11
                }
            }

            holder.itemView.setOnClickListener {
                onItemClicked(mPetInfoItems[position])
                selectedPosition = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = mPetInfoItems.size
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
                            .load(if (_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                            .apply(RequestOptions.circleCropTransform())
                            .into(item.petImage)
                    }

                    else -> setBackgroundResource(R.drawable.img_pet_profile_default)
                }
            }
        }
    }

    //==============================================================================================
    inner class ImageAdapter : RecyclerView.Adapter<ImageHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ImageHolder(layoutInflater.inflate(R.layout.adapter_image_item, parent, false))

        override fun onBindViewHolder(holder: ImageHolder, position: Int) {
            earMap.forEach { (key, value) ->
                holder.setDate(key)

                earItems = value
                thumbnialAdapter = ThumbnailAdapter()
                holder.itemView.thumbnailList.apply {
                    layoutManager = GridLayoutManager(requireContext(), 2)
                    adapter = thumbnialAdapter
                }
            }
        }

        override fun getItemCount() = earMap.size
    }

    inner class ImageHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setDate(_date: String) {
            item.date.text = _date
        }
    }

    //==============================================================================================
    private var selectedPosition = -1
    private var isEditMode = false

    inner class ThumbnailAdapter : RecyclerView.Adapter<ThumbnailHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ThumbnailHolder(layoutInflater.inflate(R.layout.adapter_thumbnail_item, parent, false))

        override fun onBindViewHolder(holder: ThumbnailHolder, position: Int) {
            earItems[position].day?.let { holder.setDay(it) }
            holder.setThumbnail(earItems[position].leftImage, earItems[position].rightImage)

            holder.itemView.setOnLongClickListener {
                isEditMode = true

                holder.itemView.check.visibility = View.VISIBLE
                deleteId.add(earItems[position].id!!)

                selectedPosition = position
                onThumbnailClicked()

                true
            }

            holder.itemView.setOnClickListener {
                Logger.d("selectedPosition : $selectedPosition, postion : $position")
                if (isEditMode) {
                    holder.itemView.check.apply {
                        when {
                            visibility == View.VISIBLE -> {
                                visibility = View.GONE
                                deleteId.remove(earItems[position].id!!)
                            }

                            else -> {
                                visibility = View.VISIBLE
                                deleteId.add(earItems[position].id!!)
                            }
                        }
                    }

                    selectedPosition = position
                    onThumbnailClicked()
                } else {
                    onThumbnailDetail(earItems[position])
                }
            }
        }

        override fun getItemCount() = earItems.size

        fun setEditMode(status: Boolean) {
            isEditMode = status
        }
    }

    inner class ThumbnailHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setDay(_day: String) {
            item.day.text = "${_day}일"
        }

        fun setThumbnail(left: ByteArray?, right: ByteArray?) {
            if (left != null && right != null) {
                val image = BitmapManager.byteToBitmap(left)
                item.thumbnail.setImageBitmap(image)
            } else {
                if (left != null) {
                    val image = BitmapManager.byteToBitmap(left)
                    item.thumbnail.setImageBitmap(image)
                } else if (right != null) {
                    val image = BitmapManager.byteToBitmap(right)
                    item.thumbnail.setImageBitmap(image)
                }
            }
        }
    }
}