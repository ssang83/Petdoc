package kr.co.petdoc.petdoc.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView

class VerticalMarginItemDecoration(
    @IntRange(from = 0) private val marginTop: Int = 0,
    @IntRange(from = 0) private val marginBetween: Int = 0,
    @IntRange(from = 0) private val marginBottom: Int = 0
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
                outRect.top = marginTop
                outRect.bottom = (marginBetween / 2)
            }
            (itemCount - 1) -> {
                outRect.top = (marginBetween / 2)
                outRect.bottom = marginBottom
            }
            else -> {
                outRect.top = (marginBetween / 2)
                outRect.bottom = (marginBetween / 2)
            }
        }
    }
}