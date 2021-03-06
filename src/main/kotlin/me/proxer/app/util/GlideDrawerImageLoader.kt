package me.proxer.app.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import me.proxer.app.GlideApp
import me.proxer.app.R
import me.proxer.app.util.extension.backgroundColorAttr
import me.proxer.app.util.extension.colorAttr

/**
 * @author Ruben Gees
 */
class GlideDrawerImageLoader : AbstractDrawerImageLoader() {

    override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
        GlideApp.with(imageView)
            .load(uri)
            .centerCrop()
            .placeholder(placeholder)
            .into(imageView)
    }

    override fun cancel(imageView: ImageView) = GlideApp.with(imageView).clear(imageView)

    override fun placeholder(ctx: Context, tag: String?): IconicsDrawable = IconicsDrawable(ctx)
        .icon(CommunityMaterial.Icon4.cmd_account)
        .sizeDp(48)
        .colorAttr(ctx, R.attr.colorOnPrimary)
        .backgroundColorAttr(ctx, R.attr.colorPrimary)
}
