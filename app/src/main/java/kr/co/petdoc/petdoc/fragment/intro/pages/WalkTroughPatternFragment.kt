package kr.co.petdoc.petdoc.fragment.intro.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_pet_hospital.*
import kotlinx.android.synthetic.main.fragment_walktrough.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants

/**
 * Petdoc
 * Class: WalkTroughPattern
 * Created by kimjoonsung on 2020/11/13.
 *
 * Description :
 */
class WalkTroughPatternFragment(var walkTroughRes: Int, var _title:String, var _content:String) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_walktrough, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title.text = _title
        content.text = _content

        Glide.with(requireContext())
                .load(walkTroughRes)
                .into(walktroughImg)

    }
}