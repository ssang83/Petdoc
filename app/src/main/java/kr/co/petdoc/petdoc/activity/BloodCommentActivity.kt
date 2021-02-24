package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_blood_comment.*
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.PetdocBaseActivity
import kr.co.petdoc.petdoc.base.UiState
import kr.co.petdoc.petdoc.databinding.ActivityBloodCommentBinding
import kr.co.petdoc.petdoc.viewmodel.BloodCommentViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Petdoc
 * Class: BloodCommentActivity
 * Created by kimjoonsung on 1/26/21.
 *
 * Description :
 */

const val EXTRA_BLOOD_COMMENT_CATEGORY = "bloodCategory"

class BloodCommentActivity
    : PetdocBaseActivity<ActivityBloodCommentBinding, BloodCommentViewModel>() {

    private val categoryName by lazy {
        intent.getStringExtra(EXTRA_BLOOD_COMMENT_CATEGORY)
            ?: throw IllegalStateException("category must set for starting BloodCommentActivity")
    }

    private val viewModel: BloodCommentViewModel by viewModel {
        parametersOf(categoryName)
    }

    override fun getLayoutId(): Int = R.layout.activity_blood_comment

    override fun getTargetViewModel() = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {
            start()

            uiState.observe(this@BloodCommentActivity, Observer { state ->
                if (state is UiState.Loading) {
                    showLoadingDialog()
                } else {
                    dismissLoadingDialog()
                }
            })
        }

        setUpViews()
    }

    private fun setUpViews() {
        btnClose.setOnClickListener { finish() }
    }
}