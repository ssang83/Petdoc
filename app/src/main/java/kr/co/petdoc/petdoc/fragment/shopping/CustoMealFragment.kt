package kr.co.petdoc.petdoc.fragment.shopping

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.shopping_custo_meal_item.*
import kotlinx.android.synthetic.main.shopping_custo_meal_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MatchFoodActivity
import kr.co.petdoc.petdoc.activity.MatchFoodResultActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: CustoMealFragment
 * Created by kimjoonsung on 2020/10/26.
 *
 * Description :
 */
class CustoMealFragment : Fragment() {

    private var petInfoItem:PetInfoItem? = null

    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)

    private var currentYear = ""
    private var currentMonth = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.shopping_custo_meal_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petInfoItem = arguments?.getParcelable("item")!!
        Logger.d("item : ${petInfoItem?.name}")

        //=========================================================================================

        currentYear = yearFormat.format(Date(System.currentTimeMillis()))
        currentMonth = monthFormat.format(Date(System.currentTimeMillis()))

        petImage.apply {
            when {
                petInfoItem?.imageUrl!!.isEmpty() -> {
                    petImage.setBackgroundResource(R.drawable.img_pet_profile_default)
                }

                else -> {
                    Glide.with(requireContext())
                        .load( if(petInfoItem?.imageUrl!!.startsWith("http")) petInfoItem?.imageUrl else "${AppConstants.IMAGE_URL}${petInfoItem?.imageUrl}")
                        .apply(RequestOptions.circleCropTransform())
                        .into(petImage)
                }
            }
        }

        petName.text = petInfoItem?.name

        val gender = if(petInfoItem?.gender == "man") {
            "남아"
        } else {
            "여아"
        }

        val year = petInfoItem?.birthday!!.split("-")[0]
        val month = petInfoItem?.birthday!!.split("-")[1]
        if (currentMonth.toInt() > month.toInt()) {
            val age = "${currentYear.toInt() - year.toInt()}세 ${currentMonth.toInt() - month.toInt()}개월"
            petInfo.text = "${age}(${gender}) ・ ${petInfoItem?.species}"
        } else {
            val calMonth = if(currentMonth.toInt() > 9) {
                currentMonth
            } else {
                currentMonth.replace("0", "")
            }
            val age = "${currentYear.toInt() - (year.toInt() + 1)}세 ${calMonth}개월"
            petInfo.text = "${age}(${gender}) ・ ${petInfoItem?.species}"
        }

        btnDiagnosisResult.visibility = View.GONE
        btnPurchase.visibility = View.GONE

        if ((petInfoItem?.regInfoStep == 7 && petInfoItem?.gender == "man")
            || (petInfoItem?.regInfoStep == 8 && petInfoItem?.gender == "woman")) {
            btnDiagnosisResult.visibility = View.VISIBLE
            btnPurchase.visibility = View.VISIBLE
            btnDiagnosis.visibility = View.GONE
        } else {
            btnDiagnosis.visibility = View.VISIBLE
            if (petInfoItem?.regPetStep != 6) {
                petInfo.text = "반려동물 등록 진행중"
            }
        }

        //==========================================================================================
        btnDiagnosis.setOnClickListener {
            if (petInfoItem?.regPetStep == 6) {
                onMatchFood(petInfoItem!!)
            } else {
                onPetAdd(petInfoItem!!)
            }
        }

        btnDiagnosisResult.setOnClickListener {
            onMatchFoodResult(petInfoItem!!)
        }

        btnPurchase.setOnClickListener {
            onPurchase(petInfoItem!!)
        }
    }

    //==============================================================================================
    private fun onMatchFood(item: PetInfoItem) {
        if (item.kind == "강아지") {
            Airbridge.trackEvent("shopping", "click", "customeal_start", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("쇼핑_홈_맞춤식진단", "Click Event", "쇼핑탭 화면에서 바로 맞춤식진단 버튼 클릭")
            startActivity(Intent(requireActivity(), MatchFoodActivity::class.java).apply {
                putExtra("item", item)
            })
        } else {
            OneBtnDialog(requireContext()).apply {
                setTitle("커스터밀")
                setMessage("커스터밀 맞춤식은 현재 강아지를 위한 제품입니다. ${item.kind}를 위한 맞춤식은 준비중이니 조금만 기다려 주세요.")
                setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                    dismiss()
                })
                show()
            }
        }
    }

    private fun onMatchFoodResult(item: PetInfoItem) {
        Airbridge.trackEvent("shopping", "click", "customeal_result", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("쇼핑_홈_진단결과", "Click Event", "쇼핑탭 화면에서 바로 진단결과 버튼 클릭")
        startActivity(Intent(requireActivity(), MatchFoodResultActivity::class.java).apply {
            putExtra("item", item)
            putExtra("type", "activity")
        })
    }

    private fun onPurchase(item: PetInfoItem) {
        startActivity(Intent(requireActivity(), MatchFoodResultActivity::class.java).apply {
            putExtra("item", item)
            putExtra("type", "activity")
            putExtra("isPurchase", true)
        })
    }

    private fun onPetAdd(item: PetInfoItem) {
        Airbridge.trackEvent("shopping", "click", "customeal_add", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("쇼핑_홈_동물등록", "Click Event", "쇼핑탭 화면에서 바로 동물등록 버튼 클릭")
        when (item.regPetStep.plus(1)) {
            6 -> {
                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 6)
                    putExtra("item", item)
                })
            }

            5 -> {
                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 5)
                    putExtra("item", item)
                })
            }

            4 -> {
                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 4)
                    putExtra("item", item)
                })
            }

            3 -> {
                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 3)
                    putExtra("item", item)
                })
            }

            2 -> {
                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 2)
                    putExtra("item", item)
                })
            }

            1 -> {
                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 1)
                    putExtra("item", item)
                })
            }
        }
    }
}