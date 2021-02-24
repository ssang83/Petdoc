package kr.co.petdoc.petdoc.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView

class HorizontalMarginItemDecoration(
    @IntRange(from = 0) private val marginStart: Int = 0,
    @IntRange(from = 0) private val marginBetween: Int,
    @IntRange(from = 0) private val marginEnd: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemCount = parent.adapter?.itemCount?:0
        when (parent.getChildAdapterPosition(view)) {
            0 -> {
                outRect.left = marginStart
                outRect.right = (marginBetween / 2)
            }
            (itemCount - 1) -> {
                outRect.left = (marginBetween / 2)
                outRect.right = marginEnd
            }
            else -> {
                outRect.right = (marginBetween / 2)
                outRect.left = (marginBetween / 2)
            }
        }
    }
}