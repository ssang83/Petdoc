package kr.co.petdoc.petdoc.fragment.scanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.adapter_scan_image_item.view.*
import kotlinx.android.synthetic.main.fragment_scan_image_detail.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.chat.ChatHomeActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImageDB
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarItem
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.BitmapManager
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.TwoBtnDialog

/**
 * Petdoc
 * Class: ImageDetailFragment
 * Created by kimjoonsung on 2020/06/29.
 *
 * Description :
 */
class ImageDetailFragment : Fragment() {

    private lateinit var pagerAdapter: ImagePagerAdapter
    private var earItem:EarItem? = null

    private lateinit var earImageDB: EarImageDB

    private var imgUrl = ""

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_image_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        earImageDB = EarImageDB.getInstance(requireActivity())!!

        imgUrl = arguments?.getString("petImage")!!
        earItem = arguments?.getParcelable("item")!!
        Logger.d("petImage : $imgUrl, earItem : $earItem")

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
        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        layoutChat.setOnClickListener {
            startActivity(Intent(requireActivity(), ChatHomeActivity::class.java))
        }

        //==========================================================================================
        pagerAdapter = ImagePagerAdapter()
        imagePager.apply {
            adapter = pagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        TabLayoutMediator(indicator, imagePager) {tab, position ->  }.attach()

        //==========================================================================================

        petImage.apply {
            when {
                imgUrl.isNotEmpty() -> {
                    Glide.with(requireContext())
                        .load(if (imgUrl.startsWith("http")) imgUrl else "${AppConstants.IMAGE_URL}$imgUrl")
                        .apply(RequestOptions.circleCropTransform())
                        .into(petImage)
                }

                else -> setBackgroundResource(R.drawable.img_pet_profile_default)
            }
        }

        //===========================================================================================

        if (earItem != null) {
            date.text = "${earItem!!.year}년${earItem!!.month}월${earItem!!.day}일"
            time.text = earItem!!.time
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EarImageDB.destroyInstance()
    }

    //==============================================================================================
    private fun deleteImage() {
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            async { earImageDB.earImageDao().deleteValueById(earItem?.id!!) }.await()
            requireActivity().onBackPressed()
        }
    }

    //==============================================================================================
    inner class ImagePagerAdapter : RecyclerView.Adapter<ImageHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ImageHolder(layoutInflater.inflate(R.layout.adapter_scan_image_item, parent, false))

        override fun onBindViewHolder(holder: ImageHolder, position: Int) {
            holder.setBind(earItem!!, position)
        }

        override fun getItemCount() =
            if (earItem?.leftImage != null && earItem?.rightImage != null) {
                2
            } else {
                1
            }
    }

    inner class ImageHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setBind(earItem:EarItem, position: Int) {
            when (position) {
                0 -> {
                    if (earItem.leftImage != null) {
                        val image = BitmapManager.byteToBitmap(earItem.leftImage!!)
                        item.earImage.setImageBitmap(image)
                        item.earType.text = "왼쪽 귀 사진"
                    }
                }

                else -> {
                    if (earItem.rightImage != null) {
                        val image = BitmapManager.byteToBitmap(earItem.rightImage!!)
                        item.earImage.setImageBitmap(image)
                        item.earType.text = "오른쪽 귀 사진"
                    }
                }
            }
        }
    }
}