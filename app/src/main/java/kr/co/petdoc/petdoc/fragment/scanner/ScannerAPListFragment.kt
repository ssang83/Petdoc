package kr.co.petdoc.petdoc.fragment.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aramhuvis.lite.base.ApInfo
import kotlinx.android.synthetic.main.adapter_scanner_ap_list_item.view.*
import kotlinx.android.synthetic.main.fragment_scanner_ap_list.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.ConnectScannerActivity
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ConnectScannerDataModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog

/**
 * Petdoc
 * Class: ScannerAPListFragment
 * Created by kimjoonsung on 2020/05/06.
 *
 * Description :
 */
class ScannerAPListFragment : Fragment() {

    private var mAPItems:MutableList<ApInfo> = mutableListOf()
    private lateinit var mAdapter:APAdapter
    private lateinit var dataModel:ConnectScannerDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, fullscreen =  true)
        dataModel = ViewModelProvider(requireActivity()).get(ConnectScannerDataModel::class.java)
        return inflater.inflate(R.layout.fragment_scanner_ap_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle(Helper.readStringRes(R.string.care_scanner_close_title))
                setMessage(Helper.readStringRes(R.string.care_scanner_close_desc))
                setConfirmButton(Helper.readStringRes(R.string.care_scanner_close_confirm), View.OnClickListener {
                    dismiss()
                    requireActivity().finish()
                })
                setCancelButton(Helper.readStringRes(R.string.care_scanner_close_cancel), View.OnClickListener {
                    dismiss()
                })
            }.show()
        }

        btnRefresh.setOnClickListener {
            ConnectScannerActivity.instance.queryScan(ConnectScannerActivity.QUERY_STATUS.QUERY_STATUS_AP_LIST)
            Logger.d("주변 공유기 리프레쉬 중입니다.")
        }

        //==========================================================================================
        mAPItems = dataModel.apInfoList.value!!
        mAdapter = APAdapter()
        apList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

    }

    fun onItemClicked(position: Int) {
        dataModel.selectedAPInfo.value = mAPItems[position]
        findNavController().navigate(ScannerAPListFragmentDirections.actionScannerAPListFragmentToScannerWifiInputPasswdFragment())
    }

    //===============================================================================================
    inner class APAdapter : RecyclerView.Adapter<APHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            APHolder(layoutInflater.inflate(R.layout.adapter_scanner_ap_list_item, parent, false))

        override fun onBindViewHolder(holder: APHolder, position: Int) {
            holder.setWifiName(mAPItems[position].ssid)
            holder.setWifiStatus(mAPItems[position].encryption)

            holder.itemView.setOnClickListener {
                onItemClicked(position)
            }
        }

        override fun getItemCount() = mAPItems.size
    }

    inner class APHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setWifiName(_name: String) {
            item.wifiName.text = _name
        }

        fun setWifiStatus(_status: String) {
            if (_status == "none") {
                item.wifiStatus.setBackgroundResource(R.drawable.ic_wifi_default)
            } else {
                item.wifiStatus.setBackgroundResource(R.drawable.ic_wifi_password_default)
            }
        }
    }
}