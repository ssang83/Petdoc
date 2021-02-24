package kr.co.petdoc.petdoc.fragment.hospital

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_hospital_filter.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel

/**
 * Petdoc
 * Class: HospitalFilterFragment
 * Created by kimjoonsung on 2020/06/12.
 *
 * Description :
 */
class HospitalFilterFragment : Fragment() {

    private lateinit var dataModel: HospitalDataModel

    private var animalIdList:MutableList<Int> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_hospital_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnClickListener { requireActivity().onBackPressed() }
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
            setFilter()
            petIdFromKind()
            requireActivity().onBackPressed()
        }

        //==========================================================================================
        btnWorkStatus.apply {
            when {
                dataModel.workingYn.value!!.toString() == "Y" -> isChecked = true
                else -> isChecked = false
            }
        }

        btnBookingStatus.apply {
            when {
                dataModel.bookingYn.value!!.toString() == "Y" -> isChecked = true
                else -> isChecked = false
            }
        }

        btnBeauty.apply {
            when {
                dataModel.beautyYn.value!!.toString() == "Y" -> {
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
                dataModel.hotelYn.value!!.toString() == "Y" -> {
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
                dataModel.allDayYn.value!!.toString() == "Y" -> {
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
                dataModel.parkingYn.value!!.toString() == "Y" -> {
                    isSelected = true
                    parking.isSelected = true
                }

                else -> {
                    isSelected = false
                    parking.isSelected = false
                }
            }
        }

        setFilterKind(dataModel.animalIdList.value!!)
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

        dataModel.filterValidation.value!!.set(0, false)
        dataModel.filterValidation.value!!.set(1, false)
        dataModel.filterValidation.value!!.set(2, false)
        dataModel.filterValidation.value!!.set(3, false)
        dataModel.filterValidation.value!!.set(4, false)
        dataModel.filterValidation.value!!.set(5, false)
        dataModel.filterValidation.value!!.set(6, false)
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

        dataModel.animalIdList.value = animalIdList
        dataModel.filterValidation.value?.apply {
            if(animalIdList.size > 0) {
                set(6, true)
            } else {
                set(6, false)
            }
        }
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
            dataModel.workingYn.value = "Y"
            dataModel.filterValidation.value?.set(0, true)
        } else {
            dataModel.workingYn.value = ""
            dataModel.filterValidation.value?.set(0, false)
        }

        if (btnBookingStatus.isChecked) {
            dataModel.bookingYn.value = "Y"
            dataModel.filterValidation.value?.set(1, true)
        } else {
            dataModel.bookingYn.value = ""
            dataModel.filterValidation.value?.set(1, false)
        }

        if (btnBeauty.isSelected) {
            dataModel.beautyYn.value = "Y"
            dataModel.filterValidation.value?.set(2, true)
        } else {
            dataModel.beautyYn.value = ""
            dataModel.filterValidation.value?.set(2, false)
        }

        if (btnHotel.isSelected) {
            dataModel.hotelYn.value = "Y"
            dataModel.filterValidation.value?.set(3, true)
        } else {
            dataModel.hotelYn.value = ""
            dataModel.filterValidation.value?.set(3, false)
        }

        if (btnAllday.isSelected) {
            dataModel.allDayYn.value = "Y"
            dataModel.filterValidation.value?.set(4, true)
        } else {
            dataModel.allDayYn.value = ""
            dataModel.filterValidation.value?.set(4, false)
        }

        if (btnParking.isSelected) {
            dataModel.parkingYn.value = "Y"
            dataModel.filterValidation.value?.set(5, true)
        } else {
            dataModel.parkingYn.value = ""
            dataModel.filterValidation.value?.set(5, false)
        }
    }
}