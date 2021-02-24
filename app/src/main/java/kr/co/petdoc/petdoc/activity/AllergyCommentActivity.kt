package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_allerge_comment.*
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.PetdocBaseActivity
import kr.co.petdoc.petdoc.base.UiState
import kr.co.petdoc.petdoc.databinding.ActivityAllergeCommentBinding
import kr.co.petdoc.petdoc.viewmodel.AllergeCommentViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

const val EXTRA_ALLERGY_COMMENT_CATEGORY = "allergeCategory"

class AllergyCommentActivity
    : PetdocBaseActivity<ActivityAllergeCommentBinding, AllergeCommentViewModel>() {
    private val categoryName by lazy {
        intent.getStringExtra(EXTRA_ALLERGY_COMMENT_CATEGORY)
            ?: throw IllegalStateException("category must set for starting AllergeCommentActivity")
    }
    private val viewModel: AllergeCommentViewModel by viewModel {
        parametersOf(categoryName)
    }

    override fun getLayoutId(): Int = R.layout.activity_allerge_comment

    override fun getTargetViewModel() = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {
            start()

            uiState.observe(this@AllergyCommentActivity, Observer { state ->
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