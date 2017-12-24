package me.proxer.app.ui.view.bbcode.tree

import android.content.Context
import android.support.annotation.ColorInt
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import me.proxer.app.ui.view.bbcode.BBUtils.applyToTextViews
import me.proxer.app.ui.view.bbcode.prototype.ColorPrototype

/**
 * @author Ruben Gees
 */
class ColorTree(
        @ColorInt private val color: Int,
        parent: BBTree?,
        children: MutableList<BBTree> = mutableListOf()
) : BBTree(parent, children) {

    override val prototype = ColorPrototype

    override fun makeViews(context: Context): List<View> {
        val childViews = super.makeViewsWithoutMerging(context)

        return applyToTextViews(childViews) { view ->
            view.text = SpannableStringBuilder(view.text).apply {
                setSpan(ForegroundColorSpan(color), 0, view.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }
}