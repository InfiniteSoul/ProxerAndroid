package me.proxer.app.ui.view.bbcode2

import android.view.View
import android.widget.TextView
import org.jetbrains.anko.childrenRecursiveSequence

/**
 * @author Ruben Gees
 */
object BBUtils {

    fun applyToTextViews(views: List<View>, operation: (TextView) -> Unit): List<View> {
        return views.map { view ->
            if (view is TextView) {
                operation(view)
            }

            view.childrenRecursiveSequence().plus(view)
                    .filterIsInstance(TextView::class.java)
                    .forEach(operation)

            view
        }
    }
}
