package me.proxer.app.manga

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE
import android.view.View.SYSTEM_UI_FLAG_VISIBLE
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.commitNow
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.systemUiVisibilityChanges
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import kotterknife.bindView
import me.proxer.app.R
import me.proxer.app.base.BaseActivity
import me.proxer.app.media.MediaActivity
import me.proxer.app.util.extension.getSafeStringExtra
import me.proxer.app.util.extension.startActivity
import me.proxer.app.util.extension.toEpisodeAppString
import me.proxer.library.enums.Category
import me.proxer.library.enums.Language
import me.proxer.library.util.ProxerUrls
import me.proxer.library.util.ProxerUtils
import java.lang.ref.WeakReference

/**
 * @author Ruben Gees
 */
class MangaActivity : BaseActivity() {

    companion object {
        private const val ID_EXTRA = "id"
        private const val EPISODE_EXTRA = "episode"
        private const val LANGUAGE_EXTRA = "language"
        private const val CHAPTER_TITLE_EXTRA = "chapter_title"
        private const val NAME_EXTRA = "name"
        private const val EPISODE_AMOUNT_EXTRA = "episode_amount"

        fun navigateTo(
            context: Activity,
            id: String,
            episode: Int,
            language: Language,
            chapterTitle: String?,
            name: String? = null,
            episodeAmount: Int? = null
        ) {
            context.startActivity<MangaActivity>(
                ID_EXTRA to id,
                EPISODE_EXTRA to episode,
                LANGUAGE_EXTRA to language,
                CHAPTER_TITLE_EXTRA to chapterTitle,
                NAME_EXTRA to name,
                EPISODE_AMOUNT_EXTRA to episodeAmount
            )
        }
    }

    val id: String
        get() = when (intent.hasExtra(ID_EXTRA)) {
            true -> intent.getSafeStringExtra(ID_EXTRA)
            false -> intent.data?.pathSegments?.getOrNull(1) ?: "-1"
        }

    var episode: Int
        get() = when (intent.hasExtra(EPISODE_EXTRA)) {
            true -> intent.getIntExtra(EPISODE_EXTRA, 1)
            false -> intent.data?.pathSegments?.getOrNull(2)?.toIntOrNull() ?: 1
        }
        set(value) {
            intent.putExtra(EPISODE_EXTRA, value)

            updateTitle()
        }

    val language: Language
        get() = when (intent.hasExtra(LANGUAGE_EXTRA)) {
            true -> intent.getSerializableExtra(LANGUAGE_EXTRA) as Language
            false -> intent.data?.pathSegments?.getOrNull(3)?.let { ProxerUtils.toApiEnum<Language>(it) }
                ?: Language.ENGLISH
        }

    var chapterTitle: String?
        get() = intent.getStringExtra(CHAPTER_TITLE_EXTRA)
        set(value) {
            intent.putExtra(CHAPTER_TITLE_EXTRA, value)

            updateTitle()
        }

    var name: String?
        get() = intent.getStringExtra(NAME_EXTRA)
        set(value) {
            intent.putExtra(NAME_EXTRA, value)

            updateTitle()
        }

    var episodeAmount: Int?
        get() = when (intent.hasExtra(EPISODE_AMOUNT_EXTRA)) {
            true -> intent.getIntExtra(EPISODE_AMOUNT_EXTRA, 1)
            else -> null
        }
        set(value) {
            when (value) {
                null -> intent.removeExtra(EPISODE_AMOUNT_EXTRA)
                else -> intent.putExtra(EPISODE_AMOUNT_EXTRA, value)
            }
        }

    private val toolbar: Toolbar by bindView(R.id.toolbar)

    private val fullscreenHandler = FullscreenHandler(WeakReference(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_manga)
        setSupportActionBar(toolbar)

        setupToolbar()
        updateTitle()

        window.decorView.systemUiVisibilityChanges()
            .skip(1)
            .autoDisposable(this.scope())
            .subscribe { handleUIChange() }

        toggleFullscreen(false)

        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                replace(R.id.container, MangaFragment.newInstance())
            }
        }
    }

    override fun onDestroy() {
        fullscreenHandler.removeCallbacksAndMessages(null)

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        IconicsMenuInflaterUtil.inflate(menuInflater, this, R.menu.activity_share, menu, true)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> name?.let {
                val link = ProxerUrls.mangaWeb(id, episode, language)

                val text = chapterTitle.let { title ->
                    when {
                        title.isNullOrBlank() -> getString(R.string.share_manga, episode, it, link)
                        else -> getString(R.string.share_manga_title, title, it, link)
                    }
                }

                ShareCompat.IntentBuilder
                    .from(this)
                    .setText(text)
                    .setType("text/plain")
                    .setChooserTitle(getString(R.string.share_title))
                    .startChooser()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration?) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)

        handleUIChange()
    }

    fun toggleFullscreen(fullscreen: Boolean, delay: Long = 0) {
        val fullscreenMessage = Message().apply {
            what = 0
            obj = fullscreen
        }

        fullscreenHandler.removeMessages(0)

        if (delay <= 0L) {
            fullscreenHandler.handleMessage(fullscreenMessage)
        } else {
            fullscreenHandler.sendMessageDelayed(fullscreenMessage, delay)
        }
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.clicks()
            .autoDisposable(this.scope())
            .subscribe {
                name?.let {
                    MediaActivity.navigateTo(this, id, name, Category.MANGA)
                }
            }
    }

    private fun updateTitle() {
        title = name
        supportActionBar?.subtitle = chapterTitle ?: Category.MANGA.toEpisodeAppString(this, episode)
    }

    private fun handleUIChange() {
        val isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && this.isInMultiWindowMode
        val isInFullscreenMode = window.decorView.systemUiVisibility and SYSTEM_UI_FLAG_FULLSCREEN != 0

        toolbar.updateLayoutParams<AppBarLayout.LayoutParams> {
            scrollFlags = when {
                isInMultiWindowMode -> SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
                isInFullscreenMode -> SCROLL_FLAG_SCROLL
                else -> SCROLL_FLAG_NO_SCROLL
            }
        }

        if (isInFullscreenMode) {
            if (isInMultiWindowMode) {
                toggleFullscreen(false)
            } else {
                toggleFullscreen(true)
            }
        } else {
            toggleFullscreen(false)

            if (!isInMultiWindowMode) {
                toggleFullscreen(true, 2_000)
            }
        }
    }

    private class FullscreenHandler(private val activity: WeakReference<MangaActivity>) : Handler() {
        override fun handleMessage(message: Message) {
            this.activity.get()?.apply {
                val isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && this.isInMultiWindowMode

                if (message.obj as Boolean && !isInMultiWindowMode) {
                    window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LOW_PROFILE or
                        SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        SYSTEM_UI_FLAG_FULLSCREEN or
                        SYSTEM_UI_FLAG_IMMERSIVE
                } else {
                    window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_VISIBLE or
                        SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        SYSTEM_UI_FLAG_IMMERSIVE
                }
            }
        }
    }
}
