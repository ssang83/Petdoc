package kr.co.petdoc.petdoc.config

import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: RemoteConfigManager
 * Created by kimjoonsung on 2020/08/19.
 *
 * Description :
 */
class RemoteConfigManager {

    private val TEN_MIN = 60 * 10
    private val ONE_DAY_SECONDS = 24 * 60 * 60

    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSetting:FirebaseRemoteConfigSettings

    companion object {

        @Volatile
        private var instance:RemoteConfigManager? = null

        fun getInstance(): RemoteConfigManager {
            if (instance == null) {
                instance = RemoteConfigManager()
            }

            return instance as RemoteConfigManager
        }
    }

    fun init() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        if (BuildConfig.DEBUG) {
            configSetting = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(TEN_MIN.toLong())
                .build()
        } else {
            configSetting = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(ONE_DAY_SECONDS.toLong())
                .build()
        }

        firebaseRemoteConfig.setConfigSettingsAsync(configSetting)
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default)
    }

    fun fetchAndActivate(
        activity: Activity,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(activity, onCompleteListener)
    }

    fun getString(key:String) = firebaseRemoteConfig.getString(key)
}