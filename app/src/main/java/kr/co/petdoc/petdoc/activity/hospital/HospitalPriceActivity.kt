package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.HospitalPriceFragment
import kr.co.petdoc.petdoc.network.response.submodel.PriceItem

/**
 * Petdoc
 * Class: HospitalPriceActivity
 * Created by kimjoonsung on 2020/10/05.
 *
 * Description :
 */
class HospitalPriceActivity : AppCompatActivity() {

    private var priceItems:MutableList<PriceItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_price)

        priceItems = intent?.getParcelableArrayListExtra("priceItems")!!

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HospitalPriceFragment().apply {
                val bundle = Bundle().apply {
                    putParcelableArrayList("priceItems", ArrayList(priceItems))
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
}