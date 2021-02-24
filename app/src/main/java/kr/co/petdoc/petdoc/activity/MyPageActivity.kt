package kr.co.petdoc.petdoc.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.mypage.MyPageMainFragmentDirections
import kr.co.petdoc.petdoc.fragment.mypage.health_care.MyHealthCareFragmentDirections
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.viewmodel.MyPageInformationModel

/**
 * petdoc-android
 * Class: MyPageActivity
 * Fixed by sungminkim on 2020/04/08.
 *
 * Description :  마이페이지 진입부터 네비게이터 묶음 전체 컨트롤
 */
class MyPageActivity : BaseActivity() {

    private lateinit var dataModel : MyPageInformationModel

    private var fromHome = false
    private var fromHealthCare = false
    private var fromBooking = false
    private var fromPurchase = false
    private var bookingId = 0
    private var petName = ""
    private var petImage = ""
    private var visitDate = ""
    private var resultDate = ""
    private var petId = ""
    private var centerName = ""
    private var deepLink = false
    private var result = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)
        mActivityList.add(this)

        dataModel = ViewModelProvider(this).get(MyPageInformationModel::class.java)

        fromHome = intent?.getBooleanExtra("fromHome", fromHome)!!
        fromHealthCare = intent?.getBooleanExtra("fromHealthCare", fromHealthCare)!!
        fromBooking = intent?.getBooleanExtra("fromBooking", fromBooking)!!
        fromPurchase = intent?.getBooleanExtra("fromPurchase", fromPurchase)!!

        if (fromHome) {
            findNavController(R.id.nav_mypage_fragment).navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToPetInformationFragment())
        } else if (fromBooking) {
            findNavController(R.id.nav_mypage_fragment).navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToMyHospitalBookingFragment())
        } else if (fromHealthCare) {
            bookingId = intent?.getIntExtra("bookingId", bookingId)!!
            petImage = intent?.getStringExtra("petImage") ?: petImage
            petName = intent?.getStringExtra("petName") ?: petName
            visitDate = intent?.getStringExtra("visitDate") ?: visitDate
            resultDate = intent?.getStringExtra("resultDate") ?: resultDate
            petId = intent?.getStringExtra("petId") ?: petId
            centerName = intent?.getStringExtra("centerName") ?: centerName
            deepLink = intent?.getBooleanExtra("deepLink", deepLink) ?: deepLink
            result = intent?.getBooleanExtra("result",result) ?: result

            Logger.d("bookingId : $bookingId, petImage : $petImage, petName : $petName, visitDate : $visitDate, petId : $petId, deepLink : $deepLink, centerName : $centerName")
            dataModel.bookingId.value = bookingId
            dataModel.petImage.value = petImage
            dataModel.petName.value = petName
            dataModel.visitDate.value = visitDate
            dataModel.resultDate.value = resultDate
            dataModel.petId.value = if(petId.isNotEmpty()) petId.toInt() else -1
            dataModel.centerName.value = centerName
            dataModel.deepLink.value = deepLink

            if (deepLink || result) {
                findNavController(R.id.nav_mypage_fragment).navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToMyHealthCareFragment())
            } else {
                findNavController(R.id.nav_mypage_fragment).navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToMyHealthCareFragment())
                findNavController(R.id.nav_mypage_fragment).navigate(MyHealthCareFragmentDirections.actionMyHealthCareFragmentToMyHealthCareResultFragment())
            }
        } else if (fromPurchase) {
            findNavController(R.id.nav_mypage_fragment).navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToMyPurchaseHistoryFragment())
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(Intent.ACTION_VIEW == intent?.action) {
            val appLinkInformation = intent.data

            //petdoc://app.android.data/infochange?userName=%EA%B9%80%EC%84%B1%EB%AF%BC&phone=01085394523&phoneCorp=SKT&gender=M&birthday=19851116&nation=0
            if (appLinkInformation?.scheme == "petdoc" && appLinkInformation.host == "app.android.data") {
                when (appLinkInformation.lastPathSegment) {
                    "infochange" -> {

                        val viewModel = ViewModelProvider(this).get(MyPageInformationModel::class.java)
                        viewModel.phone.value = appLinkInformation?.getQueryParameter("phone")
                        viewModel.phoneVerification.value = true
                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (fromHome) {
            findNavController(R.id.nav_mypage_fragment).let{
                if(it.currentDestination?.id == R.id.petInformationFragment){
                    finish()
                    return
                }
            }
            super.onBackPressed()
        } else if (fromHealthCare) {
            finish()
        } else {
            findNavController(R.id.nav_mypage_fragment).let {
                if (it.currentDestination?.id == R.id.myPageMainFragment) {
                    finish()
                    return
                }
            }
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivityList.remove(this)
        if (fromBooking) {
            activityFinish()
        }
    }
}