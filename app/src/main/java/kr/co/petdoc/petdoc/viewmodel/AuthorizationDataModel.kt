package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.petdoc.petdoc.network.response.submodel.AccountData
import kr.co.petdoc.petdoc.network.response.submodel.UserInfoData

/**
 * petdoc-android
 * Class: AuthorizationDataModel
 * Created by sungminkim on 2020/04/07.
 *
 * Description : 회원가입/인증 과정에서 사용될 로그인 액티비티 Scope에 한정되는 livedata
 */
class AuthorizationDataModel : ViewModel(){

    var email = MutableLiveData<String>().apply{ this.value = "" }

    var phone = MutableLiveData<String>().apply{ this.value = "" }

    var password = MutableLiveData<String>().apply{ this.value = "" }

    var name = MutableLiveData<String>().apply{ this.value = "" }

    var birthday = MutableLiveData<String>().apply{ this.value= "" }

    var sex = MutableLiveData<String>().apply{ this.value = "" }

    var type = MutableLiveData<String>().apply{ this.value = "email" }

    var telecom = MutableLiveData<String>().apply{ this.value = "" }

    var kakaoEmail = MutableLiveData<String>().apply{ value = "" }

    var faceBookEmail = MutableLiveData<String>().apply { value = "" }

    var naverEmail = MutableLiveData<String>().apply { value = "" }

    var googleEmail = MutableLiveData<String>().apply { value = "" }

    var socialKey = MutableLiveData<String>().apply{ value = "" }

    var socialType = MutableLiveData<Int>().apply { value = 1 }

    var userInfoData = MutableLiveData<MutableList<AccountData>>().apply{ value = mutableListOf() }

    var combineFlag = MutableLiveData<Boolean>().apply{ value = false }

    var combineUserEmail = MutableLiveData<String>().apply { value = "" }

    var googleToken = MutableLiveData<String>().apply{ value = "" }
}