package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.CustoMealInfoData
import kr.co.petdoc.petdoc.network.response.submodel.DiagnosisResultData
import kr.co.petdoc.petdoc.network.response.submodel.NutritionData
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.Helper
import java.io.Serializable

class FoodRecommentDataModel : ViewModel() {

    // 기본 펫 정보
    var petimage = MutableLiveData<String>().apply{ value = "" }
    var petname = MutableLiveData<String>().apply { value = "" }
    var cmInfoId = MutableLiveData<Int>().apply{ value = -1 }
    var petId = MutableLiveData<Int>().apply{ value = -1 }
    var gender = MutableLiveData<String>().apply{ value = "" }
    var neuter = MutableLiveData<Boolean>().apply{ value = false }
    var species = MutableLiveData<String>().apply{ value = "" }
    var matchFoodAllStep = MutableLiveData<Int>().apply { value = -1 }
    var petInfo = MutableLiveData<PetInfoItem>().apply{ value = null }

    // 맞춤식 진단 결과 정보
    var petResultData = MutableLiveData<DiagnosisResultData>().apply{ value = null }

    // 맞춤식 진단 구매하기 flag
    var isPurchase = MutableLiveData<Boolean>().apply{ value = false }

    // 보조 영양겔 정보
    var nutritionData1 = MutableLiveData<NutritionData>().apply{ value = null }
    var nutritionData2 = MutableLiveData<NutritionData>().apply{ value = null }
    var nutritionType1 = MutableLiveData<String>().apply{ value = "" }
    var nutritionType2 = MutableLiveData<String>().apply{ value = "" }

    // 맞춤식 진단 스텝 데이터 정보
    var weight = MutableLiveData<Float>().apply { value = 0f }
    var weightState = MutableLiveData<Int>().apply{ value = 0 }
    var body = MutableLiveData<PetBodyLevel>().apply { value = PetBodyLevel.NONE }
    var bodyTypeState = MutableLiveData<Int>().apply{ value = 0 }
    var activate = MutableLiveData<Int>().apply{ value = 0 }
    var activateState = MutableLiveData<Int>().apply{ value = 0}
    var allerge = MutableLiveData<HashSet<String>>().apply{ value = HashSet<String>() }
    var allergeState = MutableLiveData<Int>().apply{ value = 0}
    var likefood = MutableLiveData<HashSet<String>>().apply{ value = HashSet<String>() }
    var likeFoodState = MutableLiveData<Int>().apply{ value = 0}
    var feeding = MutableLiveData<HashMap<String, String>>().apply{ value = HashMap<String,String>() }
    var feedingFlag = MutableLiveData<BooleanArray>().apply{ value = booleanArrayOf(false,false,false) }
    var sickness = MutableLiveData<HashSet<String>>().apply{ value = HashSet<String>() }
    var pregnant = MutableLiveData<PetPregnantMode>().apply { value = PetPregnantMode.NONE }
    var pregnantStatus = MutableLiveData<String>().apply{ value = "" }
    var diagnosisTotalPage = MutableLiveData<Int>().apply{ value = 1 }
    var diagnosisPage = MutableLiveData<Int>().apply{ value = 1}
    var disease = MutableLiveData<HashSet<String>>().apply{ value = HashSet<String>().apply{ add("") } }
    var questionId = MutableLiveData<HashSet<Int>>().apply{ value = hashSetOf() }

    var matchmode = MutableLiveData<Boolean>().apply { value = false }
    var matchFoodAgain = MutableLiveData<Boolean>().apply { value = false }

    fun getCompleteName(firstVaue:String, secondValue:String):String = Helper.getCompleteWordByJongsung(petname.value.toString(), firstVaue, secondValue)

    fun weightGuideName():String = "${getCompleteName("이의","의")} ${Helper.readStringRes(R.string.pet_weight_sub_text)}"
    fun bodyGuideName():String = "${getCompleteName("이의","의")} ${Helper.readStringRes(R.string.pet_body_sub_text)}"
    fun weightWithKG():String = String.format("%.1f kg", weight.value)
    fun activateString():String = "${activate.value}"
    fun detailDiagnosisGuideName():String = "${getCompleteName("이에게","에게")} ${Helper.readStringRes(R.string.detail_diagnosis_sub_text)}(${diagnosisPage.value}/${diagnosisTotalPage.value})"

    fun bodyTypeToString():String = when(body.value){
        PetBodyLevel.LEVEL1 -> Helper.readStringRes(R.string.body_level_1_title)
        PetBodyLevel.LEVEL2 -> Helper.readStringRes(R.string.body_level_2_title)
        PetBodyLevel.LEVEL3 -> Helper.readStringRes(R.string.body_level_3_title)
        PetBodyLevel.LEVEL4 -> Helper.readStringRes(R.string.body_level_4_title)
        PetBodyLevel.LEVEL5 -> Helper.readStringRes(R.string.body_level_5_title)
        else -> ""
    }

    fun walkingTimeWithText():String = "${String.format("%d%s", activate.value, Helper.readStringRes(R.string.hour))}/주"

    fun pregantGuideWithName():String = "${getCompleteName("이가", "가")} ${Helper.readStringRes(R.string.pregnant_subguide)}"

    fun allergeTitle():String = "${getCompleteName("이에게", "에게")} ${Helper.readStringRes(R.string.food_allerge_sub_guide)}"
    fun allergeListWithString():String{
        var dataList = ""
        allerge.value?.apply{
            for(item in this){
                dataList += when (item) {
                    "chicken_meet" -> Helper.readStringRes(R.string.food_type_chicken) + ","
                    "salmon_tuna" -> Helper.readStringRes(R.string.food_type_fish) + ","
                    "lamb" -> Helper.readStringRes(R.string.food_type_sheep) + ","
                    "duck_meet" -> Helper.readStringRes(R.string.food_type_duck) + ","
                    "beef" -> Helper.readStringRes(R.string.food_type_beef) + ","
                    "like_all" -> Helper.readStringRes(R.string.food_type_meat_and_fish)+","
                    else -> ""
                }
            }
        }
        if(dataList.isNotEmpty()) dataList = dataList.substring(0, dataList.length-1)
        return dataList
    }

    fun likeTitle():String = "${getCompleteName("이가", "가")} ${Helper.readStringRes(R.string.food_like_sub_guide)}"
    fun likefoodWithString():String{
        var dataList = ""
        likefood.value?.apply {
            for (item in this) {
                dataList += when(item){
                    "chicken_meet" -> Helper.readStringRes(R.string.food_type_chicken)+","
                    "salmon_tuna" -> Helper.readStringRes(R.string.food_type_fish)+","
                    "lamb" -> Helper.readStringRes(R.string.food_type_sheep)+","
                    "duck_meet" -> Helper.readStringRes(R.string.food_type_duck)+","
                    "beef" -> Helper.readStringRes(R.string.food_type_beef)+","
                    "like_all" -> Helper.readStringRes(R.string.food_type_meat_and_fish)+","
                    else -> ""
                }
            }
        }
        if(dataList.isNotEmpty()) dataList = dataList.substring(0, dataList.length-1)
        return dataList
    }

    fun feedingText():String{
        var dataList = ""
        feeding.value?.apply{
            if(!this["dry"].isNullOrEmpty()) dataList += this["dry"]
            if(!this["wet"].isNullOrEmpty()){
                if(dataList.isNotEmpty()) dataList += ","
                dataList += this["wet"]
            }
            if(!this["snack"].isNullOrEmpty()){
                if(dataList.isNotEmpty()) dataList += ","
                dataList += this["snack"]
            }

        }
        return dataList
    }

    fun sicknessText():String{
        var dataList = ""
        sickness.value?.apply{
            for(item in this){
                dataList += when(item){
                    "strong" -> Helper.readStringRes(R.string.sickness_healthy)+","
                    "joint" -> Helper.readStringRes(R.string.sickness_joint)+","
                    "skin" -> Helper.readStringRes(R.string.sickness_skin)+","
                    "digestion" -> Helper.readStringRes(R.string.sickness_stomach)+","
                    "immunity" -> Helper.readStringRes(R.string.sickness_dispense)+","
                    "stone_kidney" -> Helper.readStringRes(R.string.sickness_bean)+","
                    "obesity" -> Helper.readStringRes(R.string.sickness_fat)+","
                    else -> ""
                }
            }
        }
        if(dataList.isNotEmpty()) dataList = dataList.substring(0, dataList.length-1)
        return dataList
    }

    fun pregnantText():String{
        return when(pregnant.value){
            PetPregnantMode.NONE -> Helper.readStringRes(R.string.non_pregnant)
            PetPregnantMode.PREGNANT -> Helper.readStringRes(R.string.pregnant_true)
            PetPregnantMode.BREEDING -> Helper.readStringRes(R.string.breeding_true)
            else -> Helper.readStringRes(R.string.non_pregnant)
        }
    }


    enum class PetBodyLevel : Serializable {
        NONE, LEVEL1, LEVEL2, LEVEL3, LEVEL4, LEVEL5
    }

    enum class PetPregnantMode : Serializable {
        NONE, PREGNANT, BREEDING
    }


    var sickDetailMode = MutableLiveData<Int>().apply{ value = 0 }
    var sickDetailData = MutableLiveData<Array<HashSet<Int>>>().apply{ value = Array<HashSet<Int>>(6){ HashSet<Int>() } }

    var progressValue = MutableLiveData<Int>().apply{ value = 0 }       // 상단, 프로그레스 필요시에 표기
}