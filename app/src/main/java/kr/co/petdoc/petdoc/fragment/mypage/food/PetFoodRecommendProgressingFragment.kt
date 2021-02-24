package kr.co.petdoc.petdoc.fragment.mypage.food

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_petfood_progressing.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel

class PetFoodRecommendProgressingFragment : Fragment(){

    private var foodRecommendDataModel : FoodRecommentDataModel? = null
    private var progressAnimate : ValueAnimator? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        return inflater.inflate(R.layout.fragment_petfood_progressing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgUrl = foodRecommendDataModel?.petimage?.value.toString()
        if (imgUrl.isNullOrBlank()) {
            Glide.with(requireActivity())
                .load(R.drawable.pet_default_image_white)
                .apply(RequestOptions.circleCropTransform())
                .into(petfood_recommend_progressing_pet_image)
        } else {
            Glide.with(requireActivity())
                .load(if(imgUrl.startsWith("http")) imgUrl else "${AppConstants.IMAGE_URL}${imgUrl}")
                .apply(RequestOptions.circleCropTransform())
                .into(petfood_recommend_progressing_pet_image)
        }

        try {
            progressAnimate = ValueAnimator.ofFloat(0f, 360f).apply{
                duration = 3000
                startDelay = 400
                addUpdateListener {
                    petfood_recommend_progressing_bar.progress = it.animatedValue as Float
                    petfood_recommend_progressing_bar.invalidate()
                    if(it.animatedFraction == 1f){
                        findNavController().navigate(PetFoodRecommendProgressingFragmentDirections.actionFoodprogressingToMypetfoodlist())
                    }
                }
            }
            progressAnimate?.start()
        } catch (e: Exception) {
            Logger.p(e)
        }

    }



}