package kr.co.petdoc.petdoc.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kr.co.petdoc.petdoc.log.Logger


/**
 * Petdoc
 * Class: TextAddressFromGPS
 * Created by kimjoonsung on 2020/06/05.
 *
 * Description :
 */
class TextAddressFromGPS(context: Context, longitude:Double, latitude:Double) {

    var longitude = 0.0
    var latitude = 0.0
    var context: Context? = null

    private val candidateCount = 3

    var city = ""
    var local = ""

    init {
        this.context = context
        this.longitude = longitude
        this.latitude = latitude

        takeAddress()
    }

    private fun takeAddress() {
        val geocoder = Geocoder(context)
        var candidate: List<Address>? = null
        try {
            candidate = geocoder.getFromLocation(latitude, longitude, candidateCount)
            for (candidateItem in candidate) {
                city = candidateItem.adminArea
                local =
                    if (candidateItem.locality == null) if (candidateItem.subLocality == null) candidateItem.thoroughfare else candidateItem.subLocality else candidateItem.locality
            }
        } catch (exception: Throwable) {
            Logger.d("address : ${exception.message}")
        }
    }


    fun getTextAddress(): String? {
        return String.format("%s %s", city, local)
    }
}