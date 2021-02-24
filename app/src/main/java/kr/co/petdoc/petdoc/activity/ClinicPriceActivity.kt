package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: ClinicPriceActivity
 * Created by kimjoonsung on 2020/07/20.
 *
 * Description :
 */
class ClinicPriceActivity : AppCompatActivity() {

    lateinit var navHost: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_price)

        navHost = findNavController(R.id.navHostFragment)
    }

    override fun onBackPressed() {
        navHost.let {
            if(it.currentDestination?.id == R.id.clinicPriceFragment) {
                finish()
                return
            }
        }
        super.onBackPressed()
    }
}