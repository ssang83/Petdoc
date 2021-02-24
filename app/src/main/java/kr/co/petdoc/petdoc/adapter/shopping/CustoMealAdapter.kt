package kr.co.petdoc.petdoc.adapter.shopping

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.shopping_custo_meal_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MatchFoodActivity
import kr.co.petdoc.petdoc.activity.MatchFoodResultActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import org.threeten.bp.LocalDate

/**
 * Petdoc
 * Class: CustoMealAdapter
 * Created by kimjoonsung on 2020/07/10.
 *
 * Description :
 */
class CustoMealAdapter(
    private val items: List<PetInfoItem>
) : RecyclerView.Adapter<CustoMealAdapter.CustoMealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CustoMealViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.shopping_custo_meal_item, parent, false)
        )

    override fun onBindViewHolder(holder: CustoMealViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class CustoMealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: PetInfoItem) {
            itemView.btnDiagnosis.setOnClickListener {
                if (item.regPetStep == 6) {
                    onMatchFood(item)
                } else {
                    onPetAdd(item)
                }
            }

            itemView.btnDiagnosisResult.setOnClickListener {
                onMatchFoodResult(item)
            }

            itemView.btnPurchase.setOnClickListener {
                onPurchase(item)
            }

            itemView.petImage.apply {
                when {
                    item.imageUrl!!.isEmpty() -> {
                        Glide.with(itemView.context)
                            .load(R.drawable.img_pet_profile_default)
                            .apply(RequestOptions.circleCropTransform())
                            .into(itemView.petImage)
                    }

                    else -> {
                        Glide.with(itemView.context)
                            .load( if(item.imageUrl!!.startsWith("http")) item.imageUrl else "${AppConstants.IMAGE_URL}${item.imageUrl}")
                            .apply(RequestOptions.circleCropTransform())
                            .into(itemView.petImage)
                    }
                }
            }

            itemView.petName.text = item.name

            val gender = if(item.gender == "man") {
                "남아"
            } else {
                "여아"
            }

            val year = item.birthday!!.split("-")[0]
            val month = item.birthday!!.split("-")[1]
            val day = item.birthday!!.split("-")[2]
            val period = LocalDate.of(year.toInt(), month.toInt(), day.toInt()).until(LocalDate.now())

            if (item.birthday!!.startsWith("1900").not()) {
                val age = "${period.years}세${period.months}개월"
                itemView.petInfo.text = "${age}(${gender}) ・ ${item.species}"
            }

            if ((item.kind == "강아지") && ((item.regPetStep == 6) && (item.regInfoAllStep == item.regInfoStep))) {
                itemView.btnDiagnosisResult.visibility = View.VISIBLE
                itemView.btnPurchase.visibility = View.VISIBLE
                itemView.btnDiagnosis.visibility = View.GONE
            } else {
                itemView.btnDiagnosisResult.visibility = View.GONE
                itemView.btnPurchase.visibility = View.GONE
                itemView.btnDiagnosis.visibility = View.VISIBLE
                if (item.regPetStep != 6) {
                    itemView.petInfo.text = "반려동물 등록 진행중"
                }
            }
        }

        private fun onMatchFood(item: PetInfoItem) {
            val context = itemView.context
            if (item.kind == "강아지") {
                Airbridge.trackEvent("shopping", "click", "customeal_start", null, null, null)
                FirebaseAPI(itemView.context).logEventFirebase("쇼핑_홈_맞춤식진단", "Click Event", "쇼핑탭 화면에서 바로 맞춤식진단 버튼 클릭")
                context.startActivity<MatchFoodActivity> {
                    putExtra("item", item)
                }
            } else {
                OneBtnDialog(context).apply {
                    setTitle("")
                    setMessage("커스터밀 맞춤식은 현재 강아지를 위한 제품입니다. 강아지를 제외한 다른 반려동물을 위한 맞춤식은 준비중이니 조금만 기다려주세요.")
                    setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                        dismiss()
                    })
                    show()
                }
            }
        }

        private fun onMatchFoodResult(item: PetInfoItem) {
            val context = itemView.context
            Airbridge.trackEvent("shopping", "click", "customeal_result", null, null, null)
            FirebaseAPI(context).logEventFirebase("쇼핑_홈_진단결과", "Click Event", "쇼핑탭 화면에서 바로 진단결과 버튼 클릭")
            context.startActivity<MatchFoodResultActivity> {
                putExtra("item", item)
                putExtra("type", "activity")
            }
        }

        private fun onPurchase(item: PetInfoItem) {
            itemView.context.startActivity<MatchFoodResultActivity> {
                putExtra("item", item)
                putExtra("type", "activity")
                putExtra("isPurchase", true)
            }
        }

        private fun onPetAdd(item: PetInfoItem) {
            val context = itemView.context
            Airbridge.trackEvent("shopping", "click", "customeal_add", null, null, null)
            FirebaseAPI(context).logEventFirebase("쇼핑_홈_동물등록", "Click Event", "쇼핑탭 화면에서 바로 동물등록 버튼 클릭")
            context.startActivity<PetAddActivity> {
                putExtra("type", PetAddType.ADD.ordinal)
                putExtra("step", item.regPetStep.plus(1))
                putExtra("item", item)
            }
        }
    }
}