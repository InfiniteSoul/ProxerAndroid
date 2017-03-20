package me.proxer.app.util

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import me.proxer.app.R

class MarginDecoration(context: Context, private val columns: Int) : RecyclerView.ItemDecoration() {

    private val margin = context.resources.getDimensionPixelSize(R.dimen.item_margin)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val position = parent.getChildLayoutPosition(view)
        val spanIndex = (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex
        val itemCount = parent.adapter.itemCount

        val isAtTop = position - columns < 0
        val isAtBottom = position >= itemCount - columns

        if (!isAtTop) {
            outRect.top = margin
        }

        if (!isAtBottom) {
            outRect.bottom = margin
        }

        if (columns > 1) {
            val isLeft = spanIndex == 0
            val isRight = spanIndex == columns - 1

            if (!isLeft) {
                outRect.left = margin
            }

            if (!isRight) {
                outRect.right = margin
            }
        }
    }
}
