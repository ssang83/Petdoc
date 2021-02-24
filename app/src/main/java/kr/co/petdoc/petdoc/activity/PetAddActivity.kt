package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_pet_add.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.petadd.*
import kr.co.petdoc.petdoc.fragment.petadd.PetSexFragment.Companion.KEY_IS_INCOMPLETE_STEP
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: PetAddActivity
 * Created by kimjoonsung on 2020/04/06.
 *
 * Description : 반려동물 등록 화면 (Running on Navigator)
 */
class PetAddActivity : BaseActivity() {

    private lateinit var dataModel:PetAddInfoDataModel
    private var petInfoItem:PetInfoItem? = null

    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)

    private var currentYear = ""
    private var currentMonth = ""

    private var step = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0)
        setContentView(R.layout.activity_pet_add)   // Start On Navigator

        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        currentYear = yearFormat.format(Date(System.currentTimeMillis()))
        currentMonth = monthFormat.format(Date(System.currentTimeMillis()))

        dataModel = ViewModelProvider(this).get(PetAddInfoDataModel::class.java)
        intent?.getIntExtra("type", PetAddType.ADD.ordinal).let {
            dataModel.type.value = it
        }

        petInfoItem = intent?.getParcelableExtra("item")
        if (petInfoItem != null) {
            dataModel.petId.value = petInfoItem!!.id
            dataModel.petName.value = petInfoItem!!.name
            dataModel.petKind.value = petInfoItem!!.kind
            dataModel.petKindKey.value = getKindKey(petInfoItem!!.kind!!)
            dataModel.petBreed.value = petInfoItem!!.species
            dataModel.petBreedId.value = petInfoItem!!.speciesId
            dataModel.petSex.value = petInfoItem!!.gender
            // 중성화 여부 선택 안한경우에 isNeutralized = true 로 내려와서 step 이 음
            // 중성화 여부 선택 전이면  petNeutralization 값 세팅하지 않
            if (intent?.getIntExtra("step", 0)!! > 5) {
                dataModel.petNeutralization.value = petInfoItem!!.isNeutralized
            }
            dataModel.petVaccine.value = petInfoItem!!.inoculationStatus
            dataModel.petProfile.value = petInfoItem!!.imageUrl
            dataModel.petBirth.value = if(petInfoItem!!.birthday != "1900-01-01") petInfoItem!!.birthday else ""
            dataModel.petAgeType.value = petInfoItem!!.ageType
            dataModel.petAge.value = if(petInfoItem!!.birthday != "1900-01-01") petInfoItem!!.birthday else ""

            calculateBirthday(petInfoItem!!)
        }

        step = intent?.getIntExtra("step", 0)!!
        dataModel.regStep.value = step
        val defaultNavHost = findNavController(R.id.petAddHost)
        when (step) {
            6 -> {
                defaultNavHost.navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                if (dataModel.petKind.value.toString() == "강아지" || dataModel.petKind.value.toString() == "고양이") {
                    defaultNavHost.navigate(PetKindFragmentDirections.actionPetKindFragmentToPetBreedFragment())
                    defaultNavHost.navigate(PetBreedFragmentDirections.actionPetBreedFragmentToPetAgeFragment())
                } else {
                    defaultNavHost.navigate(PetKindFragmentDirections.actionPetKindFragmentToPetSpeciesListFragment())
                    defaultNavHost.navigate(PetSpeciesListFragmentDirections.actionPetSpeciesListFragmentToPetAgeFragment())
                }
                defaultNavHost.navigate(PetAgeFragmentDirections.actionPetAgeFragmentToPetSexFragment())
                defaultNavHost.navigate(PetSexFragmentDirections.actionPetSexFragmentToPetVaccineFragment())
            }

            5 -> {
                defaultNavHost.navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                if (dataModel.petKind.value.toString() == "강아지" || dataModel.petKind.value.toString() == "고양이") {
                    defaultNavHost.navigate(PetKindFragmentDirections.actionPetKindFragmentToPetBreedFragment())
                    defaultNavHost.navigate(PetBreedFragmentDirections.actionPetBreedFragmentToPetAgeFragment())
                } else {
                    defaultNavHost.navigate(PetKindFragmentDirections.actionPetKindFragmentToPetSpeciesListFragment())
                    defaultNavHost.navigate(PetSpeciesListFragmentDirections.actionPetSpeciesListFragmentToPetAgeFragment())
                }
                defaultNavHost.navigate(
                        PetAgeFragmentDirections.actionPetAgeFragmentToPetSexFragment().actionId,
                        Bundle().apply { putBoolean(KEY_IS_INCOMPLETE_STEP, true) }
                )
            }

            4 -> {
                if (petInfoItem!!.species == "고양이" || petInfoItem!!.species!!.contains("소형") || petInfoItem!!.species!!.contains("중형") || petInfoItem!!.species!!.contains("대형")) {
                    dataModel.petBreedNotKnow.value = true
                } else {
                    dataModel.petBreedKnow.value = true
                }

                defaultNavHost.navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                if (dataModel.petKind.value.toString() == "강아지" || dataModel.petKind.value.toString() == "고양이") {
                    defaultNavHost.navigate(PetKindFragmentDirections.actionPetKindFragmentToPetBreedFragment())
                    defaultNavHost.navigate(PetBreedFragmentDirections.actionPetBreedFragmentToPetAgeFragment())
                } else {
                    defaultNavHost.navigate(PetKindFragmentDirections.actionPetKindFragmentToPetSpeciesListFragment())
                    defaultNavHost.navigate(PetSpeciesListFragmentDirections.actionPetSpeciesListFragmentToPetAgeFragment())
                }
            }

            3 -> {
                if (dataModel.petKind.value.toString() == "강아지" || dataModel.petKind.value.toString() == "고양이") {
                    defaultNavHost.navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                    defaultNavHost.navigate(PetKindFragmentDirections.actionPetKindFragmentToPetBreedFragment())
                } else {
                    defaultNavHost.navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                    defaultNavHost.navigate(PetKindFragmentDirections.actionPetKindFragmentToPetSpeciesListFragment())
                }
            }

            2 -> {
                defaultNavHost.navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
            }

            1 -> {
                defaultNavHost.navigate(R.id.petNameFragment)
            }
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        findNavController(R.id.petAddHost).let{
            if(it.currentDestination?.id == R.id.petNameFragment){
                finish()
                return
            }

        }
        super.onBackPressed()
    }

    //================================================================================================
    private fun calculateBirthday(item: PetInfoItem) {
        if (item.birthday!! != "1900-01-01") {
            val year = item.birthday!!.split("-")[0]
            val month = item.birthday!!.split("-")[1]
            if (currentYear == year && currentMonth == month) {
                dataModel.petYear.value = "0"
                dataModel.petMonth.value = "0"
            } else {
                if (currentMonth.toInt() >= month.toInt()) {
                    dataModel.petYear.value = "${currentYear.toInt() - year.toInt()}"
                    dataModel.petMonth.value = "${currentMonth.toInt() - month.toInt()}"

//                return "${currentYear.toInt() - year.toInt()}세 ${currentMonth.toInt() - month.toInt()}개월"
                } else {
                    val calMonth = if(currentMonth.toInt() > 9) {
                        currentMonth
                    } else {
                        currentMonth.replace("0", "")
                    }

                    dataModel.petYear.value = "${currentYear.toInt() - (year.toInt() + 1)}"
                    dataModel.petMonth.value = "${calMonth}"

//                return "${currentYear.toInt() - (year.toInt() + 1)}세 ${calMonth}개월"
                }
            }
        }
    }

    private fun getKindKey(kind: String) =
        when (kind) {
            "강아지" -> "dog"
            "고양이" -> "cat"
            "햄스터" -> "hamster"
            "토끼" -> "rabbit"
            "고슴도치" -> "hedgehog"
            "거북이" -> "turtle"
            "기니피그" -> "guinea_pig"
            "조류" -> "bird"
            "패릿" -> "ferret"
            else -> ""
        }
}