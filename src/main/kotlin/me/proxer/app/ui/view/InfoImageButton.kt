package me.proxer.app.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.TooltipCompat

/**
 * @author Ruben Gees
 */
class InfoImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    init {
        contentDescription?.let {
            TooltipCompat.setTooltipText(this, it)
        }
    }
}
