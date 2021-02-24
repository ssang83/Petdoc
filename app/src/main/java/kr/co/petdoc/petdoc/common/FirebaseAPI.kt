package kr.co.petdoc.petdoc.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Petdoc
 * Class: FirebaseAPI
 * Created by kimjoonsung on 2020/08/25.
 *
 * Description :
 */
class FirebaseAPI(context: Context) {

    private var firebaseAnalytics:FirebaseAnalytics

    init {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun logEventFirebase(category:String, action:String, label:String) {

        val params = Bundle().apply {
            putString("category", category)
            putString("action", action)
            putString("description", label)
        }

        firebaseAnalytics.logEvent(category, params)
    }
}