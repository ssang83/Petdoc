package kr.co.petdoc.petdoc.fragment.care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_health_care_guide.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: HealthCareGuideFragment
 * Created by kimjoonsung on 2020/09/09.
 *
 * Description :
 */
class HealthCareGuideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha=0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_health_care_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnClickListener { requireActivity().onBackPressed() }
//        btnGoTest.setOnClickListener { findNavController().navigate(HealthCareGuideFragmentDirections.actionHealthCareGuideFragmentToHealthCareCodeFragment()) }
    }
}