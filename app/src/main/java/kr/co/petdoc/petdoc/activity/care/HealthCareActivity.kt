package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import androidx.navigation.findNavController
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.fragment.care.HealthCareFragmentDirections

/**
 * Petdoc
 * Class: HealthCareActivity
 * Created by kimjoonsung on 2020/09/09.
 *
 * Description :
 */
class HealthCareActivity : BaseActivity() {

    private var fromMy = false
    private var fromPurchase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_care)
        mActivityList.add(this)

        fromMy = intent?.getBooleanExtra("fromMy", fromMy)!!
        fromPurchase = intent?.getBooleanExtra("fromPurchase", fromPurchase)!!

        if (fromMy) {
            findNavController(R.id.navHostFragment).navigate(HealthCareFragmentDirections.actionHealthCareFragmentToHealthCareHospitalFragment())
        } else if (fromPurchase) {
            findNavController(R.id.navHostFragment).navigate(HealthCareFragmentDirections.actionHealthCareFragmentToPurchaseFragment())
        }
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.navHostFragment)
        if (fromMy.not() && fromPurchase.not()) {
            navController.let {
                if(it.currentDestination?.id == R.id.purchase_complete_fragment ||
                    it.currentDestination?.id == R.id.healthCareFragment) {
                    finish()
                } else super.onBackPressed()
            }
        } else {
            navController.let{
                if (it.currentDestination?.id == R.id.healthCareHospitalFragment || it.currentDestination?.id == R.id.purchase_fragment) {
                    finish()
                    return
                }
            }

            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivityList.remove(this)      // 정상종료인 경우 리스트에서 제거
    }
}