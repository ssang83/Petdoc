package kr.co.petdoc.petdoc.bindingadapter

import androidx.databinding.BindingAdapter
import kr.co.petdoc.petdoc.domain.AllergyComment
import kr.co.petdoc.petdoc.widget.AllergeCommentView

@BindingAdapter("allergeComment")
fun bindComment(
    view: AllergeCommentView,
    comment: AllergyComment?
) {
    comment?.let {
        view.bindComment(comment)
    }
}