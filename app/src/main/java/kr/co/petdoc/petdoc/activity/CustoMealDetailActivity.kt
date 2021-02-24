package kr.co.petdoc.petdoc.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_banner_detail.*
import kotlinx.android.synthetic.main.activity_custo_meal_detail.*
import kotlinx.android.synthetic.main.activity_custo_meal_detail.root
import kotlinx.android.synthetic.main.activity_custo_meal_detail.scrollView
import kotlinx.android.synthetic.main.shopping_custo_meal_item.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.mypage.PetInformationFragmentDirections
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.OneBtnDialog

/**
 * Petdoc
 * Class: CustoMealDetailActivity
 * Created by kimjoonsung on 2020/07/10.
 *
 * Description :
 */
class CustoMealDetailActivity : AppCompatActivity() {

    private var petInfoItem:PetInfoItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha=0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custo_meal_detail)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        FirebaseAPI(this).logEventFirebase("커스터밀_상세", "Page View", "커스터밀 상세 페이지의 page view")

        petInfoItem = intent?.getParcelableExtra("item")!!

        btnTop.setOnClickListener { scrollView.scrollTo(0, 0) }
        btnBack.setOnClickListener { finish() }

        btnConfirm.setOnClickListener {
            if (petInfoItem?.kind == "강아지") {
                when {
                    petInfoItem?.regInfoAllStep == petInfoItem?.regInfoStep -> {
                        FirebaseAPI(this).logEventFirebase("커스터밀_상세_진단결과", "Click Event", "커스터밀 상세 페이지에서 진단결과 버튼 클릭")
                        startActivity(Intent(this, MatchFoodResultActivity::class.java).apply {
                            putExtra("item", petInfoItem)
                            putExtra("type", "activity")
                        })
                    }
                    petInfoItem?.regPetStep!! == 6 -> matchFood()
                    else -> petAdd()
                }
            } else {
                OneBtnDialog(this).apply {
                    setTitle("")
                    setMessage("커스터밀 맞춤식은 현재 강아지를 위한 제품입니다. 강아지를 제외한 다른 반려동물을 위한 맞춤식은 준비중이니 조금만 기다려주세요.")
                    setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                        dismiss()
                    })
                    show()
                }
            }
        }

        //===========================================================================================

        scrollView.viewTreeObserver.addOnScrollChangedListener(object :
            ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                if (scrollView != null) {
                    if (!scrollView.canScrollVertically(-1)) {
                        layoutHeader.setBackgroundResource(R.color.colorfbe5da)
                    } else {
                        layoutHeader.setBackgroundResource(R.color.colorfff7f0)
                    }
                }
            }
        })

        btnConfirm.apply {
            when {
                petInfoItem?.regInfoAllStep == petInfoItem?.regInfoStep -> text = "맞춤식 진단결과"
                petInfoItem?.regPetStep!! == 6 -> text = "맞춤식 진단받기"
                else -> text = "반려동물 등록하기"
            }
        }
    }

    private fun petAdd() {
        FirebaseAPI(this).logEventFirebase("커스터밀_상세_동물등록", "Click Event", "커스터밀 상세 페이지에서 동물등록 버튼 클릭")
        when (petInfoItem?.regPetStep!!.plus(1)) {
            6 -> {
                startActivity(Intent(this, PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 6)
                    putExtra("item", petInfoItem)
                })
            }

            5 -> {
                startActivity(Intent(this, PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 5)
                    putExtra("item", petInfoItem)
                })
            }

            4 -> {
                startActivity(Intent(this, PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 4)
                    putExtra("item", petInfoItem)
                })
            }

            3 -> {
                startActivity(Intent(this, PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 3)
                    putExtra("item", petInfoItem)
                })
            }

            2 -> {
                startActivity(Intent(this, PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 2)
                    putExtra("item", petInfoItem)
                })
            }

            1 -> {
                startActivity(Intent(this, PetAddActivity::class.java).apply {
                    putExtra("type", PetAddType.ADD.ordinal)
                    putExtra("step", 1)
                    putExtra("item", petInfoItem)
                })
            }
        }
    }

    private fun matchFood() {
        if (petInfoItem?.kind!! == "강아지") {
            FirebaseAPI(this).logEventFirebase("커스터밀_상세_맞춤식진단", "Click Event", "커스터밀 상세 페이지에서 맞춤식진단 버튼 클릭")
            startActivity(Intent(this, MatchFoodActivity::class.java).apply {
                putExtra("item", petInfoItem)
            })
        } else {
            OneBtnDialog(this).apply {
                setTitle("커스터밀")
                setMessage("커스터밀 맞춤식은 현재 강아지를 위한 제품입니다. ${petInfoItem?.kind!!}를 위한 맞춤식은 준비중이니 조금만 기다려 주세요.")
                setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                    dismiss()
                })
                show()
            }
        }
    }
}