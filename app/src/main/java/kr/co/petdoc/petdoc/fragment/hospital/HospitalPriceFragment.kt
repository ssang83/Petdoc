package kr.co.petdoc.petdoc.fragment.hospital

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_price_detail_item.view.*
import kotlinx.android.synthetic.main.fragment_hospital_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.submodel.PriceItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: HospitalPriceFragment
 * Created by kimjoonsung on 2020/10/05.
 *
 * Description :
 */
class HospitalPriceFragment : Fragment() {

    private lateinit var priceAdapter: PriceAdapter
    private var priceItems:MutableList<PriceItem> = mutableListOf()

    private lateinit var dataModel: HospitalDataModel

    private var margin16 = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_hospital_price, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        margin16 = Helper.convertDpToPx(requireContext(), 16f).toInt()

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        if (arguments != null) {
            priceItems = requireArguments().getParcelableArrayList<PriceItem>("priceItems")!!.toMutableList()
        } else {
            priceItems = dataModel.priceItems.value!!
        }

        priceAdapter = PriceAdapter()
        priceList.apply { adapter = priceAdapter }
    }

    //==============================================================================================
    inner class PriceAdapter : RecyclerView.Adapter<PriceHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PriceHolder(layoutInflater.inflate(R.layout.adapter_price_detail_item, parent, false))

        override fun onBindViewHolder(holder: PriceHolder, position: Int) {
            if (position != 0) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    topMargin = margin16
                }
            }

            holder.setItem(priceItems[position].name!!)
            holder.setPrice(Helper.ToPriceFormat(priceItems[position].price))
        }

        override fun getItemCount() = priceItems.size
    }

    inner class PriceHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setItem(_item: String) {
            item.clinicItem.text = _item
        }

        fun setPrice(_price: String) {
            item.clinicPrice.text = "${_price}원"
        }
    }
}