package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_external_device_item.view.*
import kotlinx.android.synthetic.main.fragment_external_device.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils

/**
 * Petdoc
 * Class: ExternalDeviceFragment
 * Created by kimjoonsung on 2020/06/25.
 *
 * Description :
 */
class ExternalDeviceFragment : Fragment() {

    private lateinit var deviceAdapter:DeviceAdapter
    private var devicesItems:MutableList<String> = mutableListOf()

    private var connectStatus = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_external_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        deviceAdapter = DeviceAdapter()
        wifiDeviceList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }

        val device = StorageUtils.loadValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_PETDOC_SCAN_NAME,
            ""
        )

        connectStatus = StorageUtils.loadBooleanValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS
        )

        devicesItems.clear()
        devicesItems.add(device)
        deviceAdapter.notifyDataSetChanged()
    }

    private fun onItemClicked(device: String) {
        bundleOf("name" to device).let {
            findNavController().navigate(R.id.action_externalDeviceFragment_to_externalDeviceDetailFragment, it)
        }
    }

    //==============================================================================================
    inner class DeviceAdapter : RecyclerView.Adapter<DeviceHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DeviceHolder(layoutInflater.inflate(R.layout.adapter_external_device_item, parent, false))

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            holder.setName(devicesItems[position])

            if (devicesItems[position].isNotEmpty()) {
                holder.itemView.setOnClickListener { onItemClicked(devicesItems[position]) }
            }
        }

        override fun getItemCount() = if(devicesItems.size == 0) {
            1
        } else {
            devicesItems.size
        }
    }

    inner class DeviceHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setName(_name: String) {
            item.deviceName.apply {
                when {
                    _name.isEmpty() -> {
                        text = Helper.readStringRes(R.string.no_device)
                        setTextColor(Helper.readColorRes(R.color.light_grey3))
                        item.layoutDevice.visibility = View.GONE
                    }

                    else -> {
                        text = _name
                        setTextColor(Helper.readColorRes(R.color.dark_grey))
                        item.layoutDevice.visibility = View.VISIBLE

                        if (connectStatus) {
                            item.deviceStatus.visibility = View.VISIBLE
                        } else {
                            item.deviceStatus.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }
}

/*
package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_external_device_item.view.*
import kotlinx.android.synthetic.main.fragment_external_device.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils

class ExternalDeviceFragment : Fragment() {

    private lateinit var deviceAdapter: DeviceAdapter
    private var devicesItems: MutableList<String> = mutableListOf()

    private var connectStatus = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_external_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        deviceAdapter = DeviceAdapter()
        wifiDeviceList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }

        val device = StorageUtils.loadValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_PETDOC_SCAN_NAME,
            ""
        )

        connectStatus = StorageUtils.loadBooleanValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS
        )

        devicesItems.clear()
        devicesItems.add(device)
        deviceAdapter.notifyDataSetChanged()
    }

    inner class DeviceAdapter : RecyclerView.Adapter<DeviceHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DeviceHolder(layoutInflater.inflate(R.layout.adapter_external_device_item, parent, false))

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            holder.bind(devicesItems[position])
        }

        override fun getItemCount() = if(devicesItems.size == 0) {
            1
        } else {
            devicesItems.size
        }
    }

    inner class DeviceHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var device: String

        init {
            itemView.setOnClickListener {
                bundleOf("name" to device).let {
                    findNavController().navigate(R.id.action_externalDeviceFragment_to_externalDeviceDetailFragment, it)
                }
            }
        }

        fun bind(device: String) {
            this.device = device
            itemView.apply {
                when {
                    device.isEmpty() -> {
                        tvDeviceName.text = Helper.readStringRes(R.string.no_device)
                        tvDeviceName.setTextColor(Helper.readColorRes(R.color.light_grey3))
                        tvDeviceStatus.visibility = View.GONE
                        ivArrow.visibility = View.GONE
                    }

                    else -> {
                        tvDeviceName.text = device
                        tvDeviceName.setTextColor(Helper.readColorRes(R.color.light_grey3))
                        tvDeviceName.setTextColor(Helper.readColorRes(R.color.dark_grey))
                        ivArrow.visibility = View.VISIBLE
                        if (connectStatus) {
                            tvDeviceStatus.visibility = View.VISIBLE
                        } else {
                            tvDeviceStatus.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }
}*/
