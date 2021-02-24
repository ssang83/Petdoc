package kr.co.petdoc.petdoc.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.hospital.HospitalActivity
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel

/**
 * Petdoc
 * Class: HospitalFragment
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : 병원 찾기 화면
 */
class HospitalFragment : Fragment() {

    private lateinit var dataModel: HospitalDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(this).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("latitude : ${dataModel.currentLat.value.toString()}, longitude : ${dataModel.currentLng.value.toString()}")

        startActivity(Intent(requireActivity(), HospitalActivity::class.java).apply {
            putExtra("latitude", "0")
            putExtra("longitude", "0")
        })
    }
}