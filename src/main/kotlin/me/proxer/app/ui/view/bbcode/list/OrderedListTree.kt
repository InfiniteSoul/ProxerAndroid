package me.proxer.app.ui.view.bbcode.list

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.AppCompatTextView
import android.view.Gravity.CENTER
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout.VERTICAL
import me.proxer.app.R
import me.proxer.app.ui.view.bbcode.BBTree
import org.jetbrains.anko.dip

/**
 * @author Ruben Gees
 */
class OrderedListTree(parent: BBTree?, children: MutableList<BBTree> = mutableListOf()) : BBTree(parent, children) {

    override val prototype = OrderedListPrototype

    @SuppressLint("SetTextI18n")
    override fun makeViews(context: Context): List<View> {
        val children = children.filterIsInstance(ListItemTree::class.java).flatMap { it.makeViews(context) }

        return listOf(LinearLayout(context).apply {
            val eightDip = dip(8)

            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = VERTICAL

            children.forEachIndexed { index, it ->
                addView(LinearLayout(context).apply {
                    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    orientation = HORIZONTAL

                    addView(AppCompatTextView(context).apply {
                        TextViewCompat.setTextAppearance(this, R.style.TextAppearance_AppCompat_Small)

                        layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                            setMargins(0, 0, eightDip, 0)

                            gravity = CENTER
                        }

                        text = "${index + 1}."
                    })

                    addView(it)
                })
            }
        })
    }
}
