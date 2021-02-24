package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.viewmodel.CareScanDataModel
import org.greenrobot.eventbus.EventBus

/**
 * Petdoc
 * Class: EarActivity
 * Created by kimjoonsung on 2020/05/18.
 *
 * Description :
 */
class EarActivity : BaseActivity() {

    private lateinit var dataModel: CareScanDataModel

    private var petId = 0
    private var petImage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ear)
        dataModel = ViewModelProvider(this).get(CareScanDataModel::class.java)

        petId = intent?.getIntExtra("petId", petId)!!
        petImage = intent?.getStringExtra("petImage")!!
        Logger.d("petId : $petId, petIamage : $petImage")

        dataModel.petId.value = petId
        dataModel.petImage.value = petImage

        scanner.reBindProcessWithScannerNetwork()
    }

    override fun onBackPressed() {
        findNavController(R.id.scanEarHost).let{
            if(it.currentDestination?.id == R.id.earFragment){
                EventBus.getDefault().post("Ear")
                finish()
                return
            }
        }
        super.onBackPressed()
    }
}