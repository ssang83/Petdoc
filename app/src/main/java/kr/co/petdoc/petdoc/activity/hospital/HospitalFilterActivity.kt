package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_hospital_filter.*
import kotlinx.android.synthetic.main.activity_hospital_filter.btnClose
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils

/**
 * Petdoc
 * Class: HospitalFilterActivity
 * Created by kimjoonsung on 2020/07/21.
 *
 * Description :
 */
class HospitalFilterActivity : AppCompatActivity() {

    private var animalIdList:MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_hospital_filter)

        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        btnClose.setOnClickListener { finish() }
        btnReset.setOnClickListener { filterReset() }

        btnBeauty.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                beauty.isSelected = true
            } else {
                it.isSelected = false
                beauty.isSelected = false
            }
        }

        btnHotel.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                hotel.isSelected = true
            } else {
                it.isSelected = false
                hotel.isSelected = false
            }
        }

        btnAllday.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                allDay.isSelected = true
            } else {
                it.isSelected = false
                allDay.isSelected = false
            }
        }

        btnParking.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                parking.isSelected = true
            } else {
                it.isSelected = false
                parking.isSelected = false
            }
        }

        btnClinic.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                clinic.isSelected = true
            } else {
                it.isSelected = false
                clinic.isSelected = false
            }
        }

        btnKindergarden.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                kindergarden.isSelected = true
            } else {
                it.isSelected = false
                kindergarden.isSelected = false
            }
        }

        btnCafe.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                cafe.isSelected = true
            } else {
                it.isSelected = false
                cafe.isSelected = false
            }
        }

        //=============================================================================================

        btnDog.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                dog.isSelected = true
            } else {
                it.isSelected = false
                dog.isSelected = false
            }
        }

        btnCat.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                cat.isSelected = true
            } else {
                it.isSelected = false
                cat.isSelected = false
            }
        }

        btnHamster.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                hamster.isSelected = true
            } else {
                it.isSelected = false
                hamster.isSelected = false
            }
        }

        btnPig.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                pig.isSelected = true
            } else {
                it.isSelected = false
                pig.isSelected = false
            }
        }

        btnHedgehog.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                hedgehog.isSelected = true
            } else {
                it.isSelected = false
                hedgehog.isSelected = false
            }
        }

        btnTurtle.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                turtle.isSelected = true
            } else {
                it.isSelected = false
                turtle.isSelected = false
            }
        }

        btnBirds.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                birds.isSelected = true
            } else {
                it.isSelected = false
                birds.isSelected = false
            }
        }

        btnParet.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                paret.isSelected = true
            } else {
                it.isSelected = false
                paret.isSelected = false
            }
        }

        btnFilterComplete.setOnClickListener {
            FirebaseAPI(this).logEventFirebase("병원예약_필터적용", "Click Event", "필터 적용 완료")
            setFilter()
            petIdFromKind()
            finish()
        }

        //==========================================================================================
        btnWorkStatus.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_CLINIC_STATUS, "") == "Y" -> isChecked = true
                else -> isChecked = false
            }
        }

        btnBookingStatus.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_BOOKING_STATUS, "") == "Y" -> isChecked = true
                else -> isChecked = false
            }
        }

        btnBeauty.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_BEAUTY_STATUS, "") == "Y" -> {
                    isSelected = true
                    beauty.isSelected = true
                }

                else -> {
                    isSelected = false
                    beauty.isSelected = false
                }
            }
        }

        btnHotel.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_HOTEL_STATUS, "") == "Y" -> {
                    isSelected = true
                    hotel.isSelected = true
                }

                else -> {
                    isSelected = false
                    hotel.isSelected = false
                }
            }
        }

        btnAllday.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_ALLDAY_STATUS, "") == "Y" -> {
                    isSelected = true
                    allDay.isSelected = true
                }

                else -> {
                    isSelected = false
                    allDay.isSelected = false
                }
            }
        }

        btnParking.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_PARKING_STATUS, "") == "Y" -> {
                    isSelected = true
                    parking.isSelected = true
                }

                else -> {
                    isSelected = false
                    parking.isSelected = false
                }
            }
        }

        btnCafe.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_CAFE_STATUS, "") == "Y" -> {
                    isSelected = true
                    cafe.isSelected = true
                }

                else -> {
                    isSelected = false
                    cafe.isSelected = false
                }
            }
        }

        btnClinic.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_CLINIC_FACILITY_STATUS, "") == "Y" -> {
                    isSelected = true
                    clinic.isSelected = true
                }

                else -> {
                    isSelected = false
                    clinic.isSelected = false
                }
            }
        }

        btnKindergarden.apply {
            when {
                StorageUtils.loadValueFromPreference(this@HospitalFilterActivity, AppConstants.PREF_KEY_KINERGARDEN_STATUS, "") == "Y" -> {
                    isSelected = true
                    kindergarden.isSelected = true
                }

                else -> {
                    isSelected = false
                    kindergarden.isSelected = false
                }
            }
        }

        val items = StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_CLINIC_PET_STATUS, "")
        if (items.isNotEmpty()) {
            val petIdList = Gson().fromJson(items, Array<Int>::class.java).toMutableList()
            setFilterKind(petIdList)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun filterReset() {
        btnWorkStatus.isChecked = false
        btnBookingStatus.isChecked = false

        btnBeauty.isSelected = false
        beauty.isSelected = false
        btnHotel.isSelected = false
        hotel.isSelected = false
        btnAllday.isSelected = false
        allDay.isSelected = false
        btnParking.isSelected = false
        parking.isSelected = false
        btnClinic.isSelected = false
        clinic.isSelected = false
        btnKindergarden.isSelected = false
        kindergarden.isSelected = false
        btnCafe.isSelected = false
        cafe.isSelected = false

        btnDog.isSelected = false
        dog.isSelected = false
        btnCat.isSelected = false
        cat.isSelected = false
        btnHamster.isSelected = false
        hamster.isSelected = false
        btnPig.isSelected = false
        pig.isSelected = false
        btnHedgehog.isSelected = false
        hedgehog.isSelected = false
        btnTurtle.isSelected = false
        turtle.isSelected = false
        btnBirds.isSelected = false
        birds.isSelected = false
        btnParet.isSelected = false
        paret.isSelected = false

    }

    private fun petIdFromKind() {
        if(btnDog.isSelected) animalIdList.add(2)
        if(btnCat.isSelected) animalIdList.add(3)
        if(btnHamster.isSelected) animalIdList.add(4)
        if(btnHedgehog.isSelected) animalIdList.add(6)
        if(btnTurtle.isSelected) animalIdList.add(7)
        if(btnPig.isSelected) animalIdList.add(8)
        if(btnBirds.isSelected) animalIdList.add(9)
        if(btnParet.isSelected) animalIdList.add(10)

        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_CLINIC_PET_STATUS, animalIdList.toString())
    }

    private fun animalFilterResetUI() {
        btnDog.isSelected = false
        dog.isSelected = false
        btnCat.isSelected = false
        cat.isSelected = false
        btnHamster.isSelected = false
        hamster.isSelected = false
        btnPig.isSelected = false
        pig.isSelected = false
        btnHedgehog.isSelected = false
        hedgehog.isSelected = false
        btnTurtle.isSelected = false
        turtle.isSelected = false
        btnBirds.isSelected = false
        birds.isSelected = false
        btnParet.isSelected = false
        paret.isSelected = false
    }

    private fun setFilterKind(animalList: MutableList<Int>) {
        animalFilterResetUI()
        for (i in 0 until animalList.size) {
            when (animalList[i]) {
                2 -> {
                    btnDog.isSelected = true
                    dog.isSelected = true
                }

                3 -> {
                    btnCat.isSelected = true
                    cat.isSelected = true
                }

                4-> {
                    btnHamster.isSelected = true
                    hamster.isSelected = true
                }

                6-> {
                    btnHedgehog.isSelected = true
                    hedgehog.isSelected = true
                }

                7 -> {
                    btnTurtle.isSelected = true
                    turtle.isSelected = true
                }

                8 -> {
                    btnPig.isSelected = true
                    pig.isSelected = true
                }

                9 -> {
                    btnBirds.isSelected = true
                    birds.isSelected = true
                }

                10 -> {
                    btnParet.isSelected = true
                    paret.isSelected = true
                }
            }
        }
    }

    private fun setFilter() {
        if (btnWorkStatus.isChecked) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_CLINIC_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_CLINIC_STATUS, "")
        }

        if (btnBookingStatus.isChecked) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_BOOKING_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_BOOKING_STATUS, "")
        }

        if (btnBeauty.isSelected) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_BEAUTY_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_BEAUTY_STATUS, "")
        }

        if (btnHotel.isSelected) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_HOTEL_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_HOTEL_STATUS, "")
        }

        if (btnAllday.isSelected) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_ALLDAY_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_ALLDAY_STATUS, "")
        }

        if (btnParking.isSelected) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_PARKING_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_PARKING_STATUS, "")
        }

        if (btnClinic.isSelected) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_CLINIC_FACILITY_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_CLINIC_FACILITY_STATUS, "")
        }

        if (btnKindergarden.isSelected) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_KINERGARDEN_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_KINERGARDEN_STATUS, "")
        }

        if (btnCafe.isSelected) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_CAFE_STATUS, "Y")
        } else {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_CAFE_STATUS, "")
        }
    }
}