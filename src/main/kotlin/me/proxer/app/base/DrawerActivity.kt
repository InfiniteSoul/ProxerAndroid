package me.proxer.app.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
import me.proxer.app.MainActivity
import me.proxer.app.MainApplication.Companion.bus
import me.proxer.app.R
import me.proxer.app.auth.LoginDialog
import me.proxer.app.auth.LoginEvent
import me.proxer.app.auth.LogoutDialog
import me.proxer.app.auth.LogoutEvent
import me.proxer.app.notification.NotificationActivity
import me.proxer.app.profile.ProfileActivity
import me.proxer.app.ucp.UcpActivity
import me.proxer.app.util.data.StorageHelper
import me.proxer.app.util.wrapper.MaterialDrawerWrapper
import me.proxer.app.util.wrapper.MaterialDrawerWrapper.AccountItem
import me.proxer.app.util.wrapper.MaterialDrawerWrapper.DrawerItem
import me.proxer.library.enums.Device
import me.proxer.library.util.ProxerUrls
import kotlin.properties.Delegates

/**
 * @author Ruben Gees
 */
@Suppress("UnnecessaryAbstractClass")
abstract class DrawerActivity : BaseActivity() {

    protected open val contentView
        get() = R.layout.activity_default

    protected var drawer by Delegates.notNull<MaterialDrawerWrapper>()
        private set

    protected open val isRootActivity = false
    protected open val isMainActivity = false

    protected open val root: ViewGroup by bindView(R.id.root)
    protected open val toolbar: Toolbar by bindView(R.id.toolbar)
    protected open val appbar: AppBarLayout by bindView(R.id.appbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(contentView)
        setSupportActionBar(toolbar)

        drawer = MaterialDrawerWrapper(this, toolbar, savedInstanceState, isRootActivity, isMainActivity).also {
            it.itemClickSubject
                .autoDisposable(this.scope())
                .subscribe { item -> handleDrawerItemClick(item) }

            it.accountClickSubject
                .autoDisposable(this.scope())
                .subscribe { item -> handleAccountItemClick(item) }
        }

        Observable.merge(bus.register(LoginEvent::class.java), bus.register(LogoutEvent::class.java))
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this.scope())
            .subscribe { drawer.refreshHeader() }
    }

    override fun onResume() {
        super.onResume()

        drawer.refreshHeader()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        drawer.saveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (!drawer.onBackPressed()) {
            super.onBackPressed()
        }
    }

    protected open fun handleDrawerItemClick(item: MaterialDrawerWrapper.DrawerItem) = when (item) {
        DrawerItem.DONATE -> showPage(ProxerUrls.donateWeb(Device.DEFAULT))
        else -> MainActivity.navigateToSection(this, item)
    }

    protected open fun handleAccountItemClick(item: MaterialDrawerWrapper.AccountItem) = when (item) {
        AccountItem.GUEST, AccountItem.LOGIN -> LoginDialog.show(this)
        AccountItem.LOGOUT -> LogoutDialog.show(this)
        AccountItem.USER -> showProfilePage()
        AccountItem.NOTIFICATIONS -> NotificationActivity.navigateTo(this)
        AccountItem.UCP -> UcpActivity.navigateTo(this)
    }

    private fun showProfilePage() = StorageHelper.user?.let { (_, id, name, image) ->
        ProfileActivity.navigateTo(this, id, name, image, null)
    }
}
