package kr.co.petdoc.petdoc.fragment.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_my_post_test.*
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.PetTalkDetailActivity
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentMyPostTestBinding
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.network.response.submodel.MyPetTalkData
import kr.co.petdoc.petdoc.viewmodel.MyPostViewModel
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Petdoc
 * Class: MyPostBindingFragment
 * Created by kimjoonsung on 1/11/21.
 *
 * Description :
 */
class MyPostBindingFragment : PetdocBaseFragment<FragmentMyPostTestBinding, MyPostViewModel>() {

    private val viewModel: MyPostViewModel by viewModel()

    override fun getTargetViewModel(): MyPostViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_my_post_test

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.start()
        setUpView()
    }

    private fun setUpView() {
        btnBack.setOnSingleClickListener { requireActivity().onBackPressed() }
    }

    private fun onItemClicked(item: MyPetTalkData) {
        startActivity(Intent(requireActivity(), PetTalkDetailActivity::class.java).apply {
            putExtra("petTalkId", item.id)
        })
    }
}