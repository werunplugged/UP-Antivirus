package com.unplugged.up_antivirus.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import com.unplugged.antivirus.R


class CircleImageView(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageView(context, attrs) {

    init {
        //the outline (view edges) of the view should be derived    from the background
        outlineProvider = ViewOutlineProvider.BACKGROUND

        //cut the view to match the view to the outline of the background
        clipToOutline = true

        //use the following background to calculate the outline
        setBackgroundResource(R.drawable.image_round_bg)

        //fill in the whole image view, crop if needed while keeping the center
        scaleType = ScaleType.CENTER_CROP
    }
}