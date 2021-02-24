package kr.co.petdoc.petdoc.fragment.hospital

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_marker_duplicate_list_item.view.*
import kotlinx.android.synthetic.main.dialog_hospital_list.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem

/**
 * Petdoc
 * Class: HospitalListDialogFragment
 * Created by kimjoonsung on 2020/07/30.
 *
 * Description :
 */
class HospitalListDialogFragment(private val callback: CallbackListener) : AppCompatDialogFragment() {

    private lateinit var mAdapter:HospitalAdapter
    private var items:MutableList<HospitalItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialog)
    }

    override fun onStart() {
        super.onStart()

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        dialog?.window?.setLayout(width, height)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_hospital_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        items = PetdocApplication.mHospitalMarkerItems

        mAdapter = HospitalAdapter()
        hospitalList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    //==============================================================================================
    inner class HospitalAdapter : RecyclerView.Adapter<HospitalHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            HospitalHolder(layoutInflater.inflate(R.layout.adapter_marker_duplicate_list_item, parent, false))

        override fun onBindViewHolder(holder: HospitalHolder, position: Int) {
            holder.setMarkerType(items[position].bookingType)
            holder.setName(items[position].name)

            holder.itemView.setOnClickListener {
                callback.onSelectItem(items[position])
                dismiss()
            }
        }

        override fun getItemCount() = items.size
    }

    inner class HospitalHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setMarkerType(type: String?) {
            when (type) {
                BookingType.A.name,
                BookingType.R.name,
                BookingType.B.name -> {
                    item.markerType.setBackgroundResource(R.drawable.ic_reservation_marker)
                }

                else -> item.markerType.setBackgroundResource(R.drawable.blue_green_circle)
            }
        }

        fun setName(name: String) {
            item.hospitalName.text = name
        }
    }

    interface CallbackListener {
        fun onSelectItem(item:HospitalItem)
    }
}