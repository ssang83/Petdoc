package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_hospital.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel

/**
 * Petdoc
 * Class: HospitalActivity
 * Created by kimjoonsung on 2020/06/08.
 *
 * Description :
 */
class HospitalActivity : AppCompatActivity() {

    private lateinit var dataModel:HospitalDataModel

    private var latitude = ""
    private var longitude = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital)
        dataModel = ViewModelProvider(this).get(HospitalDataModel::class.java)

        latitude = intent?.getStringExtra("latitude")!!
        longitude = intent?.getStringExtra("longitude")!!

        dataModel.currentLat.value = latitude
        dataModel.currentLng.value = longitude
    }

    override fun onBackPressed() {
        if(navHospitalFragment.childFragmentManager?.backStackEntryCount == 0){
            finish()
        }else super.onBackPressed()
    }
}