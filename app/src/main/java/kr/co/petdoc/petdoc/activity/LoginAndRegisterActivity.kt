package kr.co.petdoc.petdoc.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_login_register.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.login.LoginFragmentDirections
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.PermissionCallback
import kr.co.petdoc.petdoc.utils.PermissionStatus
import kr.co.petdoc.petdoc.viewmodel.AuthorizationDataModel


/**
 * petdoc-android
 * Class: LoginAndRegisterActivity
 * Created by sungminkim on 2020/04/06.
 *
 * Description : 로그인 / 회원가입 처리 액티비티
 */
class LoginAndRegisterActivity : AppCompatActivity() {

    lateinit var authorization : AuthorizationDataModel

    private var deepLink = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Helper.statusBarColorChange(this, true, alpha=0, fullscreen = true)
        setContentView(R.layout.activity_login_register)             // start On Navigator
//        login_register_root_view.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        authorization = ViewModelProvider(this).get(AuthorizationDataModel::class.java)

        deepLink = intent?.getBooleanExtra("deepLink", false) ?:deepLink

        Helper.permissionCheck(this,
            arrayOf("android.permission.INTERNET","android.permission.ACCESS_NETWORK_STATE"),
            object : PermissionCallback{
                override fun callback(status: PermissionStatus) {
                    when(status){
                        PermissionStatus.ALL_GRANTED -> {}
                        PermissionStatus.DENIED_SOME -> {
                            //finish()
                        }
                    }
                }
            },true )

        if (deepLink) {
            findNavController(R.id.nav_login_host_fragment).navigate(LoginFragmentDirections.actionLoginFragmentToRegisterPolicyFragment())
        }
    }

/*    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(Intent.ACTION_VIEW == intent?.action) {
            val appLinkInformation = intent.data

            //petdoc://app.android.data/auth?userName=%EA%B9%80%EC%84%B1%EB%AF%BC&phone=01085394523&phoneCorp=SKT&gender=M&birthday=19851116&nation=0
            if (appLinkInformation?.scheme == "petdoc" && appLinkInformation.host == "app.android.data") {
                when (appLinkInformation.lastPathSegment) {
                    "auth" -> {
                        authorization.name.value = appLinkInformation.getQueryParameter("userName") ?: ""
                        authorization.phone.value = appLinkInformation.getQueryParameter("phone") ?: ""
                        authorization.sex.value =  appLinkInformation.getQueryParameter("gender") ?: ""
                        authorization.birthday.value = appLinkInformation.getQueryParameter("birthday") ?: ""
                        authorization.telecom.value = appLinkInformation.getQueryParameter("phoneCorp") ?: ""

                        findNavController(R.id.nav_login_host_fragment).let{
//                            if(it.currentDestination?.id == R.id.authorizationFragment){
                                //onBackPressed()
//                                it.navigate(AuthorizationFragmentDirections.actionAuthorizationFragmentToUserInformationCheckFragment())
//                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }*/

    override fun onBackPressed() {
        findNavController(R.id.nav_login_host_fragment).let {
            if(it.currentDestination?.id == R.id.loginFragment) {
                finish()
                return
            }
        }

        super.onBackPressed()
    }

}