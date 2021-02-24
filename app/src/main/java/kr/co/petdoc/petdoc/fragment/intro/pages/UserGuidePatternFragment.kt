package kr.co.petdoc.petdoc.fragment.intro.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_intro_guide_template.*
import kr.co.petdoc.petdoc.R

class UserGuidePatternFragment(var animationResource:Int, var titleRes:Int, var contentRes:Int) : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_intro_guide_template, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intro_user_guide_animation_area.apply{
            setAnimation(animationResource)
        }

        intro_user_guide_title_area.text = requireContext().getString(titleRes)
        intro_user_guide_content_area.text = requireContext().getString(contentRes)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)

        if(menuVisible){
            if(!intro_user_guide_animation_area.isAnimating) intro_user_guide_animation_area.playAnimation()
        }else{
            intro_user_guide_animation_area.cancelAnimation()
        }
    }
}