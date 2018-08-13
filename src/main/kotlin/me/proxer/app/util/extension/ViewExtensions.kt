@file:Suppress("NOTHING_TO_INLINE")

package me.proxer.app.util.extension

import android.animation.LayoutTransition
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.text.PrecomputedTextCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import me.proxer.app.R
import me.saket.bettermovementmethod.BetterLinkMovementMethod

inline var AppCompatTextView.fastText: CharSequence
    get() = text
    set(value) {
        val textMetrics = TextViewCompat.getTextMetricsParams(this)

        setTextFuture(PrecomputedTextCompat.getTextFuture(value, textMetrics, null))
    }

inline fun ImageView.setIconicsImage(
    icon: IIcon,
    sizeDp: Int,
    paddingDp: Int = sizeDp / 4,
    colorRes: Int = R.color.icon
) {
    setImageDrawable(
        IconicsDrawable(context, icon)
            .sizeDp(sizeDp)
            .paddingDp(paddingDp)
            .colorRes(context, colorRes)
    )
}

inline fun IconicsDrawable.colorRes(context: Context, id: Int): IconicsDrawable {
    return this.color(ContextCompat.getColor(context, id))
}

inline fun IconicsDrawable.iconColor(context: Context): IconicsDrawable {
    return this.colorRes(context, R.color.icon)
}

inline fun TextView.setSimpleOnLinkClickListener(crossinline listener: (view: TextView, link: String) -> Unit) {
    setOnLinkClickListener { view, link ->
        listener(view, link)

        true
    }
}

inline fun TextView.setSimpleOnLinkLongClickListener(crossinline listener: (view: TextView, link: String) -> Unit) {
    setOnLinkLongClickListener { view, link ->
        listener(view, link)

        true
    }
}

inline fun TextView.setOnLinkClickListener(noinline listener: (view: TextView, link: String) -> Boolean) {
    val listenerWrapper = BetterLinkMovementMethod.OnLinkClickListener(listener)

    movementMethod.let {
        if (it is BetterLinkMovementMethod) {
            it.setOnLinkClickListener(listenerWrapper)
        } else {
            movementMethod = BetterLinkMovementMethod.newInstance().setOnLinkClickListener(listenerWrapper)
        }
    }
}

inline fun TextView.setOnLinkLongClickListener(noinline listener: (view: TextView, link: String) -> Boolean) {
    val listenerWrapper = BetterLinkMovementMethod.OnLinkLongClickListener(listener)

    movementMethod.let {
        if (it is BetterLinkMovementMethod) {
            it.setOnLinkLongClickListener(listenerWrapper)
        } else {
            movementMethod = BetterLinkMovementMethod.newInstance().setOnLinkLongClickListener(listenerWrapper)
        }
    }
}

inline fun ViewGroup.enableLayoutAnimationsSafely() {
    this.layoutTransition = LayoutTransition().apply { setAnimateParentHierarchy(false) }
}

fun RecyclerView.LayoutManager.isAtCompleteTop() = when (this) {
    is StaggeredGridLayoutManager -> findFirstCompletelyVisibleItemPositions(null).contains(0)
    is LinearLayoutManager -> findFirstCompletelyVisibleItemPosition() == 0
    else -> false
}

fun RecyclerView.LayoutManager.isAtTop() = when (this) {
    is StaggeredGridLayoutManager -> findFirstVisibleItemPositions(null).contains(0)
    is LinearLayoutManager -> findFirstVisibleItemPosition() == 0
    else -> false
}

fun RecyclerView.LayoutManager.scrollToTop() = when (this) {
    is StaggeredGridLayoutManager -> scrollToPositionWithOffset(0, 0)
    is LinearLayoutManager -> scrollToPositionWithOffset(0, 0)
    else -> Unit
}
