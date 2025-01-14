package com.unplugged.up_antivirus.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CellMarginDecoration(private val topBottom: Int, private val leftRight: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = topBottom
            }
            left = leftRight
            right = leftRight
            bottom = topBottom
        }
    }
}